package br.edu.imepac.atendimento.integration.agendamento;

import br.edu.imepac.atendimento.integration.agendamento.dto.ConsultaRefDTO;
import br.edu.imepac.atendimento.outbox.EventoPermanenteException;
import br.edu.imepac.commons.exceptions.ServicoIndisponivelException;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendamentoClientTest {

    @Mock
    private AgendamentoFeignClient feignClient;

    private AgendamentoClient client;

    @BeforeEach
    void setUp() {
        client = new AgendamentoClient(feignClient);
    }

    @Test
    void buscarConsulta_existente_retornaOptional() {
        ConsultaRefDTO dto = new ConsultaRefDTO();
        dto.setId(1L);
        when(feignClient.buscarConsulta(1L)).thenReturn(dto);

        Optional<ConsultaRefDTO> resultado = client.buscarConsulta(1L);

        assertTrue(resultado.isPresent());
    }

    @Test
    void buscarConsulta_404_retornaEmpty() {
        when(feignClient.buscarConsulta(99L)).thenThrow(mock(FeignException.NotFound.class));

        Optional<ConsultaRefDTO> resultado = client.buscarConsulta(99L);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void buscarConsulta_falhaRede_lancaServicoIndisponivel() {
        when(feignClient.buscarConsulta(1L)).thenThrow(mock(FeignException.ServiceUnavailable.class));

        assertThrows(ServicoIndisponivelException.class, () -> client.buscarConsulta(1L));
    }

    @Test
    void confirmarRealizacao_sucesso_semExcecao() {
        doNothing().when(feignClient).confirmarRealizacao(1L);

        client.confirmarRealizacao(1L);
    }

    @Test
    void confirmarRealizacao_404_lancaEventoPermanente() {
        doThrow(mock(FeignException.NotFound.class)).when(feignClient).confirmarRealizacao(1L);

        assertThrows(EventoPermanenteException.class, () -> client.confirmarRealizacao(1L));
    }

    @Test
    void confirmarRealizacao_409_lancaEventoPermanente() {
        doThrow(mock(FeignException.Conflict.class)).when(feignClient).confirmarRealizacao(1L);

        assertThrows(EventoPermanenteException.class, () -> client.confirmarRealizacao(1L));
    }

    @Test
    void confirmarRealizacao_falhaRede_propagaFeignException() {
        // outbox trata como transitorio — nao deve virar EventoPermanenteException
        doThrow(mock(FeignException.ServiceUnavailable.class)).when(feignClient).confirmarRealizacao(1L);

        assertThrows(FeignException.class, () -> client.confirmarRealizacao(1L));
    }
}
