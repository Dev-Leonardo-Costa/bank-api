package com.leonardo.bank_api.customer.controller.customercontrollerdocs;


import com.leonardo.bank_api.customer.dto.request.CreateCustomerRequest;
import com.leonardo.bank_api.customer.dto.response.CustomerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(
        name = "Clientes",
        description = "Gerenciamento de clientes da instituição financeira"
)
@SecurityRequirement(name = "bearerAuth")
public interface CustomerControllerDocs {

    @Operation(
            summary = "Cadastrar cliente",
            description = """
                Cadastra um novo cliente na Bank API.

                O CPF e o e-mail devem ser únicos.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Cliente cadastrado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "CPF ou e-mail já cadastrado"
            )
    })
    ResponseEntity<CustomerResponse> createCustomer(@RequestBody @Valid CreateCustomerRequest request);

    @Operation(
            summary = "Consultar cliente",
            description = "Consulta os dados de um cliente pelo identificador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cliente encontrado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente não encontrado"
            )
    })
    ResponseEntity<CustomerResponse> getCustomerById(@PathVariable @Valid Long id);
}
