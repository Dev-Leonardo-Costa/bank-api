package com.leonardo.bank_api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI bankApiOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("Bank API")
                        .version("1.0.0")
                        .description("""
                                API REST para simulação de operações bancárias.

                                Principais funcionalidades:
                                - Gerenciamento de clientes
                                - Contas bancárias
                                - Autenticação JWT
                                - Transferências
                                - PIX
                                - Limite diário de PIX
                                - PIX agendado
                                - Extrato bancário
                                - Comprovantes
                                """)
                        .contact(new Contact()
                                .name("Leonardo Costa")))
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(SECURITY_SCHEME_NAME)
                )
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        SECURITY_SCHEME_NAME,
                                        new SecurityScheme()
                                                .name(SECURITY_SCHEME_NAME)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                );
    }
}