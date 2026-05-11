package br.edu.imepac.commons.services;

import br.edu.imepac.commons.entities.PacienteEntity;
import br.edu.imepac.commons.repositories.PacienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * CRUD de pacientes. Segue o mesmo padrao do ConvenioService.
 */
@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    public PacienteService(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    public List<PacienteEntity> findAll() {
        return pacienteRepository.findAll();
    }

    public Optional<PacienteEntity> findById(Long id) {
        return pacienteRepository.findById(id);
    }

    public PacienteEntity save(PacienteEntity paciente) {
        return pacienteRepository.save(paciente);
    }

    // atualiza todos os campos se o paciente existir, senao retorna vazio
    public Optional<PacienteEntity> update(Long id, PacienteEntity dadosAtualizados) {
        return pacienteRepository.findById(id).map(existing -> {
            existing.setNome(dadosAtualizados.getNome());
            existing.setCpf(dadosAtualizados.getCpf());
            existing.setDataNascimento(dadosAtualizados.getDataNascimento());
            existing.setTelefone(dadosAtualizados.getTelefone());
            existing.setEmail(dadosAtualizados.getEmail());
            existing.setEndereco(dadosAtualizados.getEndereco());
            existing.setConvenioId(dadosAtualizados.getConvenioId());
            return pacienteRepository.save(existing);
        });
    }

    public boolean deleteById(Long id) {
        if (pacienteRepository.existsById(id)) {
            pacienteRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
