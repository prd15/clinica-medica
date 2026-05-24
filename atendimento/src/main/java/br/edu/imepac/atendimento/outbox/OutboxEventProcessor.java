package br.edu.imepac.atendimento.outbox;

import br.edu.imepac.atendimento.clients.AgendamentoClient;
import br.edu.imepac.commons.entities.atendimento.OutboxEvent;
import br.edu.imepac.commons.repositories.atendimento.OutboxEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// Processa UM evento por vez, no contexto transacional do scheduler (que mantem o
// LOCK PESSIMISTIC_WRITE com SKIP_LOCKED ate o commit do batch — essencial pra
// multi-instancia). Distingue erro PERMANENTE (DESCARTADO direto) de TRANSITORIO (retry).
//
// Decisao deliberada: NAO usar @Transactional REQUIRES_NEW aqui porque conflita com
// o lock pessimista da outer tx (inner tx bloquearia na mesma linha). Falha catastrofica
// num evento (ex.: DataIntegrityViolationException no save) pode causar rollback do
// batch inteiro; aceitavel porque outro scheduler/proxima execucao retoma. O try/catch
// abaixo lida com TODA excecao da entrega (RuntimeException), nao afeta o save.
@Slf4j
@Component
public class OutboxEventProcessor {

    static final String EVENT_CONFIRMACAO_REALIZACAO = "CONFIRMACAO_REALIZACAO";

    private final OutboxEventRepository outboxEventRepository;
    private final AgendamentoClient agendamentoClient;
    private final int maxRetry;

    public OutboxEventProcessor(OutboxEventRepository outboxEventRepository,
                                AgendamentoClient agendamentoClient,
                                @Value("${outbox.max-retry:3}") int maxRetry) {
        this.outboxEventRepository = outboxEventRepository;
        this.agendamentoClient = agendamentoClient;
        this.maxRetry = maxRetry;
    }

    public void processar(OutboxEvent evento) {
        try {
            entregar(evento);
            evento.marcarProcessado();
        } catch (EventoPermanenteException e) {
            // erro irrecuperavel — DESCARTADO sem retry
            evento.descartar();
            log.error("Evento outbox DESCARTADO (erro permanente) id={} eventType={} aggregateId={}: {}",
                    evento.getId(), evento.getEventType(), evento.getAggregateId(), e.getMessage());
        } catch (Exception e) {
            // erro transitorio — registrarFalha incrementa tentativas e promove a DESCARTADO se esgotou
            evento.registrarFalha(maxRetry);
            if (evento.getStatus() == br.edu.imepac.commons.entities.atendimento.OutboxStatus.DESCARTADO) {
                log.error("Evento outbox DESCARTADO (esgotou {} tentativas) id={} eventType={} aggregateId={} ultimo erro: {}",
                        maxRetry, evento.getId(), evento.getEventType(), evento.getAggregateId(), e.getMessage());
            } else {
                log.warn("Falha transitoria entregando evento outbox id={} eventType={} aggregateId={} tentativa={}/{}: {}",
                        evento.getId(), evento.getEventType(), evento.getAggregateId(),
                        evento.getTentativas(), maxRetry, e.getMessage());
            }
        }
        outboxEventRepository.save(evento);
    }

    private void entregar(OutboxEvent evento) {
        if (EVENT_CONFIRMACAO_REALIZACAO.equals(evento.getEventType())) {
            // aggregateId carrega o consultaId que deve ser marcado como REALIZADA
            agendamentoClient.confirmarRealizacao(Long.valueOf(evento.getAggregateId()));
            return;
        }
        throw new EventoPermanenteException("Tipo de evento outbox desconhecido: " + evento.getEventType());
    }
}
