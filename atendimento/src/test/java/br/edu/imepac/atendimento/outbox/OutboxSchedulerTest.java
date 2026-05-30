package br.edu.imepac.atendimento.outbox;

import br.edu.imepac.atendimento.outbox.OutboxEvent;
import br.edu.imepac.atendimento.outbox.OutboxStatus;
import br.edu.imepac.atendimento.outbox.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxSchedulerTest {

    private static final int MAX_RETRY = 3;
    private static final int BATCH_SIZE = 50;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private OutboxEventProcessor processor;

    private OutboxScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new OutboxScheduler(outboxEventRepository, processor, MAX_RETRY, BATCH_SIZE);
    }

    private OutboxEvent evento(String aggregateId, String eventType) {
        return OutboxEvent.pendente("CONSULTA", aggregateId, eventType, "{\"consultaId\":" + aggregateId + "}");
    }

    @Test
    void delegaProcessamentoAoProcessorParaCadaEventoDoBatch() {
        // IDs distintos sao essenciais — OutboxEvent tem @EqualsAndHashCode(of="id"),
        // sem id duas instancias diferentes contam como iguais no verify do Mockito
        OutboxEvent ev1 = evento("5", "CONFIRMACAO_REALIZACAO");
        ev1.setId(101L);
        OutboxEvent ev2 = evento("7", "CONFIRMACAO_REALIZACAO");
        ev2.setId(102L);
        when(outboxEventRepository.buscarParaProcessar(any(), eq(MAX_RETRY), any()))
                .thenReturn(List.of(ev1, ev2));

        scheduler.processarPendentes();

        verify(processor).processar(ev1);
        verify(processor).processar(ev2);
    }

    @Test
    void listaVazia_naoChamaProcessor() {
        when(outboxEventRepository.buscarParaProcessar(any(), eq(MAX_RETRY), any()))
                .thenReturn(List.of());

        scheduler.processarPendentes();

        verify(processor, never()).processar(any());
    }

    @Test
    void buscaApenasPendentesEFalhasDentroDoLimiteDeRetry() {
        when(outboxEventRepository.buscarParaProcessar(any(), eq(MAX_RETRY), any()))
                .thenReturn(List.of());

        scheduler.processarPendentes();

        // PROCESSADO/DESCARTADO nunca entram na busca; respeita o limite de retry e o batch limitado
        verify(outboxEventRepository, times(1)).buscarParaProcessar(
                eq(List.of(OutboxStatus.PENDENTE, OutboxStatus.FALHA)), eq(MAX_RETRY),
                eq(PageRequest.of(0, BATCH_SIZE)));
    }
}
