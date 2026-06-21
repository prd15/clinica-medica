package br.edu.imepac.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Atribui um correlation-id a cada request na borda e o injeta no header encaminhado
 * aos microsservicos, garantindo rastreabilidade de ponta a ponta. Registra uma linha
 * de acesso por request com o id, metodo, rota, status e latencia.
 */
@Component
public class CorrelationIdGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdGlobalFilter.class);

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String existing = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);
        String correlationId = StringUtils.hasText(existing) ? existing : UUID.randomUUID().toString();

        // Os headers de entrada do Netty sao read-only; decora o request com uma copia
        // gravavel em vez de mutar o original (que lanca UnsupportedOperationException).
        HttpHeaders headers = new HttpHeaders();
        headers.addAll(exchange.getRequest().getHeaders());
        headers.set(CORRELATION_ID_HEADER, correlationId);
        ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public HttpHeaders getHeaders() {
                return headers;
            }
        };

        exchange.getResponse().getHeaders().set(CORRELATION_ID_HEADER, correlationId);

        long start = System.currentTimeMillis();
        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .doFinally(signal -> {
                    long took = System.currentTimeMillis() - start;
                    HttpStatusCode status = exchange.getResponse().getStatusCode();
                    MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
                    try {
                        log.info("{} {} -> {} ({}ms)",
                                exchange.getRequest().getMethod(),
                                exchange.getRequest().getURI().getRawPath(),
                                status != null ? status.value() : 0,
                                took);
                    } finally {
                        MDC.remove(CORRELATION_ID_MDC_KEY);
                    }
                });
    }

    @Override
    public int getOrder() {
        // logo apos o InvalidPathFilter (HIGHEST_PRECEDENCE), antes do roteamento
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
