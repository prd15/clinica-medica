package br.edu.imepac.commons.entities.atendimento;

// ciclo de vida de um evento do outbox: nasce PENDENTE, vai a PROCESSADO em sucesso
// ou FALHA quando a entrega erra (e sera reprocessado ate o limite de tentativas)
public enum OutboxStatus {
    PENDENTE,
    PROCESSADO,
    FALHA
}
