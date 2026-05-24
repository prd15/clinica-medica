package br.edu.imepac.commons.repositories.atendimento;

import br.edu.imepac.commons.entities.atendimento.OutboxEvent;
import br.edu.imepac.commons.entities.atendimento.OutboxStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    // eventos elegiveis para entrega: PENDENTE ou FALHA que ainda nao esgotaram as tentativas.
    // PESSIMISTIC_WRITE + SKIP_LOCKED (timeout=-2): se outra instancia do scheduler ja travou
    // certos eventos, esta consulta os pula em vez de bloquear ou explodir. Essencial em k8s
    // com 2+ replicas do atendimento — evita dupla entrega da notificacao.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("SELECT e FROM OutboxEvent e " +
            "WHERE e.status IN :status AND e.tentativas < :maxTentativas " +
            "ORDER BY e.criadoEm")
    List<OutboxEvent> buscarParaProcessar(@Param("status") Collection<OutboxStatus> status,
                                          @Param("maxTentativas") int maxTentativas);
}
