package br.edu.imepac.atendimento.anotacao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnotacaoRepository extends JpaRepository<AnotacaoEntity, Long> {

    List<AnotacaoEntity> findByProntuarioId(Long prontuarioId);
}
