package br.edu.imepac.atendimento.outbox;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    // eventos elegiveis para entrega: PENDENTE ou FALHA que ainda nao esgotaram as tentativas.
    // PESSIMISTIC_WRITE + SKIP_LOCKED (timeout=-2): se outra instancia do scheduler ja travou
    // certos eventos, esta consulta os pula em vez de bloquear ou explodir. Essencial em k8s
    // com 2+ replicas do atendimento — evita dupla entrega da notificacao.
    // Pageable LIMITA o batch: como a entrega (HTTP Feign) ocorre dentro da transacao que
    // segura o lock, um batch ilimitado prenderia lock + conexao do pool durante toda a I/O.
    // O limite mantem o tempo de lock por poll previsivel; o backlog drena em ciclos seguintes.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("SELECT e FROM OutboxEvent e " +
            "WHERE e.status IN :status AND e.tentativas < :maxTentativas " +
            "ORDER BY e.createdAt")
    List<OutboxEvent> buscarParaProcessar(@Param("status") Collection<OutboxStatus> status,
                                          @Param("maxTentativas") int maxTentativas,
                                          Pageable pageable);

    // housekeeping: remove eventos terminais (PROCESSADO/DESCARTADO) antigos para a tabela
    // nao crescer indefinidamente e o indice (status, tentativas) nao degradar. Bulk delete.
    @Modifying
    @Query("DELETE FROM OutboxEvent e WHERE e.status IN :status AND e.createdAt < :limite")
    int purgarTerminaisAntesDe(@Param("status") Collection<OutboxStatus> status,
                               @Param("limite") LocalDateTime limite);
}
