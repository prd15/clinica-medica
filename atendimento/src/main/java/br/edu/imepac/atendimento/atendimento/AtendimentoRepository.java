package br.edu.imepac.atendimento.atendimento;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AtendimentoRepository extends JpaRepository<AtendimentoEntity, Long> {

    List<AtendimentoEntity> findByPacienteId(Long pacienteId);

    Optional<AtendimentoEntity> findByConsultaId(Long consultaId);

    boolean existsByConsultaId(Long consultaId);
}
