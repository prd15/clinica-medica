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
}
