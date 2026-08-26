package com.leonardo.bank_api.transaction.controller.transactioncontrollerdocs;

import com.leonardo.bank_api.shared.dto.PagedResponse;
import com.leonardo.bank_api.shared.enums.TransactionType;
import com.leonardo.bank_api.transaction.dto.request.DepositRequest;
import com.leonardo.bank_api.transaction.dto.request.TransferRequest;
import com.leonardo.bank_api.transaction.dto.request.WithdrawRequest;
import com.leonardo.bank_api.transaction.dto.response.StatementTransactionResponse;
import com.leonardo.bank_api.transaction.dto.response.TransactionReceiptResponse;
import com.leonardo.bank_api.transaction.dto.response.TransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;


@Tag(
        name = "Transações",
        description = "Consulta de movimentações, extratos e comprovantes"
)
@SecurityRequirement(name = "bearerAuth")
public interface TransactionControllerDocs {

    @Operation(
            summary = "Depositar em conta",
            description = """
                Realiza um depósito em uma conta bancária.

                O depósito pode ser feito por qualquer usuário,
                desde que o identificador da conta seja válido.
                """
    )@ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Depósito realizado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Conta não encontrada"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos"
            )
    })
    ResponseEntity<TransactionResponse> deposit(
            @PathVariable Long accountId,
            @Valid @RequestBody DepositRequest request
    );

    @Operation(
            summary = "Transferir entre contas",
            description = """
                Realiza uma transferência entre duas contas bancárias.

                A transferência só pode ser feita pelo titular da conta de origem.
                """
    )@ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Transferência realizada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não possui acesso à conta de origem"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Conta de origem ou destino não encontrada"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos"
            )
    })
    ResponseEntity<TransactionResponse> transfer(
            @PathVariable Long sourceAccountId,
            @Valid @RequestBody TransferRequest request
    );


    @Operation(
            summary = "Sacar de conta",
            description = """
                Realiza um saque em uma conta bancária.

                O saque só pode ser feito pelo titular da conta.
                """
    )@ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Saque realizado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não possui acesso à conta"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Conta não encontrada"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos"
            )
    })
    ResponseEntity<TransactionResponse> withdraw(
            @PathVariable Long accountId,
            @Valid @RequestBody WithdrawRequest request
    );

    @Operation(
            summary = "Consultar extrato da conta",
            description = """
                Retorna o extrato paginado da conta.

                As movimentações são apresentadas como:

                - CREDIT para valores recebidos;
                - DEBIT para valores enviados.

                O extrato também identifica a contraparte da operação.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Extrato retornado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não possui acesso à conta"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Conta não encontrada"
            )
    })
    ResponseEntity<PagedResponse<TransactionResponse>> getStatement(
            @PathVariable Long accountId,

            @RequestParam(required = false)
            TransactionType type,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    );


    @Operation(
            summary = "Consultar comprovante da transação",
            description = """
                Retorna o comprovante de uma transação.

                O comprovante apresenta informações do pagador,
                recebedor, contas envolvidas, valor, status e data.

                Somente usuários que participaram da transação
                podem consultar o comprovante.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Comprovante retornado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não participou da transação"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Transação não encontrada"
            )
    })
    ResponseEntity<Page<StatementTransactionResponse>> getStatement(@PathVariable Long accountId, Pageable pageable);


    @Operation(
            summary = "Consultar comprovante da transação",
            description = """
                Retorna o comprovante de uma transação.

                O comprovante apresenta informações do pagador,
                recebedor, contas envolvidas, valor, status e data.

                Somente usuários que participaram da transação
                podem consultar o comprovante.
                """
    )@ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Comprovante retornado com sucesso"
            )
    })
    ResponseEntity<TransactionReceiptResponse> getReceipt(@PathVariable Long transactionId);

}
