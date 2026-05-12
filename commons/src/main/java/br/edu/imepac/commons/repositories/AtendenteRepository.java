package br.edu.imepac.commons.repositories;

import br.edu.imepac.commons.entities.AtendenteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AtendenteRepository extends JpaRepository<AtendenteEntity, Long> {

    Optional<AtendenteEntity> findByUsuario(String usuario);
}
