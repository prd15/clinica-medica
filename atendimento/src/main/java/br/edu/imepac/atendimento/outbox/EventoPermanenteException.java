package br.edu.imepac.atendimento.outbox;

// Marca um erro PERMANENTE durante a entrega de um evento outbox. Diferente de
// excecao generica (que aciona retry transitorio), permanente vai direto para
// DESCARTADO sem incrementar tentativas. Usar para 404 (recurso sumiu), 409
// (conflito imutavel — ex.: consulta ja realizada), tipo de evento desconhecido,
// payload invalido. NUNCA usar para timeout, 5xx, connection refused.
public class EventoPermanenteException extends RuntimeException {

    public EventoPermanenteException(String message) {
        super(message);
    }

    public EventoPermanenteException(String message, Throwable cause) {
        super(message, cause);
    }
}
