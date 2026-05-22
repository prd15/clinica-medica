package br.edu.imepac.commons.repositories.administrativo;

import br.edu.imepac.commons.entities.administrativo.EspecialidadeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EspecialidadeRepository extends JpaRepository<EspecialidadeEntity, Long> {

    Optional<EspecialidadeEntity> findByNome(String nome);
}