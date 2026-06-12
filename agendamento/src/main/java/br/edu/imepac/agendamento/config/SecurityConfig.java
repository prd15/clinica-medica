package br.edu.imepac.agendamento.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Value("${app.security.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${app.security.jwt.audience}")
    private String audience;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()

                        // paths especificos antes dos genericos — ordem importa no Spring Security
                        .requestMatchers(HttpMethod.GET, "/v1/consultas/contagem").hasAnyRole("ADMIN", "SERVICE")
                        .requestMatchers(HttpMethod.GET, "/v1/consultas/minha-agenda").hasAnyRole("MEDICO", "ADMIN")

                        // realizar: somente SERVICE — endpoint interno do Outbox, ADMIN nao deve chamar diretamente
                        .requestMatchers(HttpMethod.PATCH, "/v1/consultas/*/realizar").hasRole("SERVICE")
                        .requestMatchers(HttpMethod.PATCH, "/v1/consultas/*/reagendar").hasAnyRole("ADMIN", "ATENDENTE")
                        .requestMatchers(HttpMethod.PATCH, "/v1/consultas/*/confirmar").hasAnyRole("ADMIN", "ATENDENTE")

                        .requestMatchers(HttpMethod.POST,   "/v1/consultas").hasAnyRole("ADMIN", "ATENDENTE")
                        .requestMatchers(HttpMethod.GET,    "/v1/consultas/**").hasAnyRole("ADMIN", "ATENDENTE", "MEDICO", "SERVICE")
                        .requestMatchers(HttpMethod.DELETE, "/v1/consultas/**").hasAnyRole("ADMIN", "ATENDENTE")

                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRolesConverter());
        return converter;
    }

    // Valida assinatura (JWKS) + issuer + audience + expiracao.
    // Sem isso, qualquer token assinado pela mesma chave (outro realm/client) seria aceito.
    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        OAuth2TokenValidator<Jwt> audienceValidator = jwt ->
                jwt.getAudience() != null && jwt.getAudience().contains(audience)
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(
                                new OAuth2Error("invalid_token", "Audience requerida ausente: " + audience, null));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(),
                new JwtIssuerValidator(issuerUri),
                audienceValidator));
        return decoder;
    }
}
