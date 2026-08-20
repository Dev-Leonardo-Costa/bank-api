package com.leonardo.bank_api.pix.service;

import com.leonardo.bank_api.pix.dto.request.CreatePixKeyRequest;
import com.leonardo.bank_api.pix.dto.request.PixTransferRequest;
import com.leonardo.bank_api.pix.dto.response.PixKeyResponse;
import com.leonardo.bank_api.pix.dto.response.PixRecipientResponse;
import com.leonardo.bank_api.transaction.dto.response.TransactionResponse;

import java.util.List;

public interface PixService {

    PixKeyResponse createPixKey(CreatePixKeyRequest request);

    TransactionResponse transfer(Long sourceAccountId, PixTransferRequest request);

    List<PixKeyResponse> findMyPixKeys();

    PixRecipientResponse findRecipientByKey(String keyValue);

    void deletePixKey(Long pixKeyId);
}
