package br.edu.imepac.commons.repositories.atendimento;

import br.edu.imepac.commons.entities.atendimento.SolicitacaoExameEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitacaoExameRepository extends JpaRepository<SolicitacaoExameEntity, Long> {

    List<SolicitacaoExameEntity> findByAtendimentoId(Long atendimentoId);
}
