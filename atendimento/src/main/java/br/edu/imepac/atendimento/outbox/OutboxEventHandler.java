package br.edu.imepac.atendimento.outbox;

// Estrategia de entrega para UM tipo de evento outbox. O OutboxEventProcessor
// roteia o evento para o handler cujo eventType() bate com o evento.getEventType().
//
// Contrato:
// - eventType(): string constante e unica por implementacao. Usada como chave
//   no Map<String, OutboxEventHandler> do processor.
// - handle(evento): tenta entregar. Lanca EventoPermanenteException pra erros
//   irrecuperaveis (DESCARTADO direto). Qualquer outra excecao e' tratada como
//   transitoria (registrarFalha incrementa tentativas).
//
// ESCOPO RESTRITO (package-private): alinha com OutboxEventProcessor, que ja' e'
// package-private por decisao arquitetural (deve ser usado apenas pelo
// OutboxScheduler do mesmo pacote, dentro do lock pessimista). Handlers vivem
// no mesmo pacote.
interface OutboxEventHandler {

    String eventType();

    void handle(OutboxEvent evento);
}
