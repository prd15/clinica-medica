package br.edu.imepac.administrativo.atendente;

import br.edu.imepac.administrativo.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AtendenteController.class)
@Import(SecurityConfig.class)
class AtendenteSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    // @EnableJpaAuditing na Application class precisa de JpaMetamodelMappingContext mesmo no slice web
    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private AtendenteService atendenteService;

    @MockBean
    private ModelMapper modelMapper;

    @Test
    void semJwt_rotaProtegida_retorna401() throws Exception {
        mockMvc.perform(get("/v1/atendentes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void atendente_naoAcessaAtendentes_retorna403() throws Exception {
        // ATENDENTE nao tem permissao para gerenciar atendentes — rota exclusiva do ADMIN
        mockMvc.perform(get("/v1/atendentes")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ATENDENTE"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void medico_naoAcessaAtendentes_retorna403() throws Exception {
        mockMvc.perform(get("/v1/atendentes")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_MEDICO"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_acessaAtendentes_retorna200() throws Exception {
        when(atendenteService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/v1/atendentes")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }
}
