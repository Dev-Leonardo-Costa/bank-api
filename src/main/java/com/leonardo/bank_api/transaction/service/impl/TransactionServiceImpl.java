package com.leonardo.bank_api.transaction.service.impl;

import com.leonardo.bank_api.account.entity.Account;
import com.leonardo.bank_api.account.repository.AccountRepository;
import com.leonardo.bank_api.common.exception.BusinessException;
import com.leonardo.bank_api.common.exception.ForbiddenOperationException;
import com.leonardo.bank_api.common.exception.ResourceNotFoundException;
import com.leonardo.bank_api.shared.enums.*;
import com.leonardo.bank_api.transaction.dto.request.DepositRequest;
import com.leonardo.bank_api.transaction.dto.request.TransferRequest;
import com.leonardo.bank_api.transaction.dto.request.WithdrawRequest;
import com.leonardo.bank_api.transaction.dto.response.StatementTransactionResponse;
import com.leonardo.bank_api.transaction.dto.response.TransactionResponse;
import com.leonardo.bank_api.transaction.entity.Transaction;
import com.leonardo.bank_api.transaction.mapper.TransactionMapper;
import com.leonardo.bank_api.transaction.repository.TransactionRepository;
import com.leonardo.bank_api.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @Transactional
    @Override
    public TransactionResponse deposit(Long accountId, DepositRequest request) {

        Account account = getOwnedAccount(accountId);

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException(
                    "Não é possível depositar em uma conta inativa"
            );
        }

        account.setBalance(
                account.getBalance().add(request.amount())
        );

        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.COMPLETED)
                .amount(request.amount())
                .destinationAccount(account)
                .build();

        Transaction savedTransaction =
                transactionRepository.save(transaction);

        return transactionMapper.toResponse(savedTransaction);
    }

    @Transactional
    @Override
    public TransactionResponse transfer(
            Long sourceAccountId,
            TransferRequest request
    ) {

        Long destinationAccountId = request.destinationAccountId();

        if (sourceAccountId.equals(destinationAccountId)) {
            throw new BusinessException(
                    "A conta de origem e destino não podem ser iguais"
            );
        }

        Long firstId = Math.min(sourceAccountId, destinationAccountId);
        Long secondId = Math.max(sourceAccountId, destinationAccountId);

        Account firstAccount = accountRepository.findByIdForUpdate(firstId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Conta não encontrada")
                );

        Account secondAccount = accountRepository.findByIdForUpdate(secondId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Conta não encontrada")
                );

        Account sourceAccount =
                firstAccount.getId().equals(sourceAccountId)
                        ? firstAccount
                        : secondAccount;

        Account destinationAccount =
                firstAccount.getId().equals(destinationAccountId)
                        ? firstAccount
                        : secondAccount;

        validateOwnership(sourceAccount);

        if (sourceAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException(
                    "A conta de origem não está ativa"
            );
        }

        if (destinationAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException(
                    "A conta de destino não está ativa"
            );
        }

        if (sourceAccount.getBalance().compareTo(request.amount()) < 0) {
            throw new BusinessException("Saldo insuficiente");
        }

        sourceAccount.setBalance(
                sourceAccount.getBalance().subtract(request.amount())
        );

        destinationAccount.setBalance(
                destinationAccount.getBalance().add(request.amount())
        );

        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        Transaction transaction = Transaction.builder()
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.COMPLETED)
                .amount(request.amount())
                .sourceAccount(sourceAccount)
                .destinationAccount(destinationAccount)
                .build();

        Transaction savedTransaction =
                transactionRepository.save(transaction);

        return transactionMapper.toResponse(savedTransaction);
    }

    @Transactional
    @Override
    public TransactionResponse withdraw(Long accountId, WithdrawRequest request) {
        Account account = getOwnedAccount(accountId);

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException(
                    "Não é possível realizar saque em uma conta inativa"
            );
        }

        if (account.getBalance().compareTo(request.amount()) < 0) {
            throw new BusinessException(
                    "Saldo insuficiente"
            );
        }

        account.setBalance(
                account.getBalance().subtract(request.amount())
        );

        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .type(TransactionType.WITHDRAW)
                .status(TransactionStatus.COMPLETED)
                .amount(request.amount())
                .sourceAccount(account)
                .build();

        Transaction savedTransaction =
                transactionRepository.save(transaction);

        return transactionMapper.toResponse(savedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getStatement(
            Long accountId,
            TransactionType type,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {

        getOwnedAccount(accountId);

        Specification<Transaction> spec =
                (root, query, cb) -> cb.or(
                        cb.equal(
                                root.get("sourceAccount").get("id"),
                                accountId
                        ),
                        cb.equal(
                                root.get("destinationAccount").get("id"),
                                accountId
                        )
                );

        if (type != null) {
            spec = spec.and(
                    (root, query, cb) ->
                            cb.equal(root.get("type"), type)
            );
        }

        if (startDate != null) {
            LocalDateTime startDateTime = startDate.atStartOfDay();

            spec = spec.and(
                    (root, query, cb) ->
                            cb.greaterThanOrEqualTo(
                                    root.get("createdAt"),
                                    startDateTime
                            )
            );
        }

        if (endDate != null) {
            LocalDateTime endDateTime =
                    endDate.plusDays(1).atStartOfDay();

            spec = spec.and(
                    (root, query, cb) ->
                            cb.lessThan(
                                    root.get("createdAt"),
                                    endDateTime
                            )
            );
        }

        return transactionRepository
                .findAll(spec, pageable)
                .map(transaction -> toStatementResponse(transaction, accountId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StatementTransactionResponse> getStatement(
            Long accountId,
            Pageable pageable
    ) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Conta não encontrada"
                        )
                );

        validateOwnership(account);

        return transactionRepository
                .findStatementByAccountId(accountId, pageable)
                .map(transaction ->
                        toStatementTransactionResponse(
                                transaction,
                                accountId
                        )
                );
    }

    private Account getOwnedAccount(Long accountId) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Conta não encontrada")
                );

        validateOwnership(account);

        return account;
    }

    private void validateOwnership(Account account) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        if (!account.getCustomer().getEmail().equalsIgnoreCase(email)) {
            throw new ForbiddenOperationException(
                    "Você não possui permissão para acessar esta conta"
            );
        }
    }

    private TransactionResponse toStatementResponse(Transaction transaction, Long accountId) {

        MovementType movementType;

        if (transaction.getType() == TransactionType.DEPOSIT) {
            movementType = MovementType.CREDIT;

        } else if (transaction.getType() == TransactionType.WITHDRAW) {
            movementType = MovementType.DEBIT;

        } else {
            movementType = transaction.getSourceAccount()
                    .getId()
                    .equals(accountId)
                    ? MovementType.DEBIT
                    : MovementType.CREDIT;
        }

        String description =
                resolveDescription(transaction, movementType);

        TransactionResponse response =
                transactionMapper.toResponse(transaction);

        return new TransactionResponse(
                response.id(),
                response.type(),
                response.status(),
                response.amount(),
                response.sourceAccountId(),
                response.destinationAccountId(),
                movementType,
                description,
                response.createdAt()
        );
    }


    private String resolveDescription(
            Transaction transaction,
            MovementType movementType
    ) {

        return switch (transaction.getType()) {

            case DEPOSIT -> "Depósito";

            case WITHDRAW -> "Saque";

            case TRANSFER ->
                    movementType == MovementType.DEBIT
                            ? "Transferência enviada"
                            : "Transferência recebida";

            case PIX ->
                    movementType == MovementType.DEBIT
                            ? "PIX enviado"
                            : "PIX recebido";
        };
    }

    private StatementTransactionResponse toStatementTransactionResponse(
            Transaction transaction,
            Long accountId
    ) {

        TransactionDirection direction =
                resolveTransactionDirection(
                        transaction,
                        accountId
                );

        String description =
                resolveTransactionDescription(
                        transaction,
                        direction
                );

        return new StatementTransactionResponse(
                transaction.getId(),
                transaction.getType(),
                direction,
                transaction.getAmount(),
                description,
                transaction.getCreatedAt()
        );
    }

    private TransactionDirection resolveTransactionDirection(
            Transaction transaction,
            Long accountId
    ) {

        if (transaction.getType() == TransactionType.DEPOSIT) {
            return TransactionDirection.CREDIT;
        }

        if (transaction.getType() == TransactionType.WITHDRAW) {
            return TransactionDirection.DEBIT;
        }

        if (transaction.getSourceAccount() != null
                && transaction.getSourceAccount()
                .getId()
                .equals(accountId)) {

            return TransactionDirection.DEBIT;
        }

        return TransactionDirection.CREDIT;
    }

    private String resolveTransactionDescription(
            Transaction transaction,
            TransactionDirection direction
    ) {

        return switch (transaction.getType()) {

            case DEPOSIT ->
                    "Depósito";

            case WITHDRAW ->
                    "Saque";

            case TRANSFER ->
                    direction == TransactionDirection.DEBIT
                            ? "Transferência enviada"
                            : "Transferência recebida";

            case PIX ->
                    direction == TransactionDirection.DEBIT
                            ? "PIX enviado"
                            : "PIX recebido";
        };
    }
}