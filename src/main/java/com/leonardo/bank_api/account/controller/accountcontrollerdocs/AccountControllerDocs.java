package com.leonardo.bank_api.account.controller.accountcontrollerdocs;

import com.leonardo.bank_api.account.dto.AccountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(
        name = "Contas",
        description = "Operações relacionadas às contas bancárias"
)
@SecurityRequirement(name = "bearerAuth")
public interface AccountControllerDocs {

    @Operation(
            summary = "Criar conta bancária",
            description = """
                Cria uma nova conta bancária vinculada a um cliente.

                A conta é criada com saldo inicial zero e status ativo.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Conta criada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente não encontrado"
            )
    })
    ResponseEntity<AccountResponse> createAccount();

    @Operation(
            summary = "Consultar conta bancária",
            description = "Consulta os dados de uma conta bancária pelo identificador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Conta encontrada"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Conta não encontrada"
            )
    })
    ResponseEntity<AccountResponse> getAccountById(@PathVariable Long id);
}
