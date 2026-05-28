package br.edu.imepac.agendamento.consulta;

import br.edu.imepac.agendamento.config.SecurityConfig;
import br.edu.imepac.agendamento.integration.administrativo.AdministrativoClient;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConsultaController.class)
@Import(SecurityConfig.class)
class ConsultaSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    // @EnableJpaAuditing na Application class precisa de JpaMetamodelMappingContext mesmo no slice web
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    // Evita conexao com Keycloak no contexto de teste
    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private ConsultaService consultaService;

    @MockBean
    private AdministrativoClient administrativoClient;

    @MockBean
    private ModelMapper modelMapper;

    @Test
    void semJwt_rotaProtegida_retorna401() throws Exception {
        mockMvc.perform(get("/v1/consultas/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void service_realizarConsulta_passaSegurancaERetorna404() throws Exception {
        // SERVICE passa a seguranca; consultaService retorna empty = 404 (nao 403)
        when(consultaService.realizar(any())).thenReturn(Optional.empty());

        mockMvc.perform(patch("/v1/consultas/1/realizar")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SERVICE"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void admin_realizarConsulta_retorna403() throws Exception {
        // ADMIN nao pode chamar /realizar diretamente — endpoint interno do Outbox
        mockMvc.perform(patch("/v1/consultas/1/realizar")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void atendente_realizarConsulta_retorna403() throws Exception {
        mockMvc.perform(patch("/v1/consultas/1/realizar")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ATENDENTE"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void actuatorHealth_semJwt_naoRetorna401Nem403() throws Exception {
        // Actuator nao e carregado no slice @WebMvcTest (retorna 404 neste contexto)
        // mas confirma que a rota nao esta protegida por autenticacao
        MvcResult result = mockMvc.perform(get("/actuator/health")).andReturn();
        assertThat(result.getResponse().getStatus())
                .isNotEqualTo(HttpStatus.UNAUTHORIZED.value())
                .isNotEqualTo(HttpStatus.FORBIDDEN.value());
    }
}
