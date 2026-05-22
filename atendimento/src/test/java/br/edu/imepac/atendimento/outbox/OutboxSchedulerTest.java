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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxSchedulerTest {

    private static final int MAX_RETRY = 3;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private AgendamentoClient agendamentoClient;

    private OutboxScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new OutboxScheduler(outboxEventRepository, agendamentoClient, MAX_RETRY);
    }

    private OutboxEvent evento(String aggregateId, String eventType) {
        return OutboxEvent.pendente("CONSULTA", aggregateId, eventType, "{\"consultaId\":" + aggregateId + "}");
    }

    @Test
    void entregaComSucesso_marcaProcessado() {
        OutboxEvent ev = evento("5", "CONFIRMACAO_REALIZACAO");
        when(outboxEventRepository.findByStatusInAndTentativasLessThan(any(), eq(MAX_RETRY)))
                .thenReturn(List.of(ev));

        scheduler.processarPendentes();

        verify(agendamentoClient).confirmarRealizacao(5L);
        assertEquals(OutboxStatus.PROCESSADO, ev.getStatus());
        verify(outboxEventRepository).save(ev);
    }

    @Test
    void falhaNaEntrega_incrementaTentativaEMantemFalha() {
        OutboxEvent ev = evento("7", "CONFIRMACAO_REALIZACAO");
        when(outboxEventRepository.findByStatusInAndTentativasLessThan(any(), eq(MAX_RETRY)))
                .thenReturn(List.of(ev));
        doThrow(new RuntimeException("rede caiu")).when(agendamentoClient).confirmarRealizacao(7L);

        scheduler.processarPendentes();

        assertEquals(OutboxStatus.FALHA, ev.getStatus());
        assertEquals(1, ev.getTentativas());
        verify(outboxEventRepository).save(ev);
    }

    @Test
    void aggregateIdInvalido_marcaFalhaSemTravarOsDemais() {
        OutboxEvent ruim = evento("abc", "CONFIRMACAO_REALIZACAO");
        OutboxEvent bom = evento("9", "CONFIRMACAO_REALIZACAO");
        when(outboxEventRepository.findByStatusInAndTentativasLessThan(any(), eq(MAX_RETRY)))
                .thenReturn(List.of(ruim, bom));

        scheduler.processarPendentes();

        assertEquals(OutboxStatus.FALHA, ruim.getStatus());
        assertEquals(OutboxStatus.PROCESSADO, bom.getStatus());
        verify(agendamentoClient).confirmarRealizacao(9L);
        verify(outboxEventRepository, times(2)).save(any(OutboxEvent.class));
    }

    @Test
    void tipoDeEventoDesconhecido_marcaFalha() {
        OutboxEvent ev = evento("3", "EVENTO_QUE_NAO_EXISTE");
        when(outboxEventRepository.findByStatusInAndTentativasLessThan(any(), eq(MAX_RETRY)))
                .thenReturn(List.of(ev));

        scheduler.processarPendentes();

        assertEquals(OutboxStatus.FALHA, ev.getStatus());
        verify(agendamentoClient, never()).confirmarRealizacao(anyLong());
    }

    @Test
    void listaVazia_naoChamaClienteNemSalva() {
        when(outboxEventRepository.findByStatusInAndTentativasLessThan(any(), eq(MAX_RETRY)))
                .thenReturn(List.of());

        scheduler.processarPendentes();

        verify(agendamentoClient, never()).confirmarRealizacao(anyLong());
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void buscaApenasPendentesEFalhasDentroDoLimiteDeRetry() {
        when(outboxEventRepository.findByStatusInAndTentativasLessThan(any(), eq(MAX_RETRY)))
                .thenReturn(List.of());

        scheduler.processarPendentes();

        // garante que PROCESSADO nunca entra na busca e que o limite de retry e respeitado
        verify(outboxEventRepository).findByStatusInAndTentativasLessThan(
                eq(List.of(OutboxStatus.PENDENTE, OutboxStatus.FALHA)), eq(MAX_RETRY));
    }
}
