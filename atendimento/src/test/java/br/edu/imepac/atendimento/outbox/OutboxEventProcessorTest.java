package br.edu.imepac.atendimento.outbox;

import br.edu.imepac.atendimento.clients.AgendamentoClient;
import br.edu.imepac.commons.entities.atendimento.OutboxEvent;
import br.edu.imepac.commons.entities.atendimento.OutboxStatus;
import br.edu.imepac.commons.repositories.atendimento.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OutboxEventProcessorTest {

    private static final int MAX_RETRY = 3;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private AgendamentoClient agendamentoClient;

    private OutboxEventProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new OutboxEventProcessor(outboxEventRepository, agendamentoClient, MAX_RETRY);
    }

    private OutboxEvent evento(String aggregateId, String eventType) {
        return OutboxEvent.pendente("CONSULTA", aggregateId, eventType, "{\"consultaId\":" + aggregateId + "}");
    }

    @Test
    void entregaComSucesso_marcaProcessadoESalva() {
        OutboxEvent ev = evento("5", "CONFIRMACAO_REALIZACAO");

        processor.processar(ev);

        verify(agendamentoClient).confirmarRealizacao(5L);
        assertEquals(OutboxStatus.PROCESSADO, ev.getStatus());
        verify(outboxEventRepository).save(ev);
    }

    @Test
    void falhaTransitoria_incrementaTentativaEMantemFalha() {
        OutboxEvent ev = evento("7", "CONFIRMACAO_REALIZACAO");
        doThrow(new RuntimeException("rede caiu")).when(agendamentoClient).confirmarRealizacao(7L);

        processor.processar(ev);

        assertEquals(OutboxStatus.FALHA, ev.getStatus());
        assertEquals(1, ev.getTentativas());
        verify(outboxEventRepository).save(ev);
    }

    @Test
    void falhaTransitoria_esgotandoLimite_marcaDescartado() {
        OutboxEvent ev = evento("7", "CONFIRMACAO_REALIZACAO");
        ev.setTentativas(MAX_RETRY - 1); // proxima falha esgota
        doThrow(new RuntimeException("rede caiu")).when(agendamentoClient).confirmarRealizacao(7L);

        processor.processar(ev);

        assertEquals(OutboxStatus.DESCARTADO, ev.getStatus());
        assertEquals(MAX_RETRY, ev.getTentativas());
    }

    @Test
    void erroPermanenteDoClient_marcaDescartadoSemIncrementarTentativas() {
        OutboxEvent ev = evento("8", "CONFIRMACAO_REALIZACAO");
        doThrow(new EventoPermanenteException("consulta nao existe (404)"))
                .when(agendamentoClient).confirmarRealizacao(8L);

        processor.processar(ev);

        assertEquals(OutboxStatus.DESCARTADO, ev.getStatus());
        assertEquals(0, ev.getTentativas(), "permanente nao consome tentativa");
        verify(outboxEventRepository).save(ev);
    }

    @Test
    void tipoDeEventoDesconhecido_descartaSemTentarEntregar() {
        OutboxEvent ev = evento("3", "EVENTO_QUE_NAO_EXISTE");

        processor.processar(ev);

        assertEquals(OutboxStatus.DESCARTADO, ev.getStatus());
        assertEquals(0, ev.getTentativas());
        verify(agendamentoClient, never()).confirmarRealizacao(anyLong());
        verify(outboxEventRepository).save(ev);
    }

    @Test
    void aggregateIdInvalido_marcaFalhaTransitoria() {
        // Long.valueOf("abc") joga NumberFormatException — generica, vai como transitoria
        OutboxEvent ev = evento("abc", "CONFIRMACAO_REALIZACAO");

        processor.processar(ev);

        assertEquals(OutboxStatus.FALHA, ev.getStatus());
        assertEquals(1, ev.getTentativas());
        verify(outboxEventRepository).save(ev);
    }
}
