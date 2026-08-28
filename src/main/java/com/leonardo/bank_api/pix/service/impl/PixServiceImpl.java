package com.leonardo.bank_api.pix.service.impl;

import com.leonardo.bank_api.account.entity.Account;
import com.leonardo.bank_api.account.repository.AccountRepository;
import com.leonardo.bank_api.common.exception.BusinessException;
import com.leonardo.bank_api.common.exception.ForbiddenOperationException;
import com.leonardo.bank_api.common.exception.IdempotencyConflictException;
import com.leonardo.bank_api.common.exception.ResourceNotFoundException;
import com.leonardo.bank_api.pix.dto.request.CreatePixKeyRequest;
import com.leonardo.bank_api.pix.dto.request.PixTransferRequest;
import com.leonardo.bank_api.pix.dto.request.UpdatePixLimitRequest;
import com.leonardo.bank_api.pix.dto.response.PixKeyResponse;
import com.leonardo.bank_api.pix.dto.response.PixLimitResponse;
import com.leonardo.bank_api.pix.dto.response.PixRecipientResponse;
import com.leonardo.bank_api.pix.entity.PixIdempotency;
import com.leonardo.bank_api.pix.entity.PixKey;
import com.leonardo.bank_api.pix.mapper.PixMapper;
import com.leonardo.bank_api.pix.repository.PixIdempotencyRepository;
import com.leonardo.bank_api.pix.repository.PixKeyRepository;
import com.leonardo.bank_api.pix.service.PixService;
import com.leonardo.bank_api.pix.validation.PixKeyValidator;
import com.leonardo.bank_api.shared.enums.AccountStatus;
import com.leonardo.bank_api.shared.enums.PixKeyType;
import com.leonardo.bank_api.shared.enums.TransactionStatus;
import com.leonardo.bank_api.shared.enums.TransactionType;
import com.leonardo.bank_api.transaction.dto.response.TransactionResponse;
import com.leonardo.bank_api.transaction.entity.Transaction;
import com.leonardo.bank_api.transaction.mapper.TransactionMapper;
import com.leonardo.bank_api.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PixServiceImpl implements PixService {

    private final PixKeyRepository pixKeyRepository;
    private final AccountRepository accountRepository;
    private final PixMapper pixMapper;
    private final List<PixKeyValidator> validators;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final PixIdempotencyRepository pixIdempotencyRepository;

    @Transactional
    @Override
    public PixKeyResponse createPixKey(CreatePixKeyRequest request) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Account account = accountRepository.findByCustomerEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Conta não encontrada")
                );

        PixKeyValidator validator = getValidator(request.type());

        String keyValue =
                validator.validateAndNormalize(request.keyValue());

        validateCustomerData(request.type(), keyValue, account);

        if (pixKeyRepository.existsByKeyValue(keyValue)) {
            throw new BusinessException(
                    "Chave PIX já cadastrada"
            );
        }

        PixKey pixKey = PixKey.builder()
                .type(request.type())
                .keyValue(keyValue)
                .account(account)
                .build();

        PixKey savedPixKey = pixKeyRepository.save(pixKey);

        return pixMapper.toResponse(savedPixKey);
    }

    @Transactional
    @Override
    public TransactionResponse transfer(Long sourceAccountId, PixTransferRequest request, String idempotencyKey) {

        validateIdempotencyKey(idempotencyKey);

        String requestHash = generateRequestHash(sourceAccountId, request);

        int reserved = pixIdempotencyRepository.reserve(idempotencyKey, requestHash);

        if (reserved == 0) {
            PixIdempotency existing =
                    pixIdempotencyRepository
                            .findByIdempotencyKey(idempotencyKey)
                            .orElseThrow(() ->
                                    new BusinessException(
                                            "Não foi possível recuperar a operação idempotente"
                                    )
                            );

            if (!requestHash.equals(existing.getRequestHash())) {
                throw new IdempotencyConflictException("Idempotency-Key já utilizada com dados diferentes");
            }

            if (existing.getTransaction() == null) {
                throw new BusinessException("Operação idempotente ainda não possui transação associada");
            }
            return transactionMapper.toResponse(existing.getTransaction());
        }

        PixTransferContext context = preparePixTransfer(sourceAccountId, request.pixKey());

        validateOwnership(context.sourceAccount());

        Transaction transaction = executePixTransferEntity(context, request.amount());

        PixIdempotency idempotency = pixIdempotencyRepository
                .findByIdempotencyKey(idempotencyKey)
                .orElseThrow(() ->
                        new BusinessException(
                                "Registro de idempotência não encontrado"
                        )
                );

        idempotency.setTransaction(transaction);

        pixIdempotencyRepository.save(idempotency);

        return transactionMapper.toResponse(transaction);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PixKeyResponse> findMyPixKeys() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return pixKeyRepository
                .findAllByAccountCustomerEmail(email)
                .stream()
                .map(pixMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public PixRecipientResponse findRecipientByKey(String keyValue) {

        String normalizedKey = normalizePixKey(keyValue);

        PixKey pixKey = pixKeyRepository.findByKeyValue(normalizedKey)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Chave PIX não encontrada"
                        )
                );

        return new PixRecipientResponse(
                pixKey.getAccount()
                        .getCustomer()
                        .getFullName(),
                pixKey.getType(),
                maskPixKey(
                        pixKey.getType(),
                        pixKey.getKeyValue()
                ),
                "Bank API"
        );
    }

    @Transactional
    @Override
    public void deletePixKey(Long pixKeyId) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        PixKey pixKey = pixKeyRepository.findById(pixKeyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Chave PIX não encontrada"
                        )
                );

        String ownerEmail = pixKey.getAccount()
                .getCustomer()
                .getEmail();

        if (!ownerEmail.equalsIgnoreCase(email)) {
            throw new ForbiddenOperationException(
                    "Você não possui permissão para excluir esta chave PIX"
            );
        }

        pixKeyRepository.delete(pixKey);
    }

    @Override
    @Transactional(readOnly = true)
    public PixLimitResponse getDailyPixLimit(Long accountId) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Conta não encontrada"
                        )
                );

        validateOwnership(account);

        LocalDate today = LocalDate.now();

        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        BigDecimal usedToday =
                transactionRepository.sumAmountByAccountAndPeriod(
                        accountId,
                        TransactionType.PIX,
                        TransactionStatus.COMPLETED,
                        startOfDay,
                        endOfDay
                );

        BigDecimal availableToday =
                account.getDailyPixLimit()
                        .subtract(usedToday);

        return new PixLimitResponse(
                account.getDailyPixLimit(),
                usedToday,
                availableToday
        );
    }

    @Transactional
    @Override
    public PixLimitResponse updateDailyPixLimit(Long accountId, UpdatePixLimitRequest request) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Conta não encontrada"
                        )
                );

        validateOwnership(account);

        account.setDailyPixLimit(
                request.dailyPixLimit()
        );

        accountRepository.save(account);

        LocalDate today = LocalDate.now();

        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        BigDecimal usedToday =
                transactionRepository.sumAmountByAccountAndPeriod(
                        accountId,
                        TransactionType.PIX,
                        TransactionStatus.COMPLETED,
                        startOfDay,
                        endOfDay
                );

        BigDecimal availableToday =
                account.getDailyPixLimit()
                        .subtract(usedToday);

        return new PixLimitResponse(
                account.getDailyPixLimit(),
                usedToday,
                availableToday
        );
    }

    @Transactional
    @Override
    public TransactionResponse executeScheduledPix(Long sourceAccountId, String pixKey, BigDecimal amount) {
        PixTransferContext context = preparePixTransfer(sourceAccountId, pixKey);

        Transaction transaction = executePixTransferEntity(context, amount);

        return transactionMapper.toResponse(transaction);
    }

    private PixKeyValidator getValidator(PixKeyType type) {

        return validators.stream()
                .filter(validator -> validator.getType() == type)
                .findFirst()
                .orElseThrow(() ->
                        new BusinessException(
                                "Tipo de chave PIX não suportado"
                        )
                );
    }

    private void validateCustomerData(
            PixKeyType type,
            String keyValue,
            Account account
    ) {

        if (type == PixKeyType.EMAIL) {

            String customerEmail = account.getCustomer()
                    .getEmail()
                    .trim()
                    .toLowerCase();

            if (!customerEmail.equals(keyValue)) {
                throw new BusinessException(
                        "A chave PIX do tipo EMAIL deve ser o e-mail cadastrado do cliente"
                );
            }
        }

        if (type == PixKeyType.CPF) {

            String customerCpf = account.getCustomer()
                    .getCpf()
                    .replaceAll("\\D", "");

            if (!customerCpf.equals(keyValue)) {
                throw new BusinessException(
                        "A chave PIX do tipo CPF deve ser o CPF cadastrado do cliente"
                );
            }
        }
    }

    private void validateOwnership(Account account) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        if (!account.getCustomer()
                .getEmail()
                .equalsIgnoreCase(email)) {

            throw new ForbiddenOperationException(
                    "Você não possui permissão para movimentar esta conta"
            );
        }
    }


    private String normalizePixKey(String keyValue) {

        if (keyValue == null || keyValue.isBlank()) {
            throw new BusinessException("Chave PIX é obrigatória");
        }

        String value = keyValue.trim();

        if (value.contains("@")) {
            return value.toLowerCase();
        }

        String onlyNumbers = value.replaceAll("\\D", "");

        if (onlyNumbers.length() == 11) {
            return onlyNumbers;
        }

        if (onlyNumbers.length() == 13 && onlyNumbers.startsWith("55")) {
            return "+" + onlyNumbers;
        }

        return value;
    }

    private String maskPixKey(PixKeyType type, String keyValue) {

        return switch (type) {

            case CPF -> "***.***.***-" +
                    keyValue.substring(keyValue.length() - 2);

            case EMAIL -> {
                String[] parts = keyValue.split("@");

                yield parts[0].charAt(0)
                        + "***@"
                        + parts[1];
            }

            case PHONE -> "+55 (***) *****-"
                    + keyValue.substring(
                    keyValue.length() - 4
            );

            case RANDOM -> keyValue.substring(0, 8) + "****";
        };

    }

    private void validateDailyPixLimit(
            Account sourceAccount,
            BigDecimal amount
    ) {

        LocalDate today = LocalDate.now();

        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        BigDecimal amountSentToday =
                transactionRepository.sumAmountByAccountAndPeriod(
                        sourceAccount.getId(),
                        TransactionType.PIX,
                        TransactionStatus.COMPLETED,
                        startOfDay,
                        endOfDay
                );

        BigDecimal totalAfterTransfer =
                amountSentToday.add(amount);

        if (totalAfterTransfer.compareTo(
                sourceAccount.getDailyPixLimit()
        ) > 0) {

            throw new BusinessException(
                    "Limite diário de PIX excedido"
            );
        }
    }

    private record PixTransferContext(Account sourceAccount, Account destinationAccount) {
    }

    private PixTransferContext preparePixTransfer(Long sourceAccountId, String pixKeyValue) {

        String normalizedKey =
                normalizePixKey(pixKeyValue);

        PixKey pixKey = pixKeyRepository
                .findByKeyValue(normalizedKey)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Chave PIX não encontrada"
                        )
                );

        Long destinationAccountId =
                pixKey.getAccount().getId();

        if (sourceAccountId.equals(destinationAccountId)) {
            throw new BusinessException(
                    "Não é possível realizar PIX para a própria conta"
            );
        }

        Long firstId =
                Math.min(
                        sourceAccountId,
                        destinationAccountId
                );

        Long secondId =
                Math.max(
                        sourceAccountId,
                        destinationAccountId
                );

        Account firstAccount =
                accountRepository.findByIdForUpdate(firstId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Conta não encontrada"
                                )
                        );

        Account secondAccount =
                accountRepository.findByIdForUpdate(secondId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Conta não encontrada"
                                )
                        );

        Account sourceAccount =
                firstAccount.getId().equals(sourceAccountId)
                        ? firstAccount
                        : secondAccount;

        Account destinationAccount =
                firstAccount.getId().equals(destinationAccountId)
                        ? firstAccount
                        : secondAccount;

        return new PixTransferContext(sourceAccount, destinationAccount);
    }

    private TransactionResponse executePixTransfer(PixTransferContext context, BigDecimal amount) {

        Account sourceAccount =
                context.sourceAccount();

        Account destinationAccount =
                context.destinationAccount();

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

        if (sourceAccount.getBalance()
                .compareTo(amount) < 0) {

            throw new BusinessException(
                    "Saldo insuficiente"
            );
        }

        validateDailyPixLimit(
                sourceAccount,
                amount
        );

        sourceAccount.setBalance(
                sourceAccount.getBalance()
                        .subtract(amount)
        );

        destinationAccount.setBalance(
                destinationAccount.getBalance()
                        .add(amount)
        );

        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        Transaction transaction =
                Transaction.builder()
                        .type(TransactionType.PIX)
                        .status(TransactionStatus.COMPLETED)
                        .amount(amount)
                        .sourceAccount(sourceAccount)
                        .destinationAccount(destinationAccount)
                        .build();

        Transaction savedTransaction =
                transactionRepository.save(transaction);

        return transactionMapper
                .toResponse(savedTransaction);
    }

    private void validateIdempotencyKey(String idempotencyKey) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BusinessException("Idempotency-Key é obrigatório");
        }

        if (idempotencyKey.length() > 100) {
            throw new BusinessException("Idempotency-Key deve possuir no máximo 100 caracteres");
        }
    }

    private Transaction executePixTransferEntity(PixTransferContext context, BigDecimal amount) {

        Account sourceAccount = context.sourceAccount();

        Account destinationAccount = context.destinationAccount();

        if (sourceAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("A conta de origem não está ativa");
        }

        if (destinationAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException(
                    "A conta de destino não está ativa"
            );
        }

        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("Saldo insuficiente");
        }

        validateDailyPixLimit(sourceAccount, amount);

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));

        destinationAccount.setBalance(destinationAccount.getBalance().add(amount));

        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        Transaction transaction =
                Transaction.builder()
                        .type(TransactionType.PIX)
                        .status(TransactionStatus.COMPLETED)
                        .amount(amount)
                        .sourceAccount(sourceAccount)
                        .destinationAccount(destinationAccount)
                        .build();

        return transactionRepository.save(transaction);
    }

    private String generateRequestHash(Long sourceAccountId, PixTransferRequest request) {

        String rawData =
                sourceAccountId
                        + "|"
                        + request.pixKey()
                        + "|"
                        + request.amount().stripTrailingZeros().toPlainString();

        try {

            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            rawData.getBytes(StandardCharsets.UTF_8)
                    );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(
                    "Erro ao gerar hash da requisição PIX",
                    ex
            );
        }
    }
}