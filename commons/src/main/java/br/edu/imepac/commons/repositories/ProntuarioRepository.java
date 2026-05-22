package br.edu.imepac.commons.repositories;

import br.edu.imepac.commons.entities.ProntuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProntuarioRepository extends JpaRepository<ProntuarioEntity, Long> {

    // atendimentoId tem unique constraint, entao Optional reflete a invariante 1:1
    Optional<ProntuarioEntity> findByAtendimentoId(Long atendimentoId);
}
