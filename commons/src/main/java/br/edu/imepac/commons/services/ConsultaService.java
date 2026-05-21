package br.edu.imepac.commons.services;

import br.edu.imepac.commons.entities.ConsultaEntity;
import br.edu.imepac.commons.entities.StatusConsulta;
import br.edu.imepac.commons.repositories.ConsultaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ConsultaService {

    private static final int SLOT_MINUTOS = 30;

    private final ConsultaRepository consultaRepository;

    public ConsultaService(ConsultaRepository consultaRepository) {
        this.consultaRepository = consultaRepository;
    }

    @Transactional(readOnly = true)
    public Optional<ConsultaEntity> findById(Long id) {
        return consultaRepository.findById(id);
    }

    public ConsultaEntity agendar(ConsultaEntity consulta) {
        if (consulta.getDataHora() == null || consulta.getDataHora().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Data da consulta nao pode estar no passado");
        }
        if (existeConflito(consulta.getMedicoId(), consulta.getDataHora(), null)) {
            throw new IllegalStateException("Medico ja possui consulta neste horario");
        }
        consulta.setStatus(StatusConsulta.PENDENTE);
        return consultaRepository.save(consulta);
    }

    public Optional<ConsultaEntity> cancelar(Long id) {
        return consultaRepository.findById(id).map(consulta -> {
            if (consulta.getStatus() == StatusConsulta.REALIZADA) {
                throw new IllegalStateException("Consulta ja realizada nao pode ser cancelada");
            }
            consulta.setStatus(StatusConsulta.CANCELADA);
            return consultaRepository.save(consulta);
        });
    }

    public Optional<ConsultaEntity> reagendar(Long id, LocalDateTime novaDataHora) {
        return consultaRepository.findById(id).map(consulta -> {
            if (consulta.getStatus() == StatusConsulta.CANCELADA
                    || consulta.getStatus() == StatusConsulta.REALIZADA) {
                throw new IllegalStateException(
                        "Consulta no status " + consulta.getStatus() + " nao pode ser reagendada");
            }
            if (novaDataHora == null || novaDataHora.isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Data do reagendamento nao pode estar no passado");
            }
            if (existeConflito(consulta.getMedicoId(), novaDataHora, consulta.getId())) {
                throw new IllegalStateException("Medico ja possui consulta neste horario");
            }
            consulta.setDataHora(novaDataHora);
            return consultaRepository.save(consulta);
        });
    }

    public Optional<ConsultaEntity> confirmar(Long id) {
        return consultaRepository.findById(id).map(consulta -> {
            if (consulta.getStatus() != StatusConsulta.PENDENTE) {
                throw new IllegalStateException(
                        "Consulta nao pode ser confirmada no status atual: " + consulta.getStatus());
            }
            consulta.setStatus(StatusConsulta.CONFIRMADA);
            return consultaRepository.save(consulta);
        });
    }

    @Transactional(readOnly = true)
    public List<ConsultaEntity> findByMedicoId(Long medicoId) {
        return consultaRepository.findByMedicoId(medicoId);
    }

    @Transactional(readOnly = true)
    public List<ConsultaEntity> findByPacienteId(Long pacienteId) {
        return consultaRepository.findByPacienteId(pacienteId);
    }

    @Transactional(readOnly = true)
    public List<ConsultaEntity> findByData(LocalDate data) {
        LocalDateTime inicio = data.atStartOfDay();
        LocalDateTime fim = data.atTime(23, 59, 59, 999_999_999);
        return consultaRepository.findByDataHoraBetween(inicio, fim);
    }

    @Transactional(readOnly = true)
    public List<ConsultaEntity> findMinhaAgenda(Long medicoId) {
        return consultaRepository.findByMedicoIdAndStatus(medicoId, StatusConsulta.PENDENTE);
    }

    private boolean existeConflito(Long medicoId, LocalDateTime dataHora, Long ignorarConsultaId) {
        LocalDateTime inicio = dataHora.minusMinutes(SLOT_MINUTOS);
        LocalDateTime fim = dataHora.plusMinutes(SLOT_MINUTOS);
        List<ConsultaEntity> conflitantes = consultaRepository
                .findByMedicoIdAndDataHoraBetweenAndStatusNot(medicoId, inicio, fim, StatusConsulta.CANCELADA);
        return conflitantes.stream()
                .anyMatch(c -> ignorarConsultaId == null || !c.getId().equals(ignorarConsultaId));
    }
}
