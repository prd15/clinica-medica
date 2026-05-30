package br.edu.imepac.agendamento.integration.administrativo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Component
class ServiceTokenProvider {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${keycloak.service.token-url}")
    private String tokenUrl;

    @Value("${keycloak.service.client-id}")
    private String clientId;

    @Value("${keycloak.service.client-secret}")
    private String clientSecret;

    private volatile String cachedToken;
    private volatile Instant expiresAt = Instant.MIN;

    String getToken() {
        String token = cachedToken;
        if (token != null && Instant.now().isBefore(expiresAt.minusSeconds(30))) {
            return token;
        }
        return refreshAndGet();
    }

    private synchronized String refreshAndGet() {
        // re-check sob lock: outra thread pode ter renovado enquanto esperavamos
        if (cachedToken == null || !Instant.now().isBefore(expiresAt.minusSeconds(30))) {
            refresh();
        }
        return cachedToken;
    }

    private void refresh() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(
                tokenUrl, new HttpEntity<>(body, headers), Map.class);

        if (response == null || !response.containsKey("access_token")) {
            throw new IllegalStateException("Keycloak nao retornou access_token");
        }

        cachedToken = (String) response.get("access_token");
        // expires_in pode vir como Integer ou Long dependendo do serializer — Number cobre ambos
        long expiresIn = ((Number) response.getOrDefault("expires_in", 300)).longValue();
        expiresAt = Instant.now().plusSeconds(expiresIn);
        log.debug("Token SERVICE renovado, expira em {}s", expiresIn);
    }
}
