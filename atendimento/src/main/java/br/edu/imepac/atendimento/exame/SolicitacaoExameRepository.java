package br.edu.imepac.atendimento.exame;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitacaoExameRepository extends JpaRepository<SolicitacaoExameEntity, Long> {

    List<SolicitacaoExameEntity> findByAtendimentoId(Long atendimentoId);
}
