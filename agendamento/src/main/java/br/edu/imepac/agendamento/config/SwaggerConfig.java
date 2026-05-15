package br.edu.imepac.agendamento.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Clinica Medica — API Agendamento")
                        .version("1.0.0")
                        .description("API REST do modulo de agendamento. Gerencia consultas medicas "
                                + "com validacao de conflito de horario e integracao com o modulo administrativo."));
    }
}
