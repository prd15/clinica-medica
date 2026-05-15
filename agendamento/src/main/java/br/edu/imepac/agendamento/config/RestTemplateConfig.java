package br.edu.imepac.agendamento.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    // expoe o RestTemplate como bean pra injetar via construtor no AdministrativoClient
    // (sem isso o Spring nao sabe construir um RestTemplate sozinho — nao tem default bean)
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
