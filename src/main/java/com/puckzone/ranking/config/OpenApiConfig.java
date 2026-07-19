package com.puckzone.ranking.config;

import java.util.List;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Metadatos del API para springdoc. El esquema bearer-jwt habilita el botón
 * Authorize de Swagger UI: el gateway exige el token en cada ruta /api/** (este
 * servicio no lo valida aún, pero las llamadas de la UI pasan por el gateway).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI puckzoneOpenApi() {
        return new OpenAPI()
                // Server relativo: la spec se resuelve contra el origen desde
                // donde se cargo (el gateway en Azure, localhost en dev). Sin
                // esto springdoc generaria la URL interna del contenedor y el
                // Try it out de Swagger UI apuntaria a un host inalcanzable.
                .servers(List.of(new Server().url("/")))
                .info(new Info()
                        .title("PuckZone Ranking API")
                        .version("v1")
                        .description("Leaderboards por jugador, global y por universidad. "
                                + "ELO estándar K=30, inicial 1200."))
                .components(new Components().addSecuritySchemes("bearer-jwt",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}
