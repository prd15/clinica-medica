package br.edu.imepac.commons.repositories;

import br.edu.imepac.commons.entities.PacienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<PacienteEntity, Long> {

    // case-insensitive pra nao depender de como o usuario digitou
    List<PacienteEntity> findByNomeContainingIgnoreCase(String nome);

    // cpf e unico, retorna Optional pra nao quebrar com nulo
    Optional<PacienteEntity> findByCpf(String cpf);

    // todos os pacientes de um convenio — usado no filtro da listagem
    List<PacienteEntity> findByConvenioId(Long convenioId);
}
