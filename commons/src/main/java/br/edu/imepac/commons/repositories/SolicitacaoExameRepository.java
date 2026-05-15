package br.edu.imepac.commons.repositories;

import br.edu.imepac.commons.entities.SolicitacaoExameEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitacaoExameRepository extends JpaRepository<SolicitacaoExameEntity, Long> {

    List<SolicitacaoExameEntity> findByAtendimentoId(Long atendimentoId);
}
