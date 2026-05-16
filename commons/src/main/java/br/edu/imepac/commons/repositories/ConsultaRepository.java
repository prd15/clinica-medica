package br.edu.imepac.commons.repositories;

import br.edu.imepac.commons.entities.ConsultaEntity;
import br.edu.imepac.commons.entities.StatusConsulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConsultaRepository extends JpaRepository<ConsultaEntity, Long> {

    // agenda do medico — tudo que ele ja atendeu ou vai atender
    List<ConsultaEntity> findByMedicoId(Long medicoId);

    // historico do paciente
    List<ConsultaEntity> findByPacienteId(Long pacienteId);

    // util pra buscar consultas de um dia inteiro: passa inicio e fim do dia
    List<ConsultaEntity> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);

    // filtra a agenda do medico por status — ex: somente PENDENTE
    List<ConsultaEntity> findByMedicoIdAndStatus(Long medicoId, StatusConsulta status);

    // validacao critica de conflito: existe consulta no mesmo medico+horario que NAO esteja CANCELADA?
    // Spring Data deriva a query a partir do nome do metodo — sem precisar escrever JPQL/SQL
    boolean existsByMedicoIdAndDataHoraAndStatusNot(Long medicoId, LocalDateTime dataHora, StatusConsulta status);

    List<ConsultaEntity> findByMedicoIdAndDataHoraBetweenAndStatusNot(Long medicoId,
                                                                     LocalDateTime inicio,
                                                                     LocalDateTime fim,
                                                                     StatusConsulta status);
}
