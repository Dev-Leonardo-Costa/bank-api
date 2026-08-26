package com.leonardo.bank_api.pix.controller.pixcontrollerdocs;

import com.leonardo.bank_api.pix.dto.request.CreatePixKeyRequest;
import com.leonardo.bank_api.pix.dto.request.PixTransferRequest;
import com.leonardo.bank_api.pix.dto.request.UpdatePixLimitRequest;
import com.leonardo.bank_api.pix.dto.response.PixKeyResponse;
import com.leonardo.bank_api.pix.dto.response.PixLimitResponse;
import com.leonardo.bank_api.pix.dto.response.PixRecipientResponse;
import com.leonardo.bank_api.transaction.dto.response.TransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(
        name = "PIX",
        description = "Operações relacionadas ao sistema PIX"
)
@SecurityRequirement(name = "bearerAuth")
public interface PixControllerDocs {

    @Operation(
            summary = "Cadastrar chave PIX",
            description = """
                Cadastra uma nova chave PIX para uma conta do usuário autenticado.

                Tipos suportados:
                - CPF
                - EMAIL
                - PHONE
                - RANDOM
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Chave PIX cadastrada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Chave PIX inválida"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não possui acesso à conta"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Chave PIX já cadastrada"
            )
    })
    ResponseEntity<PixKeyResponse> createPixKey(
            @Valid @RequestBody CreatePixKeyRequest request
    );


    @Operation(
            summary = "Realizar transferência PIX",
            description = """
                Realiza uma transferência PIX utilizando uma chave de destino.

                Durante a operação são realizadas validações de:

                - propriedade da conta de origem;
                - existência da chave PIX;
                - conta de origem ativa;
                - conta de destino ativa;
                - saldo disponível;
                - limite diário de PIX;
                - tentativa de transferência para a própria conta.

                As contas envolvidas são bloqueadas durante a operação
                para reduzir inconsistências causadas por concorrência.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "PIX realizado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Regra de negócio violada"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não possui acesso à conta de origem"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Conta ou chave PIX não encontrada"
            )
    })
    ResponseEntity<TransactionResponse> transfer(
            @PathVariable Long sourceAccountId,
            @Valid @RequestBody PixTransferRequest request
    );

    @Operation(
            summary = "Listar minhas chaves PIX",
            description = """
                Retorna todas as chaves PIX cadastradas pelo usuário autenticado.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Chaves PIX retornadas com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado"
            )
    })
    ResponseEntity<List<PixKeyResponse>> findMyPixKeys();

    @Operation(
            summary = "Consultar chave PIX",
            description = """
                Consulta uma chave PIX pelo valor da chave.

                A resposta apresenta os dados do proprietário da chave
                e da conta associada.
                """
    )@ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Chave PIX encontrada"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Chave PIX não encontrada"
            )
    })
    ResponseEntity<PixRecipientResponse> findRecipientByKey(@PathVariable String keyValue);


    @Operation(
            summary = "Excluir chave PIX",
            description = """
                Exclui uma chave PIX.

                Somente o proprietário da conta associada à chave
                pode realizar a exclusão.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Chave PIX removida com sucesso"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não possui permissão para excluir a chave"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Chave PIX não encontrada"
            )
    })
    ResponseEntity<Void> deletePixKey(@PathVariable Long pixKeyId);

    @Operation(
            summary = "Consultar limite diário PIX",
            description = """
                Consulta o limite diário PIX da conta.

                A resposta também apresenta:

                - valor utilizado no dia;
                - valor ainda disponível para transferências.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Limite consultado com sucesso"
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
    ResponseEntity<PixLimitResponse> getDailyPixLimit(@PathVariable Long accountId);

    @Operation(
            summary = "Atualizar limite diário PIX",
            description = """
                Atualiza o limite diário de PIX de uma conta.

                Somente o proprietário da conta pode alterar o limite.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Limite atualizado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Valor do limite inválido"
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
    ResponseEntity<PixLimitResponse> updateDailyPixLimit(
            @PathVariable Long accountId,
            @Valid @RequestBody
            UpdatePixLimitRequest request
    );

}
