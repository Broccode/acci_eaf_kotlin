package com.acci.eaf.iam.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Konfiguration für die OpenAPI-Dokumentation.
 */
@Configuration
class OpenApiConfig {

    /**
     * Definiert die OpenAPI-Konfiguration für die Anwendung.
     *
     * @return die OpenAPI-Konfiguration
     */
    @Bean
    fun openApi(): OpenAPI =
        OpenAPI()
            .info(apiInfo())
            .servers(listOf(Server().url("/")))
            .components(
                Components()
                    .addSecuritySchemes(
                        "bearerAuth",
                        SecurityScheme()
                            .name("bearerAuth")
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("JWT Authorization header using the Bearer scheme")
                    )
            )
            .addSecurityItem(SecurityRequirement().addList("bearerAuth"))

    /**
     * Definiert die API-Informationen.
     *
     * @return die API-Informationen
     */
    private fun apiInfo(): Info =
        Info()
            .title("EAF IAM API")
            .description("API für die Verwaltung von Benutzern und Berechtigungen in der EAF-Plattform")
            .version("v1")
            .contact(
                Contact()
                    .name("ACCI Development Team")
                    .email("support@acci.com")
                    .url("https://acci.com")
            )
}
