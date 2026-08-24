package com.leonardo.bank_api.transaction.integration;

import com.leonardo.bank_api.account.entity.Account;
import com.leonardo.bank_api.account.repository.AccountRepository;
import com.leonardo.bank_api.customer.entity.Customer;
import com.leonardo.bank_api.customer.repository.CustomerRepository;
import com.leonardo.bank_api.shared.enums.AccountStatus;
import com.leonardo.bank_api.shared.enums.TransactionStatus;
import com.leonardo.bank_api.shared.enums.TransactionType;
import com.leonardo.bank_api.transaction.service.TransactionService;
import com.leonardo.bank_api.transaction.dto.response.TransactionReceiptResponse;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.within;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class TransactionIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void cleanDatabase() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnTransactionReceiptFromDatabase() {

        String senderEmail = "receipt.origem@email.com";
        String receiverEmail = "receipt.destino@email.com";

        // Cria o cliente de origem
        Customer sender = customerRepository.save(
                Customer.builder()
                        .fullName("Cliente Origem")
                        .cpf("52998224725")
                        .email(senderEmail)
                        .build()
        );

        // Cria o cliente de destino
        Customer receiver = customerRepository.save(
                Customer.builder()
                        .fullName("Cliente Destino")
                        .cpf("11144477735")
                        .email(receiverEmail)
                        .build()
        );

        // Cria a conta de origem
        Account sourceAccount = accountRepository.save(
                Account.builder()
                        .number("00000020")
                        .agency("0001")
                        .balance(new BigDecimal("1000.00"))
                        .dailyPixLimit(new BigDecimal("5000.00"))
                        .status(AccountStatus.ACTIVE)
                        .customer(sender)
                        .build()
        );

        // Cria a conta de destino
        Account destinationAccount = accountRepository.save(
                Account.builder()
                        .number("00000021")
                        .agency("0001")
                        .balance(new BigDecimal("500.00"))
                        .dailyPixLimit(new BigDecimal("5000.00"))
                        .status(AccountStatus.ACTIVE)
                        .customer(receiver)
                        .build()
        );

        LocalDateTime createdAt = LocalDateTime.now();

        // Persiste uma transação PIX real no PostgreSQL
        Transaction transaction = transactionRepository.save(
                Transaction.builder()
                        .type(TransactionType.PIX)
                        .status(TransactionStatus.COMPLETED)
                        .amount(new BigDecimal("250.00"))
                        .sourceAccount(sourceAccount)
                        .destinationAccount(destinationAccount)
                        .createdAt(createdAt)
                        .build()
        );

        // Simula o cliente de origem autenticado
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                senderEmail,
                                null
                        )
                );

        // Busca o comprovante através do service real
        TransactionReceiptResponse response =
                transactionService.getReceipt(
                        transaction.getId()
                );

        // Validações
        assertThat(response)
                .isNotNull();

        assertThat(response.transactionId())
                .isEqualTo(transaction.getId());

        assertThat(response.type())
                .isEqualTo(TransactionType.PIX);

        assertThat(response.status())
                .isEqualTo(TransactionStatus.COMPLETED);

        assertThat(response.amount())
                .isEqualByComparingTo("250.00");

        // Pagador
        assertThat(response.payerName())
                .isEqualTo("Cliente Origem");

        assertThat(response.payerAgency())
                .isEqualTo("0001");

        assertThat(response.payerAccount())
                .isEqualTo("00000020");

        // Recebedor
        assertThat(response.receiverName())
                .isEqualTo("Cliente Destino");

        assertThat(response.receiverAgency())
                .isEqualTo("0001");

        assertThat(response.receiverAccount())
                .isEqualTo("00000021");

        // PostgreSQL pode arredondar a precisão do timestamp.
        // Por isso aceitamos diferença máxima de 2 microssegundos.
        assertThat(response.createdAt())
                .isCloseTo(
                        createdAt,
                        within(2, ChronoUnit.MICROS)
                );
    }
}