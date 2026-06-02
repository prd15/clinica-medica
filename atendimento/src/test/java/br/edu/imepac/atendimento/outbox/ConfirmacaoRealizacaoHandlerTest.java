package br.edu.imepac.atendimento.outbox;

import br.edu.imepac.atendimento.integration.agendamento.AgendamentoClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ConfirmacaoRealizacaoHandlerTest {

    @Mock
    private AgendamentoClient agendamentoClient;

    private ConfirmacaoRealizacaoHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ConfirmacaoRealizacaoHandler(agendamentoClient);
    }

    private OutboxEvent evento(String aggregateId) {
        return OutboxEvent.pendente(
                "CONSULTA", aggregateId, ConfirmacaoRealizacaoHandler.EVENT_TYPE,
                "{\"consultaId\":" + aggregateId + "}");
    }

    @Test
    void eventType_retornaConstanteEsperada() {
        assertEquals("CONFIRMACAO_REALIZACAO", handler.eventType());
    }

    @Test
    void handle_chamaConfirmarRealizacaoComConsultaIdExtraidoDoAggregateId() {
        OutboxEvent ev = evento("42");

        handler.handle(ev);

        verify(agendamentoClient).confirmarRealizacao(42L);
    }

    @Test
    void handle_propagaEventoPermanenteExceptionDoAdapter() {
        // adapter ja traduz 404/409 do Feign em EventoPermanenteException.
        // handler nao trata — propaga pro processor decidir DESCARTADO.
        OutboxEvent ev = evento("8");
        doThrow(new EventoPermanenteException("consulta nao existe (404)"))
                .when(agendamentoClient).confirmarRealizacao(8L);

        EventoPermanenteException erro = assertThrows(
                EventoPermanenteException.class, () -> handler.handle(ev));
        assertEquals("consulta nao existe (404)", erro.getMessage());
    }

    @Test
    void handle_propagaExceptionGenericaDoAdapter() {
        // timeout, 5xx, connection refused continuam como excecao generica;
        // processor trata como falha transitoria.
        OutboxEvent ev = evento("11");
        doThrow(new RuntimeException("agendamento indisponivel"))
                .when(agendamentoClient).confirmarRealizacao(11L);

        RuntimeException erro = assertThrows(RuntimeException.class, () -> handler.handle(ev));
        assertEquals("agendamento indisponivel", erro.getMessage());
    }

    @Test
    void handle_aggregateIdInvalido_lancaNumberFormatExceptionAntesDeChamarOAdapter() {
        // payload corrompido — handler nao tenta consertar, deixa propagar.
        // Processor trata como transitoria (registrarFalha incrementa tentativas).
        OutboxEvent ev = evento("abc");

        assertThrows(NumberFormatException.class, () -> handler.handle(ev));
        verify(agendamentoClient, org.mockito.Mockito.never()).confirmarRealizacao(anyLong());
    }
}
