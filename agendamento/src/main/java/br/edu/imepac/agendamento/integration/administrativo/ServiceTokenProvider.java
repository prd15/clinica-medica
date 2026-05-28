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

    synchronized String getToken() {
        if (cachedToken == null || Instant.now().isAfter(expiresAt.minusSeconds(30))) {
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
        int expiresIn = (Integer) response.getOrDefault("expires_in", 300);
        expiresAt = Instant.now().plusSeconds(expiresIn);
        log.debug("Token SERVICE renovado, expira em {}s", expiresIn);
    }
}
