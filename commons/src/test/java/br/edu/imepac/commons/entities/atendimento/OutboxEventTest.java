package br.edu.imepac.commons.entities.atendimento;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class OutboxEventTest {

    @Test
    void pendente_nasceComStatusPendenteEZeroTentativas() {
        OutboxEvent evento = OutboxEvent.pendente("CONSULTA", "1", "CONFIRMACAO_REALIZACAO", "{}");

        assertEquals(OutboxStatus.PENDENTE, evento.getStatus());
        assertEquals(0, evento.getTentativas());
        assertNotNull(evento.getCriadoEm());
        assertNull(evento.getProcessadoEm(), "processadoEm so e preenchido ao processar");
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
    void registrarFalha_incrementaTentativasEMantemSemProcessadoEm() {
        OutboxEvent evento = OutboxEvent.pendente("CONSULTA", "1", "CONFIRMACAO_REALIZACAO", "{}");

        evento.registrarFalha();
        assertEquals(1, evento.getTentativas());
        assertEquals(OutboxStatus.FALHA, evento.getStatus());
        assertNull(evento.getProcessadoEm(), "falha nao preenche processadoEm");

        evento.registrarFalha();
        assertEquals(2, evento.getTentativas());
    }
}
