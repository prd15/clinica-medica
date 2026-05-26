package br.edu.imepac.atendimento.outbox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Drena o outbox em batches. Cada evento e processado em transacao propria pelo
// OutboxEventProcessor (REQUIRES_NEW), entao a falha de um nao afeta os outros.
// A query do batch usa lock pessimista com SKIP LOCKED — quando rodam multiplas
// replicas do atendimento (k8s), cada scheduler trabalha em eventos diferentes.
@Slf4j
@Component
public class OutboxScheduler {

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxEventProcessor processor;
    private final int maxRetry;

    public OutboxScheduler(OutboxEventRepository outboxEventRepository,
                           OutboxEventProcessor processor,
                           @Value("${outbox.max-retry:3}") int maxRetry) {
        this.outboxEventRepository = outboxEventRepository;
        this.processor = processor;
        this.maxRetry = maxRetry;
    }

    // @Transactional aqui sustenta o LOCK PESSIMISTIC_WRITE da query buscarParaProcessar
    // ate o final do batch — outras replicas do scheduler pulam estes eventos via SKIP LOCKED.
    // Os updates de status acontecem em transacoes REQUIRES_NEW dentro do processor.
    @Scheduled(fixedDelayString = "${outbox.poll-interval-ms:10000}")
    @Transactional
    public void processarPendentes() {
        List<OutboxEvent> eventos = outboxEventRepository.buscarParaProcessar(
                List.of(OutboxStatus.PENDENTE, OutboxStatus.FALHA), maxRetry);
        if (eventos.isEmpty()) {
            return;
        }
        for (OutboxEvent evento : eventos) {
            processor.processar(evento);
        }
    }
}
