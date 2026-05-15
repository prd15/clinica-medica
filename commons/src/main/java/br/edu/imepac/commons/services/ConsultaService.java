package br.edu.imepac.commons.services;

import br.edu.imepac.commons.entities.ConsultaEntity;
import br.edu.imepac.commons.entities.StatusConsulta;
import br.edu.imepac.commons.repositories.ConsultaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ConsultaService {

    private final ConsultaRepository consultaRepository;

    public ConsultaService(ConsultaRepository consultaRepository) {
        this.consultaRepository = consultaRepository;
    }

    public Optional<ConsultaEntity> findById(Long id) {
        return consultaRepository.findById(id);
    }

    // metodo central do modulo: valida conflito antes de salvar
    // a validacao de convenio ativo fica no Controller (precisa de HTTP — nao e responsabilidade do Service)
    public ConsultaEntity agendar(ConsultaEntity consulta) {
        // agendamento retroativo nao faz sentido na regra de negocio
        if (consulta.getDataHora() == null || consulta.getDataHora().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Data da consulta nao pode estar no passado");
        }
        // conflito = mesmo medico, mesmo horario, status diferente de CANCELADA
        // (uma consulta cancelada libera o slot — nao bloqueia novo agendamento)
        boolean conflito = consultaRepository.existsByMedicoIdAndDataHoraAndStatusNot(
                consulta.getMedicoId(),
                consulta.getDataHora(),
                StatusConsulta.CANCELADA
        );
        if (conflito) {
            throw new RuntimeException("Medico ja possui consulta neste horario");
        }
        // toda consulta nasce PENDENTE — sobrescreve qualquer status vindo de fora
        consulta.setStatus(StatusConsulta.PENDENTE);
        return consultaRepository.save(consulta);
    }

    // soft delete logico — mantem historico, so muda o status
    // consulta REALIZADA nao pode ser cancelada (ja aconteceu — alterar status falsearia historico)
    public Optional<ConsultaEntity> cancelar(Long id) {
        return consultaRepository.findById(id).map(consulta -> {
            if (consulta.getStatus() == StatusConsulta.REALIZADA) {
                throw new IllegalStateException("Consulta ja realizada nao pode ser cancelada");
            }
            consulta.setStatus(StatusConsulta.CANCELADA);
            return consultaRepository.save(consulta);
        });
    }

    // muda data/hora — precisa revalidar conflito no novo horario
    // CANCELADA/REALIZADA sao status terminais; reagendar elas nao faz sentido
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
            boolean conflito = consultaRepository.existsByMedicoIdAndDataHoraAndStatusNot(
                    consulta.getMedicoId(),
                    novaDataHora,
                    StatusConsulta.CANCELADA
            );
            if (conflito) {
                throw new RuntimeException("Medico ja possui consulta neste horario");
            }
            consulta.setDataHora(novaDataHora);
            return consultaRepository.save(consulta);
        });
    }

    // so confirma se estiver PENDENTE — cancelada/realizada nao volta atras
    // antes a gente devolvia sem alterar; mudei pra estourar exception
    // pq retornar 200 em uma operacao que nao fez nada e enganoso pra quem consome a API
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

    public List<ConsultaEntity> findByMedicoId(Long medicoId) {
        return consultaRepository.findByMedicoId(medicoId);
    }

    public List<ConsultaEntity> findByPacienteId(Long pacienteId) {
        return consultaRepository.findByPacienteId(pacienteId);
    }

    // converte LocalDate para intervalo do dia: 00:00:00 ate 23:59:59.999...
    // assim findByDataHoraBetween pega tudo que foi marcado naquele dia
    public List<ConsultaEntity> findByData(LocalDate data) {
        LocalDateTime inicio = data.atStartOfDay();
        LocalDateTime fim = data.atTime(23, 59, 59, 999_999_999);
        return consultaRepository.findByDataHoraBetween(inicio, fim);
    }

    // agenda do medico = so o que esta PENDENTE (consultas futuras nao confirmadas ainda)
    public List<ConsultaEntity> findMinhaAgenda(Long medicoId) {
        return consultaRepository.findByMedicoIdAndStatus(medicoId, StatusConsulta.PENDENTE);
    }
}
