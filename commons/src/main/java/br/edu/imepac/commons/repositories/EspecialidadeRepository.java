package br.edu.imepac.commons.repositories;

import br.edu.imepac.commons.entities.EspecialidadeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EspecialidadeRepository extends JpaRepository<EspecialidadeEntity, Long> {

    Optional<EspecialidadeEntity> findByNome(String nome);
}