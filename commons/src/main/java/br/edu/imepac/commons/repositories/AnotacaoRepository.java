package br.edu.imepac.commons.repositories;

import br.edu.imepac.commons.entities.AnotacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnotacaoRepository extends JpaRepository<AnotacaoEntity, Long> {

    List<AnotacaoEntity> findByProntuarioId(Long prontuarioId);
}
