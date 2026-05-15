package br.edu.imepac.atendimento.config;

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
                        .title("Clínica Médica — API Atendimento")
                        .version("1.0")
                        .description("Registro de atendimentos, prontuários, anotações e exames"));
    }
}
