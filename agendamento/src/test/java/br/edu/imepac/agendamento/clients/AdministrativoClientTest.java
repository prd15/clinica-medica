package br.edu.imepac.agendamento.clients;

import br.edu.imepac.agendamento.clients.dto.ConvenioRefDTO;
import br.edu.imepac.agendamento.clients.dto.MedicoRefDTO;
import br.edu.imepac.agendamento.clients.dto.PacienteRefDTO;
import br.edu.imepac.commons.exceptions.ServicoIndisponivelException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdministrativoClientTest {

    @Mock
    private RestTemplate restTemplate;

    private AdministrativoClient client;

    @BeforeEach
    void setUp() {
        client = new AdministrativoClient(restTemplate, "http://admin:8081");
    }

    @Test
    void buscarConvenio_existente_retornaOptionalComBody() {
        ConvenioRefDTO dto = new ConvenioRefDTO();
        dto.setId(1L);
        dto.setAtivo(true);
        when(restTemplate.getForObject(contains("/v1/convenios/1"), eq(ConvenioRefDTO.class)))
                .thenReturn(dto);

        Optional<ConvenioRefDTO> resultado = client.buscarConvenio(1L);

        assertTrue(resultado.isPresent());
        assertTrue(resultado.get().getAtivo());
    }

    @Test
    void buscarConvenio_404_retornaOptionalEmpty() {
        when(restTemplate.getForObject(contains("/v1/convenios/99"), eq(ConvenioRefDTO.class)))
                .thenThrow(HttpClientErrorException.create(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        "Not Found", null, null, null));

        Optional<ConvenioRefDTO> resultado = client.buscarConvenio(99L);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void buscarConvenio_falhaRede_lancaServicoIndisponivel() {
        when(restTemplate.getForObject(contains("/v1/convenios/1"), eq(ConvenioRefDTO.class)))
                .thenThrow(new ResourceAccessException("connection refused"));

        assertThrows(ServicoIndisponivelException.class,
                () -> client.buscarConvenio(1L));
    }

    @Test
    void isConvenioAtivo_convenioAtivo_retornaTrue() {
        ConvenioRefDTO dto = new ConvenioRefDTO();
        dto.setAtivo(true);
        when(restTemplate.getForObject(contains("/v1/convenios/1"), eq(ConvenioRefDTO.class)))
                .thenReturn(dto);

        assertTrue(client.isConvenioAtivo(1L));
    }

    @Test
    void isConvenioAtivo_convenioInativo_retornaFalse() {
        ConvenioRefDTO dto = new ConvenioRefDTO();
        dto.setAtivo(false);
        when(restTemplate.getForObject(contains("/v1/convenios/1"), eq(ConvenioRefDTO.class)))
                .thenReturn(dto);

        assertFalse(client.isConvenioAtivo(1L));
    }

    @Test
    void isConvenioAtivo_404_retornaFalse() {
        when(restTemplate.getForObject(contains("/v1/convenios/99"), eq(ConvenioRefDTO.class)))
                .thenThrow(HttpClientErrorException.create(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        "Not Found", null, null, null));

        assertFalse(client.isConvenioAtivo(99L));
    }

    @Test
    void isConvenioAtivo_falhaRede_propagaServicoIndisponivel() {
        // crucial: falha de rede NAO deve mascarar como "convenio inativo" — agendar precisa
        // distinguir indisponibilidade real de recurso inativo
        when(restTemplate.getForObject(contains("/v1/convenios/1"), eq(ConvenioRefDTO.class)))
                .thenThrow(new ResourceAccessException("connection refused"));

        assertThrows(ServicoIndisponivelException.class,
                () -> client.isConvenioAtivo(1L));
    }

    @Test
    void isMedicoAtivo_medicoAtivo_retornaTrue() {
        MedicoRefDTO dto = new MedicoRefDTO();
        dto.setAtivo(true);
        when(restTemplate.getForObject(contains("/v1/medicos/3"), eq(MedicoRefDTO.class)))
                .thenReturn(dto);

        assertTrue(client.isMedicoAtivo(3L));
    }

    @Test
    void isPacienteExistente_paciente404_retornaFalse() {
        when(restTemplate.getForObject(contains("/v1/pacientes/77"), eq(PacienteRefDTO.class)))
                .thenThrow(HttpClientErrorException.create(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        "Not Found", null, null, null));

        assertFalse(client.isPacienteExistente(77L));
    }

    @Test
    void isPacienteExistente_pacienteExiste_retornaTrue() {
        PacienteRefDTO dto = new PacienteRefDTO();
        dto.setId(7L);
        when(restTemplate.getForObject(contains("/v1/pacientes/7"), eq(PacienteRefDTO.class)))
                .thenReturn(dto);

        assertTrue(client.isPacienteExistente(7L));
    }
}
