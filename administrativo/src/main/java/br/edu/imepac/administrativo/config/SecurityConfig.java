package br.edu.imepac.administrativo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

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
}
