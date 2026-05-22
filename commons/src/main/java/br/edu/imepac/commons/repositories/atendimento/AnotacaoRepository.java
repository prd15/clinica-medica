package br.edu.imepac.commons.repositories.atendimento;

import br.edu.imepac.commons.entities.atendimento.AnotacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnotacaoRepository extends JpaRepository<AnotacaoEntity, Long> {

    List<AnotacaoEntity> findByProntuarioId(Long prontuarioId);
}
