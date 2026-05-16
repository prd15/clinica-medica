package br.edu.imepac.commons.repositories;

import br.edu.imepac.commons.entities.ProntuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProntuarioRepository extends JpaRepository<ProntuarioEntity, Long> {
}
