package br.edu.imepac.commons.repositories.administrativo;

import br.edu.imepac.commons.entities.administrativo.AtendenteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AtendenteRepository extends JpaRepository<AtendenteEntity, Long> {

    Optional<AtendenteEntity> findByUsuario(String usuario);
}
