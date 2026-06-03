package br.edu.imepac.agendamento.integration.administrativo;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

public class FeignConfig {

    @Autowired
    private KeycloakServiceTokenProvider tokenProvider;

    @Bean
    public RequestInterceptor serviceAuthInterceptor() {
        return template -> template.header("Authorization", "Bearer " + tokenProvider.getToken());
    }
}
