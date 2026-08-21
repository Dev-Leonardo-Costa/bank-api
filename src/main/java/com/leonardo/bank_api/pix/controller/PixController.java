package com.leonardo.bank_api.pix.controller;

import com.leonardo.bank_api.pix.dto.request.CreatePixKeyRequest;
import com.leonardo.bank_api.pix.dto.request.PixTransferRequest;
import com.leonardo.bank_api.pix.dto.request.UpdatePixLimitRequest;
import com.leonardo.bank_api.pix.dto.response.PixKeyResponse;
import com.leonardo.bank_api.pix.dto.response.PixLimitResponse;
import com.leonardo.bank_api.pix.dto.response.PixRecipientResponse;
import com.leonardo.bank_api.pix.service.PixService;
import com.leonardo.bank_api.transaction.dto.response.TransactionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/pix")
@RequiredArgsConstructor
public class PixController {

    private final PixService pixService;

    @PostMapping("/keys")
    public ResponseEntity<PixKeyResponse> createPixKey(
            @Valid @RequestBody CreatePixKeyRequest request
    ) {

        PixKeyResponse response =
                pixService.createPixKey(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/accounts/{sourceAccountId}/transfer")
    public ResponseEntity<TransactionResponse> transfer(
            @PathVariable Long sourceAccountId,
            @Valid @RequestBody PixTransferRequest request
    ) {

        return ResponseEntity.ok(
                pixService.transfer(sourceAccountId, request)
        );
    }

    @GetMapping("/keys")
    public ResponseEntity<List<PixKeyResponse>> findMyPixKeys() {
        return ResponseEntity.ok(
                pixService.findMyPixKeys()
        );
    }

    @GetMapping("/keys/{keyValue}")
    public ResponseEntity<PixRecipientResponse> findRecipientByKey(
            @PathVariable String keyValue
    ) {

        return ResponseEntity.ok(
                pixService.findRecipientByKey(keyValue)
        );
    }

    @DeleteMapping("/keys/{pixKeyId}")
    public ResponseEntity<Void> deletePixKey(@PathVariable Long pixKeyId) {
        pixService.deletePixKey(pixKeyId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/accounts/{accountId}/limit")
    public ResponseEntity<PixLimitResponse> getDailyPixLimit(@PathVariable Long accountId) {
        return ResponseEntity.ok(
                pixService.getDailyPixLimit(accountId)
        );
    }

    @PatchMapping("/accounts/{accountId}/limit")
    public ResponseEntity<PixLimitResponse> updateDailyPixLimit(@PathVariable Long accountId, @Valid @RequestBody UpdatePixLimitRequest request) {
        return ResponseEntity.ok(
                pixService.updateDailyPixLimit(
                        accountId,
                        request
                )
        );
    }
}