package br.edu.imepac.commons.repositories.atendimento;

import br.edu.imepac.commons.entities.atendimento.OutboxEvent;
import br.edu.imepac.commons.entities.atendimento.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    // eventos elegiveis para entrega: PENDENTE ou FALHA que ainda nao esgotaram as tentativas
    List<OutboxEvent> findByStatusInAndTentativasLessThan(Collection<OutboxStatus> status, int maxTentativas);
}
