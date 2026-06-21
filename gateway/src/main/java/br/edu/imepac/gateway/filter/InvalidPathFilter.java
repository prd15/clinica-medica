package br.edu.imepac.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

// Paths com // chegam normalizados pelo Gateway e podem rotear para endpoints errados,
// causando 405 em vez de 404. Rejeita na borda antes de qualquer roteamento.
@Component
public class InvalidPathFilter implements WebFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(InvalidPathFilter.class);

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getRawPath();
        if (path.contains("//")) {
            log.warn("Path invalido rejeitado na borda: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }
}
