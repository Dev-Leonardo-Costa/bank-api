package com.leonardo.bank_api.auth.controller.authcontrollerdocs;

import com.leonardo.bank_api.security.dto.request.LoginRequest;
import com.leonardo.bank_api.security.dto.response.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;


@Tag(
        name = "Autenticação",
        description = "Endpoints responsáveis pela autenticação e geração do token JWT"
)
public interface PixControllerDocs {

    @Operation(
            summary = "Realizar login",
            description = """
                Autentica o usuário utilizando e-mail e senha.

                Quando as credenciais são válidas, a API retorna um token JWT
                que deve ser utilizado nos endpoints protegidos.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login realizado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados da requisição inválidos"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "E-mail ou senha inválidos"
            )
    })
    ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request);
}
