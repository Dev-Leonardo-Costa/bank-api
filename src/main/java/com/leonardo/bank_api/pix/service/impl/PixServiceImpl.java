package com.leonardo.bank_api.pix.service.impl;

import com.leonardo.bank_api.account.entity.Account;
import com.leonardo.bank_api.account.repository.AccountRepository;
import com.leonardo.bank_api.common.exception.BusinessException;
import com.leonardo.bank_api.common.exception.ForbiddenOperationException;
import com.leonardo.bank_api.common.exception.ResourceNotFoundException;
import com.leonardo.bank_api.pix.dto.request.CreatePixKeyRequest;
import com.leonardo.bank_api.pix.dto.request.PixTransferRequest;
import com.leonardo.bank_api.pix.dto.request.UpdatePixLimitRequest;
import com.leonardo.bank_api.pix.dto.response.PixKeyResponse;
import com.leonardo.bank_api.pix.dto.response.PixLimitResponse;
import com.leonardo.bank_api.pix.dto.response.PixRecipientResponse;
import com.leonardo.bank_api.pix.entity.PixKey;
import com.leonardo.bank_api.pix.mapper.PixMapper;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PixServiceImpl implements PixService {

    private final PixKeyRepository pixKeyRepository;
    private final AccountRepository accountRepository;
    private final PixMapper pixMapper;
    private final List<PixKeyValidator> validators;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

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
    public TransactionResponse transfer(Long sourceAccountId, PixTransferRequest request) {

        String normalizedKey = normalizePixKey(request.pixKey());

        PixKey pixKey = pixKeyRepository.findByKeyValue(normalizedKey)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Chave PIX não encontrada"
                        )
                );

        Long destinationAccountId = pixKey.getAccount().getId();

        if (sourceAccountId.equals(destinationAccountId)) {
            throw new BusinessException(
                    "Não é possível realizar PIX para a própria conta"
            );
        }

        Long firstId = Math.min(
                sourceAccountId,
                destinationAccountId
        );

        Long secondId = Math.max(
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

        if (sourceAccount.getBalance()
                .compareTo(request.amount()) < 0) {

            throw new BusinessException(
                    "Saldo insuficiente"
            );
        }

        validateDailyPixLimit(
                sourceAccount,
                request.amount()
        );

        sourceAccount.setBalance(
                sourceAccount.getBalance()
                        .subtract(request.amount())
        );

        destinationAccount.setBalance(
                destinationAccount.getBalance()
                        .add(request.amount())
        );

        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        Transaction transaction = Transaction.builder()
                .type(TransactionType.PIX)
                .status(TransactionStatus.COMPLETED)
                .amount(request.amount())
                .sourceAccount(sourceAccount)
                .destinationAccount(destinationAccount)
                .build();

        Transaction savedTransaction =
                transactionRepository.save(transaction);

        return transactionMapper.toResponse(savedTransaction);
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

        return new PixLimitResponse(account.getDailyPixLimit());
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

        return new PixLimitResponse(
                account.getDailyPixLimit()
        );
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

            case CPF ->
                    "***.***.***-" +
                            keyValue.substring(keyValue.length() - 2);

            case EMAIL -> {
                String[] parts = keyValue.split("@");

                yield parts[0].charAt(0)
                        + "***@"
                        + parts[1];
            }

            case PHONE ->
                    "+55 (***) *****-"
                            + keyValue.substring(
                            keyValue.length() - 4
                    );

            case RANDOM ->
                    keyValue.substring(0, 8) + "****";
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
}