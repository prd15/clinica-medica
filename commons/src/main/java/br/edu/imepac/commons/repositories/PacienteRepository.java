package br.edu.imepac.commons.repositories;

import br.edu.imepac.commons.entities.PacienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<PacienteEntity, Long> {

    List<PacienteEntity> findByNomeContainingIgnoreCase(String nome);

    Optional<PacienteEntity> findByCpf(String cpf);

    List<PacienteEntity> findByConvenioId(Long convenioId);
}
