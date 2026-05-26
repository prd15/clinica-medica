package br.edu.imepac.commons.entities.atendimento;

// ciclo de vida de um evento do outbox:
// PENDENTE  -> nasce aqui, ainda nao foi entregue
// PROCESSADO -> entrega bem-sucedida (terminal)
// FALHA      -> erro transitorio (timeout, 5xx) — sera reprocessado ate o limite de tentativas
// DESCARTADO -> erro permanente (404, 409, payload invalido, esgotou tentativas) — terminal, nao retenta
public enum OutboxStatus {
    PENDENTE,
    PROCESSADO,
    FALHA,
    DESCARTADO
}
