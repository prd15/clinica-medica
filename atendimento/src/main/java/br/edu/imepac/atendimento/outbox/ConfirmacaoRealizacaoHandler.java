package br.edu.imepac.atendimento.outbox;

import br.edu.imepac.atendimento.integration.agendamento.AgendamentoClient;
import org.springframework.stereotype.Component;

// Handler do eventType "CONFIRMACAO_REALIZACAO": notifica o agendamento que a
// consulta foi realizada. Depende do adapter AgendamentoClient (que ja traduz
// 404 e 409 em EventoPermanenteException) — handler nao repete esse tratamento.
//
// Nesse commit o handler vive ao lado do OutboxEventProcessor antigo, que
// continua tendo a logica hardcoded. O processor passa a usar o handler apenas
// no commit seguinte.
@Component
class ConfirmacaoRealizacaoHandler implements OutboxEventHandler {

    static final String EVENT_TYPE = "CONFIRMACAO_REALIZACAO";

    private final AgendamentoClient agendamentoClient;

    ConfirmacaoRealizacaoHandler(AgendamentoClient agendamentoClient) {
        this.agendamentoClient = agendamentoClient;
    }

    @Override
    public String eventType() {
        return EVENT_TYPE;
    }

    @Override
    public void handle(OutboxEvent evento) {
        // aggregateId carrega o consultaId que deve ser marcado como REALIZADA
        agendamentoClient.confirmarRealizacao(Long.valueOf(evento.getAggregateId()));
    }
}
