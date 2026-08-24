package com.leonardo.bank_api.transaction.unitario;

import com.leonardo.bank_api.account.entity.Account;
import com.leonardo.bank_api.account.repository.AccountRepository;
import com.leonardo.bank_api.common.exception.ForbiddenOperationException;
import com.leonardo.bank_api.common.exception.ResourceNotFoundException;
import com.leonardo.bank_api.customer.entity.Customer;
import com.leonardo.bank_api.shared.enums.TransactionStatus;
import com.leonardo.bank_api.shared.enums.TransactionType;
import com.leonardo.bank_api.transaction.dto.response.TransactionReceiptResponse;
import com.leonardo.bank_api.transaction.entity.Transaction;
import com.leonardo.bank_api.transaction.mapper.TransactionMapper;
import com.leonardo.bank_api.transaction.repository.TransactionRepository;
import com.leonardo.bank_api.transaction.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnTransactionReceiptSuccessfully() {

        Long transactionId = 1L;
        String authenticatedEmail = "origem@email.com";

        Customer payer = Customer.builder()
                .id(1L)
                .fullName("Cliente Origem")
                .email(authenticatedEmail)
                .build();

        Customer receiver = Customer.builder()
                .id(2L)
                .fullName("Cliente Destino")
                .email("destino@email.com")
                .build();

        Account sourceAccount = Account.builder()
                .id(1L)
                .number("00000001")
                .agency("0001")
                .customer(payer)
                .build();

        Account destinationAccount = Account.builder()
                .id(2L)
                .number("00000002")
                .agency("0001")
                .customer(receiver)
                .build();

        LocalDateTime createdAt =
                LocalDateTime.of(
                        2026,
                        8,
                        21,
                        19,
                        0
                );

        Transaction transaction = Transaction.builder()
                .id(transactionId)
                .type(TransactionType.PIX)
                .status(TransactionStatus.COMPLETED)
                .amount(new BigDecimal("250.00"))
                .sourceAccount(sourceAccount)
                .destinationAccount(destinationAccount)
                .createdAt(createdAt)
                .build();

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                authenticatedEmail,
                                null
                        )
                );

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(transaction));

        TransactionReceiptResponse response =
                transactionService.getReceipt(transactionId);

        assertThat(response)
                .isNotNull();

        assertThat(response.transactionId())
                .isEqualTo(transactionId);

        assertThat(response.type())
                .isEqualTo(TransactionType.PIX);

        assertThat(response.status())
                .isEqualTo(TransactionStatus.COMPLETED);

        assertThat(response.amount())
                .isEqualByComparingTo("250.00");

        assertThat(response.payerName())
                .isEqualTo("Cliente Origem");

        assertThat(response.payerAgency())
                .isEqualTo("0001");

        assertThat(response.payerAccount())
                .isEqualTo("00000001");

        assertThat(response.receiverName())
                .isEqualTo("Cliente Destino");

        assertThat(response.receiverAgency())
                .isEqualTo("0001");

        assertThat(response.receiverAccount())
                .isEqualTo("00000002");

        assertThat(response.createdAt())
                .isEqualTo(createdAt);

        verify(transactionRepository)
                .findById(transactionId);
    }

    @Test
    void shouldThrowExceptionWhenTransactionDoesNotExist() {

        Long transactionId = 99L;

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                transactionService.getReceipt(transactionId)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Transação não encontrada");

        verify(transactionRepository)
                .findById(transactionId);
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotHaveAccessToTransaction() {

        Long transactionId = 1L;

        String authenticatedEmail = "intruso@email.com";

        Customer payer = Customer.builder()
                .id(1L)
                .fullName("Cliente Origem")
                .email("origem@email.com")
                .build();

        Customer receiver = Customer.builder()
                .id(2L)
                .fullName("Cliente Destino")
                .email("destino@email.com")
                .build();

        Account sourceAccount = Account.builder()
                .id(1L)
                .number("00000001")
                .agency("0001")
                .customer(payer)
                .build();

        Account destinationAccount = Account.builder()
                .id(2L)
                .number("00000002")
                .agency("0001")
                .customer(receiver)
                .build();

        Transaction transaction = Transaction.builder()
                .id(transactionId)
                .type(TransactionType.PIX)
                .status(TransactionStatus.COMPLETED)
                .amount(new BigDecimal("250.00"))
                .sourceAccount(sourceAccount)
                .destinationAccount(destinationAccount)
                .createdAt(LocalDateTime.now())
                .build();

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                authenticatedEmail,
                                null
                        )
                );

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(transaction));

        assertThatThrownBy(() ->
                transactionService.getReceipt(transactionId)
        )
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage(
                        "Você não possui permissão para acessar esta transação"
                );
    }
}