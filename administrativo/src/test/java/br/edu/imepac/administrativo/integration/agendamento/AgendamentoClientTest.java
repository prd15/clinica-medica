package br.edu.imepac.administrativo.integration.agendamento;

import br.edu.imepac.administrativo.integration.agendamento.dto.ContagemConsultasDTO;
import br.edu.imepac.commons.exceptions.ServicoIndisponivelException;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void contarConsultasPorData_retornaTotal() {
        LocalDate data = LocalDate.of(2026, 5, 26);
        ContagemConsultasDTO dto = new ContagemConsultasDTO();
        dto.setTotal(7L);
        when(feignClient.contarConsultasPorData("2026-05-26")).thenReturn(dto);

        long resultado = client.contarConsultasPorData(data);

        assertEquals(7L, resultado);
    }

    @Test
    void contarConsultasPorData_respostaNull_retornaZero() {
        LocalDate data = LocalDate.of(2026, 5, 26);
        when(feignClient.contarConsultasPorData("2026-05-26")).thenReturn(null);

        long resultado = client.contarConsultasPorData(data);

        assertEquals(0L, resultado);
    }

    @Test
    void contarConsultasPorData_falhaRede_lancaServicoIndisponivel() {
        LocalDate data = LocalDate.of(2026, 5, 26);
        when(feignClient.contarConsultasPorData("2026-05-26"))
                .thenThrow(mock(FeignException.ServiceUnavailable.class));

        assertThrows(ServicoIndisponivelException.class,
                () -> client.contarConsultasPorData(data));
    }
}
