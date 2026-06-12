package br.edu.imepac.administrativo.config;

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

                        // endpoints publicos
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()

                        // atendentes — somente ADMIN
                        .requestMatchers("/v1/atendentes/**").hasRole("ADMIN")

                        // relatorios — somente ADMIN
                        .requestMatchers("/v1/relatorios/**").hasRole("ADMIN")

                        // pacientes
                        .requestMatchers(HttpMethod.GET,    "/v1/pacientes/**").hasAnyRole("ADMIN", "ATENDENTE", "MEDICO", "SERVICE")
                        .requestMatchers(HttpMethod.POST,   "/v1/pacientes/**").hasAnyRole("ADMIN", "ATENDENTE")
                        .requestMatchers(HttpMethod.PUT,    "/v1/pacientes/**").hasAnyRole("ADMIN", "ATENDENTE")
                        .requestMatchers(HttpMethod.DELETE, "/v1/pacientes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH,  "/v1/pacientes/**").hasRole("ADMIN")

                        // medicos
                        .requestMatchers(HttpMethod.GET,    "/v1/medicos/**").hasAnyRole("ADMIN", "ATENDENTE", "MEDICO", "SERVICE")
                        .requestMatchers(HttpMethod.POST,   "/v1/medicos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/v1/medicos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/v1/medicos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH,  "/v1/medicos/**").hasRole("ADMIN")

                        // especialidades
                        .requestMatchers(HttpMethod.GET,    "/v1/especialidades/**").hasAnyRole("ADMIN", "ATENDENTE", "MEDICO")
                        .requestMatchers(HttpMethod.POST,   "/v1/especialidades/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/v1/especialidades/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/v1/especialidades/**").hasRole("ADMIN")

                        // convenios
                        .requestMatchers(HttpMethod.GET,    "/v1/convenios/**").hasAnyRole("ADMIN", "ATENDENTE", "SERVICE")
                        .requestMatchers(HttpMethod.POST,   "/v1/convenios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/v1/convenios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/v1/convenios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH,  "/v1/convenios/**").hasRole("ADMIN")

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
