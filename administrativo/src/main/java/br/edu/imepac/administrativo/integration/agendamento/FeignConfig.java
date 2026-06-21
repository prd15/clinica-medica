package br.edu.imepac.administrativo.integration.agendamento;

import br.edu.imepac.commons.logging.CorrelationIdFilter;
import feign.RequestInterceptor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

// Nao anotado com @Configuration para nao ser descoberto pelo component scan.
// O Feign cria um contexto filho que herda beans do contexto pai (ServiceTokenProvider).
public class FeignConfig {

    @Autowired
    private ServiceTokenProvider tokenProvider;

    @Bean
    public RequestInterceptor serviceAuthInterceptor() {
        return template -> template.header("Authorization", "Bearer " + tokenProvider.getToken());
    }

    // Propaga o correlation-id da request atual para o microsservico chamado,
    // permitindo rastrear a chamada de ponta a ponta nos logs.
    @Bean
    public RequestInterceptor correlationIdInterceptor() {
        return template -> {
            String correlationId = MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
            if (correlationId != null) {
                template.header(CorrelationIdFilter.CORRELATION_ID_HEADER, correlationId);
            }
        };
    }
}
