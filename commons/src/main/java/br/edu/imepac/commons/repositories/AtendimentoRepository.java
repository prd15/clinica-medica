package br.edu.imepac.commons.repositories;

import br.edu.imepac.commons.entities.AtendimentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AtendimentoRepository extends JpaRepository<AtendimentoEntity, Long> {
}
