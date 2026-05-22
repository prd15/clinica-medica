package br.edu.imepac.atendimento.outbox;

import br.edu.imepac.atendimento.clients.AgendamentoClient;
import br.edu.imepac.commons.entities.atendimento.OutboxEvent;
import br.edu.imepac.commons.entities.atendimento.OutboxStatus;
import br.edu.imepac.commons.repositories.atendimento.OutboxEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Entrega os eventos do outbox ao agendamento, com retry. Roda em intervalo fixo
// e so reprocessa eventos PENDENTE/FALHA que ainda nao esgotaram o limite de tentativas.
// Cada evento e isolado: uma falha (ex.: payload invalido) marca aquele evento como
// FALHA e nao impede a entrega dos demais.
@Slf4j
@Component
public class OutboxScheduler {

    static final String EVENT_CONFIRMACAO_REALIZACAO = "CONFIRMACAO_REALIZACAO";

    private final OutboxEventRepository outboxEventRepository;
    private final AgendamentoClient agendamentoClient;
    private final int maxRetry;

    public OutboxScheduler(OutboxEventRepository outboxEventRepository,
                           AgendamentoClient agendamentoClient,
                           @Value("${outbox.max-retry:3}") int maxRetry) {
        this.outboxEventRepository = outboxEventRepository;
        this.agendamentoClient = agendamentoClient;
        this.maxRetry = maxRetry;
    }

    @Scheduled(fixedDelayString = "${outbox.poll-interval-ms:10000}")
    @Transactional
    public void processarPendentes() {
        List<OutboxEvent> eventos = outboxEventRepository.findByStatusInAndTentativasLessThan(
                List.of(OutboxStatus.PENDENTE, OutboxStatus.FALHA), maxRetry);
        if (eventos.isEmpty()) {
            return;
        }
        for (OutboxEvent evento : eventos) {
            try {
                entregar(evento);
                evento.marcarProcessado();
            } catch (Exception e) {
                evento.registrarFalha();
                log.error("Falha ao entregar evento outbox id={} eventType={} aggregateId={} tentativa={}/{}: {}",
                        evento.getId(), evento.getEventType(), evento.getAggregateId(),
                        evento.getTentativas(), maxRetry, e.getMessage());
            }
            outboxEventRepository.save(evento);
        }
    }

    private void entregar(OutboxEvent evento) {
        if (EVENT_CONFIRMACAO_REALIZACAO.equals(evento.getEventType())) {
            // aggregateId carrega o consultaId que deve ser marcado como REALIZADA
            agendamentoClient.confirmarRealizacao(Long.valueOf(evento.getAggregateId()));
        } else {
            throw new IllegalStateException("Tipo de evento outbox desconhecido: " + evento.getEventType());
        }
    }
}
