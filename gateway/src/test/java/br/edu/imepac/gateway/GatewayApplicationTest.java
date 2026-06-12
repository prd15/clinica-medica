package br.edu.imepac.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

// ReactiveJwtDecoder precisa ser mockado pois sem Keycloak rodando o contexto nao sobe
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayApplicationTest {

    @MockBean
    ReactiveJwtDecoder jwtDecoder;

    @Test
    void contextLoads() {
    }
}
