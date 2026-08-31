package com.leonardo.bank_api.pix.pix;

import com.leonardo.bank_api.account.entity.Account;
import com.leonardo.bank_api.account.repository.AccountRepository;
import com.leonardo.bank_api.common.exception.BusinessException;
import com.leonardo.bank_api.customer.entity.Customer;
import com.leonardo.bank_api.customer.repository.CustomerRepository;
import com.leonardo.bank_api.pix.dto.request.PixTransferRequest;
import com.leonardo.bank_api.pix.dto.request.UpdatePixLimitRequest;
import com.leonardo.bank_api.pix.dto.response.PixLimitResponse;
import com.leonardo.bank_api.pix.entity.PixKey;
import com.leonardo.bank_api.pix.repository.PixIdempotencyRepository;
import com.leonardo.bank_api.pix.repository.PixKeyRepository;
import com.leonardo.bank_api.pix.service.PixService;
import com.leonardo.bank_api.shared.enums.AccountStatus;
import com.leonardo.bank_api.shared.enums.PixKeyType;
import com.leonardo.bank_api.shared.enums.TransactionStatus;
import com.leonardo.bank_api.shared.enums.TransactionType;
import com.leonardo.bank_api.transaction.dto.response.TransactionResponse;
import com.leonardo.bank_api.transaction.entity.Transaction;
import com.leonardo.bank_api.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class PixIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private PixService pixService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PixKeyRepository pixKeyRepository;

    @Autowired
    private PixIdempotencyRepository pixIdempotencyRepository;

    @Autowired
    private TransactionRepository transactionAuditRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    private TransactionRepository transactionRepository;

    @BeforeEach
    void cleanDatabase() {

        jdbcTemplate.execute("""
            TRUNCATE TABLE
                transaction_audit,
                pix_idempotency,
                pix_schedules,
                transactions,
                pix_keys,
                accounts,
                customers
            RESTART IDENTITY CASCADE
            """);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldTransferPixAndPersistBalances() {

        Customer senderCustomer = customerRepository.save(
                Customer.builder()
                        .fullName("Cliente Origem")
                        .cpf("52998224725")
                        .email("origem@email.com")
                        .build()
        );

        Customer receiverCustomer = customerRepository.save(
                Customer.builder()
                        .fullName("Cliente Destino")
                        .cpf("11144477735")
                        .email("destino@email.com")
                        .build()
        );

        Account sourceAccount = accountRepository.save(
                Account.builder()
                        .number("00000001")
                        .agency("0001")
                        .balance(new BigDecimal("500.00"))
                        .status(AccountStatus.ACTIVE)
                        .customer(senderCustomer)
                        .build()
        );

        Account destinationAccount = accountRepository.save(
                Account.builder()
                        .number("00000002")
                        .agency("0001")
                        .balance(new BigDecimal("200.00"))
                        .status(AccountStatus.ACTIVE)
                        .customer(receiverCustomer)
                        .build()
        );

        PixKey pixKey = pixKeyRepository.save(
                PixKey.builder()
                        .type(PixKeyType.EMAIL)
                        .keyValue("destino@email.com")
                        .account(destinationAccount)
                        .build()
        );

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "origem@email.com",
                                null
                        )
                );

        PixTransferRequest request =
                new PixTransferRequest(
                        pixKey.getKeyValue(),
                        new BigDecimal("100.00")
                );

        TransactionResponse response =
                pixService.transfer(
                        sourceAccount.getId(),
                        request,
                        newIdempotencyKey()
                );

        Account updatedSource =
                accountRepository.findById(sourceAccount.getId())
                        .orElseThrow();

        Account updatedDestination =
                accountRepository.findById(destinationAccount.getId())
                        .orElseThrow();

        assertThat(updatedSource.getBalance())
                .isEqualByComparingTo("400.00");

        assertThat(updatedDestination.getBalance())
                .isEqualByComparingTo("300.00");

        assertThat(response.type())
                .isEqualTo(TransactionType.PIX);

        assertThat(response.status())
                .isEqualTo(TransactionStatus.COMPLETED);

        assertThat(response.amount())
                .isEqualByComparingTo("100.00");

        assertThat(transactionRepository.count())
                .isEqualTo(1);
    }

    @Test
    void shouldNotTransferPixWhenBalanceIsInsufficient() {

        Customer senderCustomer = customerRepository.save(
                Customer.builder()
                        .fullName("Cliente Origem")
                        .cpf("52998224725")
                        .email("origem2@email.com")
                        .build()
        );

        Customer receiverCustomer = customerRepository.save(
                Customer.builder()
                        .fullName("Cliente Destino")
                        .cpf("11144477735")
                        .email("destino2@email.com")
                        .build()
        );

        Account sourceAccount = accountRepository.save(
                Account.builder()
                        .number("00000003")
                        .agency("0001")
                        .balance(new BigDecimal("50.00"))
                        .status(AccountStatus.ACTIVE)
                        .customer(senderCustomer)
                        .build()
        );

        Account destinationAccount = accountRepository.save(
                Account.builder()
                        .number("00000004")
                        .agency("0001")
                        .balance(new BigDecimal("200.00"))
                        .status(AccountStatus.ACTIVE)
                        .customer(receiverCustomer)
                        .build()
        );

        PixKey pixKey = pixKeyRepository.save(
                PixKey.builder()
                        .type(PixKeyType.EMAIL)
                        .keyValue("destino2@email.com")
                        .account(destinationAccount)
                        .build()
        );

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "origem2@email.com",
                                null
                        )
                );

        PixTransferRequest request =
                new PixTransferRequest(
                        pixKey.getKeyValue(),
                        new BigDecimal("100.00")
                );

        assertThatThrownBy(() ->
                pixService.transfer(
                        sourceAccount.getId(),
                        request,
                        newIdempotencyKey()
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasMessage("Saldo insuficiente");

        Account updatedSource =
                accountRepository.findById(sourceAccount.getId())
                        .orElseThrow();

        Account updatedDestination =
                accountRepository.findById(destinationAccount.getId())
                        .orElseThrow();

        assertThat(updatedSource.getBalance())
                .isEqualByComparingTo("50.00");

        assertThat(updatedDestination.getBalance())
                .isEqualByComparingTo("200.00");

        assertThat(transactionRepository.count())
                .isZero();
    }

    @Test
    void shouldRollbackPixWhenTransactionPersistenceFails() {

        Customer senderCustomer = customerRepository.save(
                Customer.builder()
                        .fullName("Cliente Rollback Origem")
                        .cpf("52998224725")
                        .email("rollback.origem@email.com")
                        .build()
        );

        Customer receiverCustomer = customerRepository.save(
                Customer.builder()
                        .fullName("Cliente Rollback Destino")
                        .cpf("11144477735")
                        .email("rollback.destino@email.com")
                        .build()
        );

        Account sourceAccount = accountRepository.save(
                Account.builder()
                        .number("00000005")
                        .agency("0001")
                        .balance(new BigDecimal("500.00"))
                        .status(AccountStatus.ACTIVE)
                        .customer(senderCustomer)
                        .build()
        );

        Account destinationAccount = accountRepository.save(
                Account.builder()
                        .number("00000006")
                        .agency("0001")
                        .balance(new BigDecimal("200.00"))
                        .status(AccountStatus.ACTIVE)
                        .customer(receiverCustomer)
                        .build()
        );

        PixKey pixKey = pixKeyRepository.save(
                PixKey.builder()
                        .type(PixKeyType.EMAIL)
                        .keyValue("rollback.destino@email.com")
                        .account(destinationAccount)
                        .build()
        );

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "rollback.origem@email.com",
                                null
                        )
                );

        PixTransferRequest request =
                new PixTransferRequest(
                        pixKey.getKeyValue(),
                        new BigDecimal("100.00")
                );

        doThrow(new RuntimeException("Erro simulado ao salvar transação"))
                .when(transactionRepository)
                .save(any(Transaction.class));

        assertThatThrownBy(() ->
                pixService.transfer(
                        sourceAccount.getId(),
                        request,
                        newIdempotencyKey()
                )
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Erro simulado ao salvar transação");

        Account sourceAfterRollback =
                accountRepository.findById(sourceAccount.getId())
                        .orElseThrow();

        Account destinationAfterRollback =
                accountRepository.findById(destinationAccount.getId())
                        .orElseThrow();

        assertThat(sourceAfterRollback.getBalance())
                .isEqualByComparingTo("500.00");

        assertThat(destinationAfterRollback.getBalance())
                .isEqualByComparingTo("200.00");
    }

    @Test
    void shouldPreventConcurrentPixFromUsingSameBalance() throws Exception {

        Customer senderCustomer = customerRepository.save(
                Customer.builder()
                        .fullName("Cliente Concorrencia Origem")
                        .cpf("52998224725")
                        .email("concorrencia.origem@email.com")
                        .build()
        );

        Customer receiverCustomer = customerRepository.save(
                Customer.builder()
                        .fullName("Cliente Concorrencia Destino")
                        .cpf("11144477735")
                        .email("concorrencia.destino@email.com")
                        .build()
        );

        Account sourceAccount = accountRepository.save(
                Account.builder()
                        .number("00000007")
                        .agency("0001")
                        .balance(new BigDecimal("100.00"))
                        .status(AccountStatus.ACTIVE)
                        .customer(senderCustomer)
                        .build()
        );

        Account destinationAccount = accountRepository.save(
                Account.builder()
                        .number("00000008")
                        .agency("0001")
                        .balance(BigDecimal.ZERO)
                        .status(AccountStatus.ACTIVE)
                        .customer(receiverCustomer)
                        .build()
        );

        PixKey pixKey = pixKeyRepository.save(
                PixKey.builder()
                        .type(PixKeyType.EMAIL)
                        .keyValue("concorrencia.destino@email.com")
                        .account(destinationAccount)
                        .build()
        );

        int numberOfThreads = 2;

        ExecutorService executor =
                Executors.newFixedThreadPool(numberOfThreads);

        CountDownLatch ready =
                new CountDownLatch(numberOfThreads);

        CountDownLatch start =
                new CountDownLatch(1);

        CountDownLatch finished =
                new CountDownLatch(numberOfThreads);

        AtomicInteger successfulTransfers =
                new AtomicInteger();

        AtomicInteger failedTransfers =
                new AtomicInteger();

        Runnable pixTask = () -> {

            try {

                SecurityContextHolder.getContext()
                        .setAuthentication(
                                new UsernamePasswordAuthenticationToken(
                                        senderCustomer.getEmail(),
                                        null
                                )
                        );

                ready.countDown();

                start.await();

                /*
                 * Cada thread representa uma transferência diferente.
                 * Por isso cada uma precisa possuir sua própria
                 * Idempotency-Key.
                 */
                pixService.transfer(
                        sourceAccount.getId(),
                        new PixTransferRequest(
                                pixKey.getKeyValue(),
                                new BigDecimal("80.00")
                        ),
                        newIdempotencyKey()
                );

                successfulTransfers.incrementAndGet();

            } catch (BusinessException ex) {

                failedTransfers.incrementAndGet();

            } catch (Exception ex) {

                throw new RuntimeException(ex);

            } finally {

                SecurityContextHolder.clearContext();

                finished.countDown();
            }
        };

        executor.submit(pixTask);
        executor.submit(pixTask);

        ready.await();

        start.countDown();

        boolean completed =
                finished.await(10, TimeUnit.SECONDS);

        executor.shutdown();

        assertThat(completed).isTrue();

        Account updatedSource =
                accountRepository.findById(sourceAccount.getId())
                        .orElseThrow();

        Account updatedDestination =
                accountRepository.findById(destinationAccount.getId())
                        .orElseThrow();

        assertThat(successfulTransfers.get())
                .isEqualTo(1);

        assertThat(failedTransfers.get())
                .isEqualTo(1);

        assertThat(updatedSource.getBalance())
                .isEqualByComparingTo("20.00");

        assertThat(updatedDestination.getBalance())
                .isEqualByComparingTo("80.00");
    }

    @Test
    void shouldUpdateDailyPixLimitAndPersistInDatabase() {

        String email = "limite@email.com";

        Customer customer = customerRepository.save(
                Customer.builder()
                        .fullName("Cliente Limite PIX")
                        .cpf("12345678901")
                        .email(email)
                        .build()
        );

        Account account = accountRepository.save(
                Account.builder()
                        .number("00000009")
                        .agency("0001")
                        .balance(new BigDecimal("1000.00"))
                        .dailyPixLimit(new BigDecimal("5000.00"))
                        .status(AccountStatus.ACTIVE)
                        .customer(customer)
                        .build()
        );

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null
                        )
                );

        UpdatePixLimitRequest request =
                new UpdatePixLimitRequest(
                        new BigDecimal("8000.00")
                );

        PixLimitResponse response =
                pixService.updateDailyPixLimit(
                        account.getId(),
                        request
                );

        assertThat(response.dailyPixLimit())
                .isEqualByComparingTo("8000.00");

        Account accountFromDatabase =
                accountRepository.findById(account.getId())
                        .orElseThrow();

        assertThat(accountFromDatabase.getDailyPixLimit())
                .isEqualByComparingTo("8000.00");
    }

    @Test
    void shouldUseUpdatedDailyPixLimitWhenTransferring() {

        String senderEmail = "limite.origem@email.com";
        String receiverEmail = "limite.destino@email.com";

        Customer senderCustomer = customerRepository.save(
                Customer.builder()
                        .fullName("Cliente Origem Limite")
                        .cpf("52998224725")
                        .email(senderEmail)
                        .build()
        );

        Customer receiverCustomer = customerRepository.save(
                Customer.builder()
                        .fullName("Cliente Destino Limite")
                        .cpf("11144477735")
                        .email(receiverEmail)
                        .build()
        );

        Account sourceAccount = accountRepository.save(
                Account.builder()
                        .number("00000010")
                        .agency("0001")
                        .balance(new BigDecimal("2000.00"))
                        .dailyPixLimit(new BigDecimal("5000.00"))
                        .status(AccountStatus.ACTIVE)
                        .customer(senderCustomer)
                        .build()
        );

        Account destinationAccount = accountRepository.save(
                Account.builder()
                        .number("00000011")
                        .agency("0001")
                        .balance(new BigDecimal("500.00"))
                        .status(AccountStatus.ACTIVE)
                        .customer(receiverCustomer)
                        .build()
        );

        PixKey pixKey = pixKeyRepository.save(
                PixKey.builder()
                        .type(PixKeyType.EMAIL)
                        .keyValue(receiverEmail)
                        .account(destinationAccount)
                        .build()
        );

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                senderEmail,
                                null
                        )
                );

        // 1. Altera o limite de R$ 5.000 para R$ 1.000
        PixLimitResponse limitResponse =
                pixService.updateDailyPixLimit(
                        sourceAccount.getId(),
                        new UpdatePixLimitRequest(
                                new BigDecimal("1000.00")
                        )
                );

        assertThat(limitResponse.dailyPixLimit())
                .isEqualByComparingTo("1000.00");

        // 2. Tenta fazer um PIX de R$ 1.200
        PixTransferRequest transferRequest =
                new PixTransferRequest(
                        pixKey.getKeyValue(),
                        new BigDecimal("1200.00")
                );

        assertThatThrownBy(() ->
                pixService.transfer(
                        sourceAccount.getId(),
                        transferRequest,
                        newIdempotencyKey()
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasMessage("Limite diário de PIX excedido");

        // 3. Consulta novamente o banco
        Account sourceFromDatabase =
                accountRepository.findById(sourceAccount.getId())
                        .orElseThrow();

        Account destinationFromDatabase =
                accountRepository.findById(destinationAccount.getId())
                        .orElseThrow();

        // 4. Nenhum saldo pode ter sido alterado
        assertThat(sourceFromDatabase.getBalance())
                .isEqualByComparingTo("2000.00");

        assertThat(destinationFromDatabase.getBalance())
                .isEqualByComparingTo("500.00");

        // 5. O novo limite continua persistido
        assertThat(sourceFromDatabase.getDailyPixLimit())
                .isEqualByComparingTo("1000.00");

        // 6. Nenhuma transação PIX deve ter sido criada
        assertThat(transactionRepository.count())
                .isZero();
    }

    private String newIdempotencyKey() {
        return UUID.randomUUID().toString();
    }
}