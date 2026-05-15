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
    public Optional<ConsultaEntity> cancelar(Long id) {
        return consultaRepository.findById(id).map(consulta -> {
            consulta.setStatus(StatusConsulta.CANCELADA);
            return consultaRepository.save(consulta);
        });
    }

    // muda data/hora — precisa revalidar conflito no novo horario
    public Optional<ConsultaEntity> reagendar(Long id, LocalDateTime novaDataHora) {
        return consultaRepository.findById(id).map(consulta -> {
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
    public Optional<ConsultaEntity> confirmar(Long id) {
        return consultaRepository.findById(id).map(consulta -> {
            if (consulta.getStatus() == StatusConsulta.PENDENTE) {
                consulta.setStatus(StatusConsulta.CONFIRMADA);
                return consultaRepository.save(consulta);
            }
            // se nao for PENDENTE, devolve sem alterar — controller decide o que retornar
            return consulta;
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
