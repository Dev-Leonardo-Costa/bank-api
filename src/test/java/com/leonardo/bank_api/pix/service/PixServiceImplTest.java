package com.leonardo.bank_api.pix.service;

import com.leonardo.bank_api.account.repository.AccountRepository;
import com.leonardo.bank_api.common.exception.BusinessException;
import com.leonardo.bank_api.common.exception.ForbiddenOperationException;
import com.leonardo.bank_api.common.exception.ResourceNotFoundException;
import com.leonardo.bank_api.pix.dto.request.PixTransferRequest;
import com.leonardo.bank_api.pix.dto.request.UpdatePixLimitRequest;
import com.leonardo.bank_api.pix.dto.response.PixLimitResponse;
import com.leonardo.bank_api.pix.dto.response.PixRecipientResponse;
import com.leonardo.bank_api.pix.mapper.PixMapper;
import com.leonardo.bank_api.pix.repository.PixKeyRepository;
import com.leonardo.bank_api.pix.service.impl.PixServiceImpl;
import com.leonardo.bank_api.pix.validation.PixKeyValidator;
import com.leonardo.bank_api.shared.enums.AccountStatus;
import com.leonardo.bank_api.shared.enums.TransactionStatus;
import com.leonardo.bank_api.shared.enums.TransactionType;
import com.leonardo.bank_api.transaction.dto.response.TransactionResponse;
import com.leonardo.bank_api.transaction.entity.Transaction;
import com.leonardo.bank_api.transaction.mapper.TransactionMapper;
import com.leonardo.bank_api.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.leonardo.bank_api.account.entity.Account;
import com.leonardo.bank_api.customer.entity.Customer;
import com.leonardo.bank_api.pix.dto.request.CreatePixKeyRequest;
import com.leonardo.bank_api.pix.dto.response.PixKeyResponse;
import com.leonardo.bank_api.pix.entity.PixKey;
import com.leonardo.bank_api.shared.enums.PixKeyType;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class PixServiceImplTest {

    @Mock
    private PixKeyRepository pixKeyRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PixMapper pixMapper;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private PixKeyValidator pixKeyValidator;

    private PixServiceImpl pixService;

    @BeforeEach
    void setUp() {

        pixService = new PixServiceImpl(
                pixKeyRepository,
                accountRepository,
                pixMapper,
                List.of(pixKeyValidator),
                transactionRepository,
                transactionMapper
        );
    }


    @Test
    void shouldCreatePixKeySuccessfully() {

        String email = "cliente@email.com";
        String normalizedKey = "cliente@email.com";

        CreatePixKeyRequest request =
                new CreatePixKeyRequest(
                        PixKeyType.EMAIL,
                        email
                );

        Customer customer = Customer.builder()
                .id(1L)
                .email(email)
                .build();

        Account account = Account.builder()
                .id(1L)
                .customer(customer)
                .build();

        PixKey pixKey = PixKey.builder()
                .id(1L)
                .type(PixKeyType.EMAIL)
                .keyValue(normalizedKey)
                .account(account)
                .build();

        PixKeyResponse expectedResponse =
                new PixKeyResponse(
                        1L,
                        PixKeyType.EMAIL,
                        normalizedKey,
                        1L,
                        null
                );

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null
                        )
                );

        when(accountRepository.findByCustomerEmail(email))
                .thenReturn(Optional.of(account));

        when(pixKeyValidator.getType())
                .thenReturn(PixKeyType.EMAIL);

        when(pixKeyValidator.validateAndNormalize(email))
                .thenReturn(normalizedKey);

        when(pixKeyRepository.existsByKeyValue(normalizedKey))
                .thenReturn(false);

        when(pixKeyRepository.save(any(PixKey.class)))
                .thenReturn(pixKey);

        when(pixMapper.toResponse(pixKey))
                .thenReturn(expectedResponse);

        PixKeyResponse result =
                pixService.createPixKey(request);

        assertThat(result)
                .isEqualTo(expectedResponse);

        verify(pixKeyRepository)
                .save(any(PixKey.class));

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldThrowExceptionWhenPixKeyAlreadyExists() {

        String email = "cliente@email.com";

        CreatePixKeyRequest request =
                new CreatePixKeyRequest(
                        PixKeyType.EMAIL,
                        email
                );

        Customer customer = Customer.builder()
                .id(1L)
                .email(email)
                .build();

        Account account = Account.builder()
                .id(1L)
                .customer(customer)
                .build();

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null
                        )
                );

        when(accountRepository.findByCustomerEmail(email))
                .thenReturn(Optional.of(account));

        when(pixKeyValidator.getType())
                .thenReturn(PixKeyType.EMAIL);

        when(pixKeyValidator.validateAndNormalize(email))
                .thenReturn(email);

        // Simula que a chave já existe
        when(pixKeyRepository.existsByKeyValue(email))
                .thenReturn(true);

        assertThatThrownBy(() ->
                pixService.createPixKey(request)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessage("Chave PIX já cadastrada");

        verify(pixKeyRepository, never())
                .save(any(PixKey.class));
    }

    @Test
    void shouldThrowExceptionWhenAccountIsNotFound() {

        String email = "cliente@email.com";

        CreatePixKeyRequest request =
                new CreatePixKeyRequest(
                        PixKeyType.EMAIL,
                        email
                );

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null
                        )
                );

        when(accountRepository.findByCustomerEmail(email))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                pixService.createPixKey(request)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Conta não encontrada");

        verify(pixKeyRepository, never())
                .save(any(PixKey.class));
    }

    @Test
    void shouldThrowExceptionWhenPixEmailDoesNotBelongToCustomer() {

        String customerEmail = "cliente@email.com";
        String differentEmail = "outro@email.com";

        CreatePixKeyRequest request =
                new CreatePixKeyRequest(
                        PixKeyType.EMAIL,
                        differentEmail
                );

        Customer customer = Customer.builder()
                .id(1L)
                .email(customerEmail)
                .build();

        Account account = Account.builder()
                .id(1L)
                .customer(customer)
                .build();

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                customerEmail,
                                null
                        )
                );

        when(accountRepository.findByCustomerEmail(customerEmail))
                .thenReturn(Optional.of(account));

        when(pixKeyValidator.getType())
                .thenReturn(PixKeyType.EMAIL);

        when(pixKeyValidator.validateAndNormalize(differentEmail))
                .thenReturn(differentEmail);

        assertThatThrownBy(() ->
                pixService.createPixKey(request)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessage(
                        "A chave PIX do tipo EMAIL deve ser o e-mail cadastrado do cliente"
                );

        verify(pixKeyRepository, never())
                .save(any(PixKey.class));
    }

    @Test
    void shouldThrowExceptionWhenPixCpfDoesNotBelongToCustomer() {

        String email = "cliente@email.com";
        String customerCpf = "52998224725";
        String differentCpf = "11144477735";

        CreatePixKeyRequest request =
                new CreatePixKeyRequest(
                        PixKeyType.CPF,
                        differentCpf
                );

        Customer customer = Customer.builder()
                .id(1L)
                .email(email)
                .cpf(customerCpf)
                .build();

        Account account = Account.builder()
                .id(1L)
                .customer(customer)
                .build();

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null
                        )
                );

        when(accountRepository.findByCustomerEmail(email))
                .thenReturn(Optional.of(account));

        when(pixKeyValidator.getType())
                .thenReturn(PixKeyType.CPF);

        when(pixKeyValidator.validateAndNormalize(differentCpf))
                .thenReturn(differentCpf);

        assertThatThrownBy(() ->
                pixService.createPixKey(request)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessage(
                        "A chave PIX do tipo CPF deve ser o CPF cadastrado do cliente"
                );

        verify(pixKeyRepository, never())
                .save(any(PixKey.class));
    }

    @Test
    void shouldTransferPixSuccessfully() {

        Long sourceAccountId = 1L;
        Long destinationAccountId = 2L;

        String senderEmail = "sender@email.com";
        String pixKeyValue = "receiver@email.com";

        BigDecimal amount = new BigDecimal("100.00");

        PixTransferRequest request =
                new PixTransferRequest(
                        pixKeyValue,
                        amount
                );

        Customer senderCustomer = Customer.builder()
                .id(1L)
                .email(senderEmail)
                .build();

        Account sourceAccount = Account.builder()
                .id(sourceAccountId)
                .balance(new BigDecimal("500.00"))
                .dailyPixLimit(new BigDecimal("5000.00"))
                .status(AccountStatus.ACTIVE)
                .customer(senderCustomer)
                .build();

        Account destinationAccount = Account.builder()
                .id(destinationAccountId)
                .balance(new BigDecimal("200.00"))
                .status(AccountStatus.ACTIVE)
                .build();

        PixKey pixKey = PixKey.builder()
                .id(1L)
                .keyValue(pixKeyValue)
                .account(destinationAccount)
                .build();

        Transaction savedTransaction = Transaction.builder()
                .id(10L)
                .type(TransactionType.PIX)
                .status(TransactionStatus.COMPLETED)
                .amount(amount)
                .sourceAccount(sourceAccount)
                .destinationAccount(destinationAccount)
                .build();

        TransactionResponse expectedResponse =
                new TransactionResponse(
                        10L,
                        TransactionType.PIX,
                        TransactionStatus.COMPLETED,
                        amount,
                        sourceAccountId,
                        destinationAccountId,
                        null,
                        null,
                        null
                );

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                senderEmail,
                                null
                        )
                );

        when(pixKeyRepository.findByKeyValue(pixKeyValue))
                .thenReturn(Optional.of(pixKey));

        when(accountRepository.findByIdForUpdate(sourceAccountId))
                .thenReturn(Optional.of(sourceAccount));

        when(accountRepository.findByIdForUpdate(destinationAccountId))
                .thenReturn(Optional.of(destinationAccount));

        when(transactionRepository.sumAmountByAccountAndPeriod(
                eq(sourceAccountId),
                eq(TransactionType.PIX),
                eq(TransactionStatus.COMPLETED),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(BigDecimal.ZERO);

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(savedTransaction);

        when(transactionMapper.toResponse(savedTransaction))
                .thenReturn(expectedResponse);

        TransactionResponse result =
                pixService.transfer(
                        sourceAccountId,
                        request
                );

        assertThat(result)
                .isEqualTo(expectedResponse);

        assertThat(sourceAccount.getBalance())
                .isEqualByComparingTo("400.00");

        assertThat(destinationAccount.getBalance())
                .isEqualByComparingTo("300.00");

        verify(transactionRepository)
                .sumAmountByAccountAndPeriod(
                        eq(sourceAccountId),
                        eq(TransactionType.PIX),
                        eq(TransactionStatus.COMPLETED),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                );

        verify(transactionRepository)
                .save(any(Transaction.class));
    }

    @Test
    void shouldThrowExceptionWhenPixBalanceIsInsufficient() {

        Long sourceAccountId = 1L;
        Long destinationAccountId = 2L;

        String senderEmail = "sender@email.com";
        String pixKeyValue = "receiver@email.com";

        BigDecimal amount = new BigDecimal("600.00");

        PixTransferRequest request =
                new PixTransferRequest(
                        pixKeyValue,
                        amount
                );

        Customer senderCustomer = Customer.builder()
                .id(1L)
                .email(senderEmail)
                .build();

        Account sourceAccount = Account.builder()
                .id(sourceAccountId)
                .balance(new BigDecimal("500.00"))
                .status(AccountStatus.ACTIVE)
                .customer(senderCustomer)
                .build();

        Account destinationAccount = Account.builder()
                .id(destinationAccountId)
                .balance(new BigDecimal("200.00"))
                .status(AccountStatus.ACTIVE)
                .build();

        PixKey pixKey = PixKey.builder()
                .id(1L)
                .keyValue(pixKeyValue)
                .account(destinationAccount)
                .build();

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                senderEmail,
                                null
                        )
                );

        when(pixKeyRepository.findByKeyValue(pixKeyValue))
                .thenReturn(Optional.of(pixKey));

        when(accountRepository.findByIdForUpdate(sourceAccountId))
                .thenReturn(Optional.of(sourceAccount));

        when(accountRepository.findByIdForUpdate(destinationAccountId))
                .thenReturn(Optional.of(destinationAccount));

        assertThatThrownBy(() ->
                pixService.transfer(
                        sourceAccountId,
                        request
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasMessage("Saldo insuficiente");

        assertThat(sourceAccount.getBalance())
                .isEqualByComparingTo("500.00");

        assertThat(destinationAccount.getBalance())
                .isEqualByComparingTo("200.00");

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void shouldThrowExceptionWhenSourceAccountDoesNotBelongToAuthenticatedUser() {

        Long sourceAccountId = 1L;
        Long destinationAccountId = 2L;

        String authenticatedEmail = "cliente1@email.com";
        String ownerEmail = "outrocliente@email.com";
        String pixKeyValue = "destino@email.com";

        BigDecimal amount = new BigDecimal("100.00");

        PixTransferRequest request =
                new PixTransferRequest(
                        pixKeyValue,
                        amount
                );

        Customer sourceCustomer = Customer.builder()
                .id(1L)
                .email(ownerEmail)
                .build();

        Account sourceAccount = Account.builder()
                .id(sourceAccountId)
                .balance(new BigDecimal("500.00"))
                .status(AccountStatus.ACTIVE)
                .customer(sourceCustomer)
                .build();

        Account destinationAccount = Account.builder()
                .id(destinationAccountId)
                .balance(new BigDecimal("200.00"))
                .status(AccountStatus.ACTIVE)
                .build();

        PixKey pixKey = PixKey.builder()
                .id(1L)
                .keyValue(pixKeyValue)
                .account(destinationAccount)
                .build();

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                authenticatedEmail,
                                null
                        )
                );

        when(pixKeyRepository.findByKeyValue(pixKeyValue))
                .thenReturn(Optional.of(pixKey));

        when(accountRepository.findByIdForUpdate(sourceAccountId))
                .thenReturn(Optional.of(sourceAccount));

        when(accountRepository.findByIdForUpdate(destinationAccountId))
                .thenReturn(Optional.of(destinationAccount));

        assertThatThrownBy(() ->
                pixService.transfer(
                        sourceAccountId,
                        request
                )
        )
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage(
                        "Você não possui permissão para movimentar esta conta"
                );

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void shouldThrowExceptionWhenSourceAccountIsInactive() {

        Long sourceAccountId = 1L;
        Long destinationAccountId = 2L;

        String senderEmail = "sender@email.com";
        String pixKeyValue = "receiver@email.com";

        BigDecimal amount = new BigDecimal("100.00");

        PixTransferRequest request =
                new PixTransferRequest(
                        pixKeyValue,
                        amount
                );

        Customer senderCustomer = Customer.builder()
                .id(1L)
                .email(senderEmail)
                .build();

        Account sourceAccount = Account.builder()
                .id(sourceAccountId)
                .balance(new BigDecimal("500.00"))
                .status(AccountStatus.BLOCKED)
                .customer(senderCustomer)
                .build();

        Account destinationAccount = Account.builder()
                .id(destinationAccountId)
                .balance(new BigDecimal("200.00"))
                .status(AccountStatus.ACTIVE)
                .build();

        PixKey pixKey = PixKey.builder()
                .id(1L)
                .keyValue(pixKeyValue)
                .account(destinationAccount)
                .build();

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                senderEmail,
                                null
                        )
                );

        when(pixKeyRepository.findByKeyValue(pixKeyValue))
                .thenReturn(Optional.of(pixKey));

        when(accountRepository.findByIdForUpdate(sourceAccountId))
                .thenReturn(Optional.of(sourceAccount));

        when(accountRepository.findByIdForUpdate(destinationAccountId))
                .thenReturn(Optional.of(destinationAccount));

        assertThatThrownBy(() ->
                pixService.transfer(
                        sourceAccountId,
                        request
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasMessage("A conta de origem não está ativa");

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void shouldThrowExceptionWhenDestinationAccountIsInactive() {

        Long sourceAccountId = 1L;
        Long destinationAccountId = 2L;

        String senderEmail = "sender@email.com";
        String pixKeyValue = "receiver@email.com";

        BigDecimal amount = new BigDecimal("100.00");

        PixTransferRequest request =
                new PixTransferRequest(
                        pixKeyValue,
                        amount
                );

        Customer senderCustomer = Customer.builder()
                .id(1L)
                .email(senderEmail)
                .build();

        Account sourceAccount = Account.builder()
                .id(sourceAccountId)
                .balance(new BigDecimal("500.00"))
                .status(AccountStatus.ACTIVE)
                .customer(senderCustomer)
                .build();

        Account destinationAccount = Account.builder()
                .id(destinationAccountId)
                .balance(new BigDecimal("200.00"))
                .status(AccountStatus.BLOCKED)
                .build();

        PixKey pixKey = PixKey.builder()
                .id(1L)
                .keyValue(pixKeyValue)
                .account(destinationAccount)
                .build();

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                senderEmail,
                                null
                        )
                );

        when(pixKeyRepository.findByKeyValue(pixKeyValue))
                .thenReturn(Optional.of(pixKey));

        when(accountRepository.findByIdForUpdate(sourceAccountId))
                .thenReturn(Optional.of(sourceAccount));

        when(accountRepository.findByIdForUpdate(destinationAccountId))
                .thenReturn(Optional.of(destinationAccount));

        assertThatThrownBy(() ->
                pixService.transfer(
                        sourceAccountId,
                        request
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasMessage("A conta de destino não está ativa");

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void shouldThrowExceptionWhenPixKeyIsNotFound() {

        Long sourceAccountId = 1L;

        String pixKeyValue = "naoexiste@email.com";

        PixTransferRequest request =
                new PixTransferRequest(
                        pixKeyValue,
                        new BigDecimal("100.00")
                );

        when(pixKeyRepository.findByKeyValue(pixKeyValue))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                pixService.transfer(
                        sourceAccountId,
                        request
                )
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Chave PIX não encontrada");

        verify(accountRepository, never())
                .findByIdForUpdate(anyLong());

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void shouldMaskRandomKeyWhenFindingPixRecipient() {

        String randomKey =
                "550e8400-e29b-41d4-a716-446655440000";

        Customer customer = Customer.builder()
                .fullName("Cliente Teste")
                .build();

        Account account = Account.builder()
                .id(2L)
                .customer(customer)
                .build();

        PixKey pixKey = PixKey.builder()
                .type(PixKeyType.RANDOM)
                .keyValue(randomKey)
                .account(account)
                .build();

        when(pixKeyRepository.findByKeyValue(randomKey))
                .thenReturn(Optional.of(pixKey));

        PixRecipientResponse response =
                pixService.findRecipientByKey(randomKey);

        assertThat(response.keyValue())
                .isEqualTo("550e8400****");

        assertThat(response.keyType())
                .isEqualTo(PixKeyType.RANDOM);
    }

    @Test
    void shouldMaskPhoneWhenFindingPixRecipient() {

        String phone = "+5585999999999";

        Customer customer = Customer.builder()
                .fullName("Cliente Teste")
                .build();

        Account account = Account.builder()
                .id(2L)
                .customer(customer)
                .build();

        PixKey pixKey = PixKey.builder()
                .type(PixKeyType.PHONE)
                .keyValue(phone)
                .account(account)
                .build();

        when(pixKeyRepository.findByKeyValue(phone))
                .thenReturn(Optional.of(pixKey));

        PixRecipientResponse response =
                pixService.findRecipientByKey(phone);

        assertThat(response.keyValue())
                .isEqualTo("+55 (***) *****-9999");

        assertThat(response.keyType())
                .isEqualTo(PixKeyType.PHONE);
    }

    @Test
    void shouldMaskEmailWhenFindingPixRecipient() {

        String email = "cliente@email.com";

        Customer customer = Customer.builder()
                .fullName("Cliente Teste")
                .build();

        Account account = Account.builder()
                .id(2L)
                .customer(customer)
                .build();

        PixKey pixKey = PixKey.builder()
                .type(PixKeyType.EMAIL)
                .keyValue(email)
                .account(account)
                .build();

        when(pixKeyRepository.findByKeyValue(email))
                .thenReturn(Optional.of(pixKey));

        PixRecipientResponse response =
                pixService.findRecipientByKey(email);

        assertThat(response.keyValue())
                .isEqualTo("c***@email.com");

        assertThat(response.keyType())
                .isEqualTo(PixKeyType.EMAIL);
    }

    @Test
    void shouldMaskCpfWhenFindingPixRecipient() {

        String cpf = "52998224725";

        Customer customer = Customer.builder()
                .fullName("Cliente Teste")
                .build();

        Account account = Account.builder()
                .id(2L)
                .customer(customer)
                .build();

        PixKey pixKey = PixKey.builder()
                .type(PixKeyType.CPF)
                .keyValue(cpf)
                .account(account)
                .build();

        when(pixKeyRepository.findByKeyValue(cpf))
                .thenReturn(Optional.of(pixKey));

        PixRecipientResponse response =
                pixService.findRecipientByKey(cpf);

        assertThat(response.keyValue())
                .isEqualTo("***.***.***-25");

        assertThat(response.keyType())
                .isEqualTo(PixKeyType.CPF);
    }

    @Test
    void shouldReturnAuthenticatedUserPixKeys() {

        String email = "cliente@email.com";

        Customer customer = Customer.builder()
                .id(1L)
                .email(email)
                .build();

        Account account = Account.builder()
                .id(1L)
                .customer(customer)
                .build();

        PixKey firstPixKey = PixKey.builder()
                .id(1L)
                .type(PixKeyType.EMAIL)
                .keyValue(email)
                .account(account)
                .build();

        PixKey secondPixKey = PixKey.builder()
                .id(2L)
                .type(PixKeyType.RANDOM)
                .keyValue("550e8400-e29b-41d4-a716-446655440000")
                .account(account)
                .build();

        PixKeyResponse firstResponse =
                new PixKeyResponse(
                        1L,
                        PixKeyType.EMAIL,
                        email,
                        1L,
                        null
                );

        PixKeyResponse secondResponse =
                new PixKeyResponse(
                        2L,
                        PixKeyType.RANDOM,
                        "550e8400-e29b-41d4-a716-446655440000",
                        1L,
                        null
                );

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null
                        )
                );

        when(pixKeyRepository.findAllByAccountCustomerEmail(email))
                .thenReturn(List.of(
                        firstPixKey,
                        secondPixKey
                ));

        when(pixMapper.toResponse(firstPixKey))
                .thenReturn(firstResponse);

        when(pixMapper.toResponse(secondPixKey))
                .thenReturn(secondResponse);

        List<PixKeyResponse> result =
                pixService.findMyPixKeys();

        assertThat(result)
                .hasSize(2)
                .containsExactly(
                        firstResponse,
                        secondResponse
                );

        verify(pixKeyRepository)
                .findAllByAccountCustomerEmail(email);

        verify(pixMapper)
                .toResponse(firstPixKey);

        verify(pixMapper)
                .toResponse(secondPixKey);
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoPixKeys() {

        String email = "cliente@email.com";

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null
                        )
                );

        when(pixKeyRepository.findAllByAccountCustomerEmail(email))
                .thenReturn(List.of());

        List<PixKeyResponse> result =
                pixService.findMyPixKeys();

        assertThat(result)
                .isEmpty();

        verify(pixKeyRepository)
                .findAllByAccountCustomerEmail(email);

        verifyNoInteractions(pixMapper);
    }

    @Test
    void shouldDeletePixKeySuccessfully() {

        Long pixKeyId = 1L;
        String email = "cliente@email.com";

        Customer customer = Customer.builder()
                .id(1L)
                .email(email)
                .build();

        Account account = Account.builder()
                .id(1L)
                .customer(customer)
                .build();

        PixKey pixKey = PixKey.builder()
                .id(pixKeyId)
                .type(PixKeyType.EMAIL)
                .keyValue(email)
                .account(account)
                .build();

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null
                        )
                );

        when(pixKeyRepository.findById(pixKeyId))
                .thenReturn(Optional.of(pixKey));

        pixService.deletePixKey(pixKeyId);

        verify(pixKeyRepository)
                .findById(pixKeyId);

        verify(pixKeyRepository)
                .delete(pixKey);
    }

    @Test
    void shouldThrowExceptionWhenDeletingPixKeyNotFound() {

        Long pixKeyId = 99L;

        String email = "cliente@email.com";

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null
                        )
                );

        when(pixKeyRepository.findById(pixKeyId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                pixService.deletePixKey(pixKeyId)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Chave PIX não encontrada");

        verify(pixKeyRepository, never())
                .delete(any(PixKey.class));
    }

    @Test
    void shouldThrowExceptionWhenDeletingPixKeyFromAnotherCustomer() {

        Long pixKeyId = 1L;

        String authenticatedEmail = "cliente1@email.com";
        String ownerEmail = "cliente2@email.com";

        Customer owner = Customer.builder()
                .id(2L)
                .email(ownerEmail)
                .build();

        Account account = Account.builder()
                .id(2L)
                .customer(owner)
                .build();

        PixKey pixKey = PixKey.builder()
                .id(pixKeyId)
                .type(PixKeyType.EMAIL)
                .keyValue(ownerEmail)
                .account(account)
                .build();

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                authenticatedEmail,
                                null
                        )
                );

        when(pixKeyRepository.findById(pixKeyId))
                .thenReturn(Optional.of(pixKey));

        assertThatThrownBy(() ->
                pixService.deletePixKey(pixKeyId)
        )
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage(
                        "Você não possui permissão para excluir esta chave PIX"
                );

        verify(pixKeyRepository, never())
                .delete(any(PixKey.class));
    }

    @Test
    void shouldThrowExceptionWhenDailyPixLimitIsExceeded() {

        Long sourceAccountId = 1L;
        Long destinationAccountId = 2L;

        String senderEmail = "sender@email.com";
        String pixKeyValue = "receiver@email.com";

        BigDecimal amount = new BigDecimal("300.00");

        PixTransferRequest request =
                new PixTransferRequest(
                        pixKeyValue,
                        amount
                );

        Customer senderCustomer = Customer.builder()
                .id(1L)
                .email(senderEmail)
                .build();

        Account sourceAccount = Account.builder()
                .id(sourceAccountId)
                .balance(new BigDecimal("1000.00"))
                .dailyPixLimit(new BigDecimal("5000.00"))
                .status(AccountStatus.ACTIVE)
                .customer(senderCustomer)
                .build();

        Account destinationAccount = Account.builder()
                .id(destinationAccountId)
                .balance(new BigDecimal("200.00"))
                .status(AccountStatus.ACTIVE)
                .build();

        PixKey pixKey = PixKey.builder()
                .id(1L)
                .keyValue(pixKeyValue)
                .account(destinationAccount)
                .build();

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                senderEmail,
                                null
                        )
                );

        when(pixKeyRepository.findByKeyValue(pixKeyValue))
                .thenReturn(Optional.of(pixKey));

        when(accountRepository.findByIdForUpdate(sourceAccountId))
                .thenReturn(Optional.of(sourceAccount));

        when(accountRepository.findByIdForUpdate(destinationAccountId))
                .thenReturn(Optional.of(destinationAccount));

        // Cliente já enviou R$ 4.800 hoje
        when(transactionRepository.sumAmountByAccountAndPeriod(
                eq(sourceAccountId),
                eq(TransactionType.PIX),
                eq(TransactionStatus.COMPLETED),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(new BigDecimal("4800.00"));

        assertThatThrownBy(() ->
                pixService.transfer(
                        sourceAccountId,
                        request
                )
        )
                .isInstanceOf(BusinessException.class)
                .hasMessage("Limite diário de PIX excedido");

        assertThat(sourceAccount.getBalance())
                .isEqualByComparingTo("1000.00");

        assertThat(destinationAccount.getBalance())
                .isEqualByComparingTo("200.00");

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void shouldAllowPixWhenDailyLimitIsExactlyReached() {

        Long sourceAccountId = 1L;
        Long destinationAccountId = 2L;

        String senderEmail = "sender@email.com";
        String pixKeyValue = "receiver@email.com";

        BigDecimal amount = new BigDecimal("200.00");

        PixTransferRequest request =
                new PixTransferRequest(
                        pixKeyValue,
                        amount
                );

        Customer senderCustomer = Customer.builder()
                .id(1L)
                .email(senderEmail)
                .build();

        Account sourceAccount = Account.builder()
                .id(sourceAccountId)
                .balance(new BigDecimal("1000.00"))
                .dailyPixLimit(new BigDecimal("5000.00"))
                .status(AccountStatus.ACTIVE)
                .customer(senderCustomer)
                .build();

        Account destinationAccount = Account.builder()
                .id(destinationAccountId)
                .balance(new BigDecimal("200.00"))
                .status(AccountStatus.ACTIVE)
                .build();

        PixKey pixKey = PixKey.builder()
                .id(1L)
                .keyValue(pixKeyValue)
                .account(destinationAccount)
                .build();

        Transaction savedTransaction = Transaction.builder()
                .id(10L)
                .type(TransactionType.PIX)
                .status(TransactionStatus.COMPLETED)
                .amount(amount)
                .sourceAccount(sourceAccount)
                .destinationAccount(destinationAccount)
                .build();

        TransactionResponse expectedResponse =
                new TransactionResponse(
                        10L,
                        TransactionType.PIX,
                        TransactionStatus.COMPLETED,
                        amount,
                        sourceAccountId,
                        destinationAccountId,
                        null,
                        null,
                        null
                );

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                senderEmail,
                                null
                        )
                );

        when(pixKeyRepository.findByKeyValue(pixKeyValue))
                .thenReturn(Optional.of(pixKey));

        when(accountRepository.findByIdForUpdate(sourceAccountId))
                .thenReturn(Optional.of(sourceAccount));

        when(accountRepository.findByIdForUpdate(destinationAccountId))
                .thenReturn(Optional.of(destinationAccount));

        // Já enviou R$ 4.800 no dia
        when(transactionRepository.sumAmountByAccountAndPeriod(
                eq(sourceAccountId),
                eq(TransactionType.PIX),
                eq(TransactionStatus.COMPLETED),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(new BigDecimal("4800.00"));

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(savedTransaction);

        when(transactionMapper.toResponse(savedTransaction))
                .thenReturn(expectedResponse);

        TransactionResponse result =
                pixService.transfer(
                        sourceAccountId,
                        request
                );

        assertThat(result)
                .isEqualTo(expectedResponse);

        assertThat(sourceAccount.getBalance())
                .isEqualByComparingTo("800.00");

        assertThat(destinationAccount.getBalance())
                .isEqualByComparingTo("400.00");

        verify(transactionRepository)
                .save(any(Transaction.class));
    }

    @Test
    void shouldUpdateDailyPixLimitSuccessfully() {

        Long accountId = 1L;
        String email = "cliente@email.com";

        Customer customer = Customer.builder()
                .id(1L)
                .email(email)
                .build();

        Account account = Account.builder()
                .id(accountId)
                .dailyPixLimit(new BigDecimal("5000.00"))
                .customer(customer)
                .build();

        UpdatePixLimitRequest request =
                new UpdatePixLimitRequest(
                        new BigDecimal("8000.00")
                );

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null
                        )
                );

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        when(accountRepository.save(account))
                .thenReturn(account);

        PixLimitResponse response =
                pixService.updateDailyPixLimit(
                        accountId,
                        request
                );

        assertThat(response.dailyPixLimit())
                .isEqualByComparingTo("8000.00");

        assertThat(account.getDailyPixLimit())
                .isEqualByComparingTo("8000.00");

        verify(accountRepository)
                .save(account);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingPixLimitFromAnotherCustomer() {

        Long accountId = 1L;

        String authenticatedEmail = "cliente1@email.com";
        String ownerEmail = "cliente2@email.com";

        Customer owner = Customer.builder()
                .id(2L)
                .email(ownerEmail)
                .build();

        Account account = Account.builder()
                .id(accountId)
                .dailyPixLimit(new BigDecimal("5000.00"))
                .customer(owner)
                .build();

        UpdatePixLimitRequest request =
                new UpdatePixLimitRequest(
                        new BigDecimal("8000.00")
                );

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                authenticatedEmail,
                                null
                        )
                );

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        assertThatThrownBy(() ->
                pixService.updateDailyPixLimit(
                        accountId,
                        request
                )
        )
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage(
                        "Você não possui permissão para movimentar esta conta"
                );

        verify(accountRepository, never())
                .save(any(Account.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingPixLimitAndAccountDoesNotExist() {

        Long accountId = 99L;
        String email = "cliente@email.com";

        UpdatePixLimitRequest request =
                new UpdatePixLimitRequest(
                        new BigDecimal("8000.00")
                );

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null
                        )
                );

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                pixService.updateDailyPixLimit(
                        accountId,
                        request
                )
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Conta não encontrada");

        verify(accountRepository, never())
                .save(any(Account.class));
    }

    @Test
    void shouldReturnDailyPixLimitSuccessfully() {

        Long accountId = 1L;
        String email = "cliente@email.com";

        Customer customer = Customer.builder()
                .id(1L)
                .email(email)
                .build();

        Account account = Account.builder()
                .id(accountId)
                .dailyPixLimit(new BigDecimal("5000.00"))
                .customer(customer)
                .build();

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null
                        )
                );

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        PixLimitResponse response =
                pixService.getDailyPixLimit(accountId);

        assertThat(response)
                .isNotNull();

        assertThat(response.dailyPixLimit())
                .isEqualByComparingTo("5000.00");

        verify(accountRepository)
                .findById(accountId);
    }

    @Test
    void shouldThrowExceptionWhenGettingPixLimitAndAccountDoesNotExist() {

        Long accountId = 99L;
        String email = "cliente@email.com";

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null
                        )
                );

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                pixService.getDailyPixLimit(accountId)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Conta não encontrada");
    }
}