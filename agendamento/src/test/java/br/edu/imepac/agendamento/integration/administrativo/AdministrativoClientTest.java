package br.edu.imepac.agendamento.integration.administrativo;

import br.edu.imepac.agendamento.integration.administrativo.dto.ConvenioRefDTO;
import br.edu.imepac.agendamento.integration.administrativo.dto.MedicoRefDTO;
import br.edu.imepac.agendamento.integration.administrativo.dto.PacienteRefDTO;
import br.edu.imepac.commons.exceptions.ServicoIndisponivelException;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdministrativoClientTest {

    @Mock
    private AdministrativoFeignClient feignClient;

    private AdministrativoClient client;

    @BeforeEach
    void setUp() {
        client = new AdministrativoClient(feignClient);
    }

    @Test
    void buscarConvenio_existente_retornaOptionalComBody() {
        ConvenioRefDTO dto = new ConvenioRefDTO();
        dto.setId(1L);
        dto.setAtivo(true);
        when(feignClient.buscarConvenio(1L)).thenReturn(dto);

        Optional<ConvenioRefDTO> resultado = client.buscarConvenio(1L);

        assertTrue(resultado.isPresent());
        assertTrue(resultado.get().getAtivo());
    }

    @Test
    void buscarConvenio_404_retornaOptionalEmpty() {
        when(feignClient.buscarConvenio(99L)).thenThrow(mock(FeignException.NotFound.class));

        Optional<ConvenioRefDTO> resultado = client.buscarConvenio(99L);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void buscarConvenio_falhaRede_lancaServicoIndisponivel() {
        when(feignClient.buscarConvenio(1L)).thenThrow(mock(FeignException.ServiceUnavailable.class));

        assertThrows(ServicoIndisponivelException.class, () -> client.buscarConvenio(1L));
    }

    @Test
    void isConvenioAtivo_convenioAtivo_retornaTrue() {
        ConvenioRefDTO dto = new ConvenioRefDTO();
        dto.setAtivo(true);
        when(feignClient.buscarConvenio(1L)).thenReturn(dto);

        assertTrue(client.isConvenioAtivo(1L));
    }

    @Test
    void isConvenioAtivo_convenioInativo_retornaFalse() {
        ConvenioRefDTO dto = new ConvenioRefDTO();
        dto.setAtivo(false);
        when(feignClient.buscarConvenio(1L)).thenReturn(dto);

        assertFalse(client.isConvenioAtivo(1L));
    }

    @Test
    void isConvenioAtivo_404_retornaFalse() {
        when(feignClient.buscarConvenio(99L)).thenThrow(mock(FeignException.NotFound.class));

        assertFalse(client.isConvenioAtivo(99L));
    }

    @Test
    void isConvenioAtivo_falhaRede_propagaServicoIndisponivel() {
        // crucial: falha de rede NAO deve mascarar como "convenio inativo"
        when(feignClient.buscarConvenio(1L)).thenThrow(mock(FeignException.ServiceUnavailable.class));

        assertThrows(ServicoIndisponivelException.class, () -> client.isConvenioAtivo(1L));
    }

    @Test
    void isMedicoAtivo_medicoAtivo_retornaTrue() {
        MedicoRefDTO dto = new MedicoRefDTO();
        dto.setAtivo(true);
        when(feignClient.buscarMedico(3L)).thenReturn(dto);

        assertTrue(client.isMedicoAtivo(3L));
    }

    @Test
    void isPacienteExistente_paciente404_retornaFalse() {
        when(feignClient.buscarPaciente(77L)).thenThrow(mock(FeignException.NotFound.class));

        assertFalse(client.isPacienteExistente(77L));
    }

    @Test
    void isPacienteExistente_pacienteExiste_retornaTrue() {
        PacienteRefDTO dto = new PacienteRefDTO();
        dto.setId(7L);
        when(feignClient.buscarPaciente(7L)).thenReturn(dto);

        assertTrue(client.isPacienteExistente(7L));
    }
}
