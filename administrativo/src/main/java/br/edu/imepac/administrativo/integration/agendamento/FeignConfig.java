package br.edu.imepac.administrativo.integration.agendamento;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

// Nao anotado com @Configuration para nao ser descoberto pelo component scan.
// O Feign cria um contexto filho que herda beans do contexto pai (ServiceTokenProvider).
public class FeignConfig {

    @Autowired
    private KeycloakServiceTokenProvider tokenProvider;

    @Bean
    public RequestInterceptor serviceAuthInterceptor() {
        return template -> template.header("Authorization", "Bearer " + tokenProvider.getToken());
    }
}
