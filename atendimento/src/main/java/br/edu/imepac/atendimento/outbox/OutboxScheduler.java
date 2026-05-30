package br.edu.imepac.atendimento.outbox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// Drena o outbox em batches. A query usa lock pessimista com SKIP LOCKED — replicas
// concorrentes do scheduler trabalham em eventos disjuntos (sem dupla entrega).
// Cada evento e processado pelo OutboxEventProcessor na mesma transacao do scheduler
// (NAO usa REQUIRES_NEW — ver justificativa no OutboxEventProcessor).
@Slf4j
@Component
public class OutboxScheduler {

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxEventProcessor processor;
    private final int maxRetry;
    private final int batchSize;
    private final int retentionDays;

    public OutboxScheduler(OutboxEventRepository outboxEventRepository,
                           OutboxEventProcessor processor,
                           @Value("${outbox.max-retry:3}") int maxRetry,
                           @Value("${outbox.batch-size:50}") int batchSize,
                           @Value("${outbox.retention-days:7}") int retentionDays) {
        this.outboxEventRepository = outboxEventRepository;
        this.processor = processor;
        this.maxRetry = maxRetry;
        this.batchSize = batchSize;
        this.retentionDays = retentionDays;
    }

    // @Transactional sustenta o LOCK PESSIMISTIC_WRITE da query buscarParaProcessar
    // ate o final do batch — outras replicas pulam esses eventos via SKIP LOCKED.
    // Os updates de status do processor ocorrem nesta mesma transacao (sem REQUIRES_NEW).
    @Scheduled(fixedDelayString = "${outbox.poll-interval-ms:10000}")
    @Transactional
    public void processarPendentes() {
        List<OutboxEvent> eventos = outboxEventRepository.buscarParaProcessar(
                List.of(OutboxStatus.PENDENTE, OutboxStatus.FALHA), maxRetry,
                PageRequest.of(0, batchSize));
        if (eventos.isEmpty()) {
            return;
        }
        for (OutboxEvent evento : eventos) {
            processor.processar(evento);
        }
    }

    // housekeeping: remove eventos terminais antigos para a tabela nao crescer indefinidamente.
    // Cadencia propria (default diaria); separada do poll de entrega.
    @Scheduled(fixedDelayString = "${outbox.purge-interval-ms:86400000}")
    @Transactional
    public void purgarTerminais() {
        LocalDateTime limite = LocalDateTime.now().minusDays(retentionDays);
        int removidos = outboxEventRepository.purgarTerminaisAntesDe(
                List.of(OutboxStatus.PROCESSADO, OutboxStatus.DESCARTADO), limite);
        if (removidos > 0) {
            log.info("Outbox housekeeping: {} eventos terminais removidos (anteriores a {})",
                    removidos, limite);
        }
    }
}
