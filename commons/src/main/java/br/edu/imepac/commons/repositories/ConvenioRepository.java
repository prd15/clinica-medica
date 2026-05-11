package br.edu.imepac.commons.repositories;

import br.edu.imepac.commons.entities.ConvenioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConvenioRepository extends JpaRepository<ConvenioEntity, Long> {

    List<ConvenioEntity> findByAtivo(Boolean ativo);
}

