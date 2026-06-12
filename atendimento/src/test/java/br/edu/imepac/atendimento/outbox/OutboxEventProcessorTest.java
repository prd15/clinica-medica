package br.edu.imepac.atendimento.outbox;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxEventProcessorTest {

    private static final int MAX_RETRY = 3;
    private static final String EVENT_TYPE_CONHECIDO = "CONFIRMACAO_REALIZACAO";

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private OutboxEventHandler handler;

    private OutboxEventProcessor processor;

    @BeforeEach
    void setUp() {
        when(handler.eventType()).thenReturn(EVENT_TYPE_CONHECIDO);
        processor = new OutboxEventProcessor(outboxEventRepository, List.of(handler), MAX_RETRY);
    }

    private OutboxEvent evento(String aggregateId, String eventType) {
        return OutboxEvent.pendente("CONSULTA", aggregateId, eventType, "{\"consultaId\":" + aggregateId + "}");
    }

    @Test
    void entregaComSucesso_marcaProcessadoESalva() {
        OutboxEvent ev = evento("5", EVENT_TYPE_CONHECIDO);

        processor.processar(ev);

        verify(handler).handle(ev);
        assertEquals(OutboxStatus.PROCESSADO, ev.getStatus());
        verify(outboxEventRepository).save(ev);
    }

    @Test
    void falhaTransitoria_incrementaTentativaEMantemFalha() {
        OutboxEvent ev = evento("7", EVENT_TYPE_CONHECIDO);
        doThrow(new RuntimeException("rede caiu")).when(handler).handle(ev);

        processor.processar(ev);

        assertEquals(OutboxStatus.FALHA, ev.getStatus());
        assertEquals(1, ev.getTentativas());
        verify(outboxEventRepository).save(ev);
    }

    @Test
    void falhaTransitoria_esgotandoLimite_marcaDescartado() {
        OutboxEvent ev = evento("7", EVENT_TYPE_CONHECIDO);
        ev.setTentativas(MAX_RETRY - 1); // proxima falha esgota
        doThrow(new RuntimeException("rede caiu")).when(handler).handle(ev);

        processor.processar(ev);

        assertEquals(OutboxStatus.DESCARTADO, ev.getStatus());
        assertEquals(MAX_RETRY, ev.getTentativas());
    }

    @Test
    void erroPermanenteDoHandler_marcaDescartadoSemIncrementarTentativas() {
        OutboxEvent ev = evento("8", EVENT_TYPE_CONHECIDO);
        doThrow(new EventoPermanenteException("consulta nao existe (404)"))
                .when(handler).handle(ev);

        processor.processar(ev);

        assertEquals(OutboxStatus.DESCARTADO, ev.getStatus());
        assertEquals(0, ev.getTentativas(), "permanente nao consome tentativa");
        verify(outboxEventRepository).save(ev);
    }

    @Test
    void tipoDeEventoSemHandler_descartaSemChamarHandler() {
        OutboxEvent ev = evento("3", "EVENTO_QUE_NAO_EXISTE");

        processor.processar(ev);

        assertEquals(OutboxStatus.DESCARTADO, ev.getStatus());
        assertEquals(0, ev.getTentativas());
        verify(handler, never()).handle(any());
        verify(outboxEventRepository).save(ev);
    }

    @Test
    void listaVaziaDeHandlers_processarMarcaTodosComoDescartado() {
        // sanity: processor sem handlers (configuracao errada) faz todos os
        // eventos cairem em EventoPermanenteException interno -> DESCARTADO.
        // O warn de inicializacao avisa, mas a aplicacao nao falha no startup.
        OutboxEventProcessor semHandlers = new OutboxEventProcessor(
                outboxEventRepository, List.of(), MAX_RETRY);
        OutboxEvent ev = evento("9", EVENT_TYPE_CONHECIDO);

        semHandlers.processar(ev);

        assertEquals(OutboxStatus.DESCARTADO, ev.getStatus());
        assertEquals(0, ev.getTentativas());
        verify(outboxEventRepository).save(ev);
    }
}
