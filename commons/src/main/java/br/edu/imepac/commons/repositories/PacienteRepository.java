package br.edu.imepac.commons.repositories;

import br.edu.imepac.commons.entities.PacienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PacienteRepository extends JpaRepository<PacienteEntity, Long> {
}
