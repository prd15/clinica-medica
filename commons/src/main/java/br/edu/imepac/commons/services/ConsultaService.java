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
}
