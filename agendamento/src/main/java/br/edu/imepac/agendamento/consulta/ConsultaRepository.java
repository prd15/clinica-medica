package br.edu.imepac.agendamento.consulta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface ConsultaRepository extends JpaRepository<ConsultaEntity, Long> {

    List<ConsultaEntity> findByMedicoId(Long medicoId);

    List<ConsultaEntity> findByPacienteId(Long pacienteId);

    List<ConsultaEntity> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);

    long countByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);

    List<ConsultaEntity> findByMedicoIdAndDataHoraBetween(Long medicoId, LocalDateTime inicio, LocalDateTime fim);

    List<ConsultaEntity> findByMedicoIdAndStatusIn(Long medicoId, Collection<StatusConsulta> status);

    // validacao de conflito por slot: consultas do medico na janela de horario que NAO estejam CANCELADAS
    List<ConsultaEntity> findByMedicoIdAndDataHoraBetweenAndStatusNot(Long medicoId,
                                                                     LocalDateTime inicio,
                                                                     LocalDateTime fim,
                                                                     StatusConsulta status);
}
