package br.edu.imepac.commons.repositories;

import br.edu.imepac.commons.entities.ConsultaEntity;
import br.edu.imepac.commons.entities.StatusConsulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface ConsultaRepository extends JpaRepository<ConsultaEntity, Long> {

    // agenda do medico — tudo que ele ja atendeu ou vai atender
    List<ConsultaEntity> findByMedicoId(Long medicoId);

    // historico do paciente
    List<ConsultaEntity> findByPacienteId(Long pacienteId);

    // util pra buscar consultas de um dia inteiro: passa inicio e fim do dia
    List<ConsultaEntity> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);

    // agenda do medico num dia especifico: combina medicoId + intervalo do dia
    List<ConsultaEntity> findByMedicoIdAndDataHoraBetween(Long medicoId, LocalDateTime inicio, LocalDateTime fim);

    // agenda do medico por varios status — ex: PENDENTE + CONFIRMADA (o que ainda vai atender)
    List<ConsultaEntity> findByMedicoIdAndStatusIn(Long medicoId, Collection<StatusConsulta> status);

    // validacao de conflito por slot: consultas do medico na janela de horario que NAO estejam CANCELADAS
    List<ConsultaEntity> findByMedicoIdAndDataHoraBetweenAndStatusNot(Long medicoId,
                                                                     LocalDateTime inicio,
                                                                     LocalDateTime fim,
                                                                     StatusConsulta status);
}
