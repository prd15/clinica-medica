package br.edu.imepac.commons.entities.atendimento;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class OutboxEventTest {

    private static final int MAX_RETRY = 3;

    @Test
    void pendente_nasceComStatusPendenteEZeroTentativas() {
        OutboxEvent evento = OutboxEvent.pendente("CONSULTA", "1", "CONFIRMACAO_REALIZACAO", "{}");

        assertEquals(OutboxStatus.PENDENTE, evento.getStatus());
        assertEquals(0, evento.getTentativas());
        // createdAt e preenchido pelo @PrePersist do AuditingEntityListener no flush — fora deste unit test
        assertNull(evento.getProcessadoEm(), "processadoEm so e preenchido em estado terminal");
        assertEquals("CONSULTA", evento.getAggregateType());
        assertEquals("1", evento.getAggregateId());
        assertEquals("CONFIRMACAO_REALIZACAO", evento.getEventType());
    }

    @Test
    void marcarProcessado_defineStatusEProcessadoEm() {
        OutboxEvent evento = OutboxEvent.pendente("CONSULTA", "1", "CONFIRMACAO_REALIZACAO", "{}");

        evento.marcarProcessado();

        assertEquals(OutboxStatus.PROCESSADO, evento.getStatus());
        assertNotNull(evento.getProcessadoEm());
    }

    @Test
    void registrarFalha_dentroDoLimite_incrementaTentativasEMantemFalha() {
        OutboxEvent evento = OutboxEvent.pendente("CONSULTA", "1", "CONFIRMACAO_REALIZACAO", "{}");

        evento.registrarFalha(MAX_RETRY);

        assertEquals(1, evento.getTentativas());
        assertEquals(OutboxStatus.FALHA, evento.getStatus());
        assertNull(evento.getProcessadoEm(), "falha transitoria nao e terminal — processadoEm fica null");
    }

    @Test
    void registrarFalha_atingindoLimite_promoveParaDescartado() {
        OutboxEvent evento = OutboxEvent.pendente("CONSULTA", "1", "CONFIRMACAO_REALIZACAO", "{}");

        evento.registrarFalha(MAX_RETRY); // tentativa 1
        evento.registrarFalha(MAX_RETRY); // tentativa 2
        evento.registrarFalha(MAX_RETRY); // tentativa 3 — esgotou

        assertEquals(3, evento.getTentativas());
        assertEquals(OutboxStatus.DESCARTADO, evento.getStatus(), "esgotou retries — vai pra DESCARTADO");
        assertNotNull(evento.getProcessadoEm(), "DESCARTADO e terminal — registra processadoEm");
    }

    @Test
    void descartar_marcaTerminalSemIncrementarTentativas() {
        OutboxEvent evento = OutboxEvent.pendente("CONSULTA", "1", "EVENTO_DESCONHECIDO", "{}");

        evento.descartar();

        assertEquals(OutboxStatus.DESCARTADO, evento.getStatus());
        assertEquals(0, evento.getTentativas(), "descarte direto nao conta tentativa");
        assertNotNull(evento.getProcessadoEm());
    }
}
