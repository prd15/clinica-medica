package br.edu.imepac.atendimento.outbox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Processa UM evento por vez, no contexto transacional do scheduler (que mantem o
// LOCK PESSIMISTIC_WRITE com SKIP_LOCKED ate o commit do batch — essencial pra
// multi-instancia). Distingue erro PERMANENTE (DESCARTADO direto) de TRANSITORIO (retry).
//
// ESCOPO RESTRITO (package-private): deve ser injetado/usado APENAS pelo OutboxScheduler
// neste mesmo pacote. Chamar de fora do contexto transacional do scheduler pode perder
// o lock pessimista e abrir janela de dupla entrega em ambiente multi-instancia.
//
// Decisao deliberada: NAO usar @Transactional REQUIRES_NEW aqui porque conflita com
// o lock pessimista da outer tx (inner tx bloquearia na mesma linha). Falha catastrofica
// num evento (ex.: DataIntegrityViolationException no save) pode causar rollback do
// batch inteiro; aceitavel porque outro scheduler/proxima execucao retoma. O try/catch
// abaixo lida com TODA excecao da entrega (RuntimeException), nao afeta o save.
//
// Roteamento por eventType (OCP): processor delega a entrega ao OutboxEventHandler
// cujo eventType() bate com o evento. Tipo desconhecido -> EventoPermanenteException
// -> DESCARTADO. Adicionar tipo novo nao reabre essa classe — basta criar um novo
// @Component package-private que implemente OutboxEventHandler no mesmo pacote.
@Slf4j
@Component
class OutboxEventProcessor {

    private final OutboxEventRepository outboxEventRepository;
    private final Map<String, OutboxEventHandler> handlersPorTipo;
    private final int maxRetry;

    OutboxEventProcessor(OutboxEventRepository outboxEventRepository,
                         List<OutboxEventHandler> handlers,
                         @Value("${outbox.max-retry:3}") int maxRetry) {
        this.outboxEventRepository = outboxEventRepository;
        this.handlersPorTipo = handlers.stream()
                .collect(Collectors.toUnmodifiableMap(
                        OutboxEventHandler::eventType, h -> h));
        this.maxRetry = maxRetry;
        if (this.handlersPorTipo.isEmpty()) {
            log.warn("OutboxEventProcessor inicializado sem nenhum OutboxEventHandler — "
                    + "todos os eventos viraro DESCARTADO. Verifique a configuracao.");
        }
    }

    void processar(OutboxEvent evento) {
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
            if (evento.getStatus() == OutboxStatus.DESCARTADO) {
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
        OutboxEventHandler handler = handlersPorTipo.get(evento.getEventType());
        if (handler == null) {
            throw new EventoPermanenteException(
                    "Tipo de evento outbox desconhecido: " + evento.getEventType());
        }
        handler.handle(evento);
    }
}
