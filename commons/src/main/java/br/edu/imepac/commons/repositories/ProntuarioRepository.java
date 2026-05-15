package br.edu.imepac.commons.repositories;

import br.edu.imepac.commons.entities.ProntuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProntuarioRepository extends JpaRepository<ProntuarioEntity, Long> {

    List<ProntuarioEntity> findAllByAtendimentoId(Long atendimentoId);

    Optional<ProntuarioEntity> findFirstByAtendimentoIdOrderByIdDesc(Long atendimentoId);
}
