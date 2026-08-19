package com.leonardo.bank_api.transaction.service.impl;

import com.leonardo.bank_api.account.entity.Account;
import com.leonardo.bank_api.account.repository.AccountRepository;
import com.leonardo.bank_api.common.exception.BusinessException;
import com.leonardo.bank_api.common.exception.ForbiddenOperationException;
import com.leonardo.bank_api.common.exception.ResourceNotFoundException;
import com.leonardo.bank_api.shared.enums.AccountStatus;
import com.leonardo.bank_api.shared.enums.TransactionStatus;
import com.leonardo.bank_api.shared.enums.TransactionType;
import com.leonardo.bank_api.transaction.dto.request.DepositRequest;
import com.leonardo.bank_api.transaction.dto.request.TransferRequest;
import com.leonardo.bank_api.transaction.dto.request.WithdrawRequest;
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
                .map(transactionMapper::toResponse);
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
}