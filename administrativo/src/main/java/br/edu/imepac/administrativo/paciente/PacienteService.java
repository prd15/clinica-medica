package br.edu.imepac.administrativo.paciente;

import br.edu.imepac.commons.exceptions.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    public PacienteService(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    @Transactional(readOnly = true)
    public List<PacienteEntity> findAll() {
        return pacienteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<PacienteEntity> findByNome(String nome) {
        return pacienteRepository.findByNomeContainingIgnoreCase(nome);
    }

    @Transactional(readOnly = true)
    public Optional<PacienteEntity> findByCpf(String cpf) {
        return pacienteRepository.findByCpf(cpf);
    }

    @Transactional(readOnly = true)
    public List<PacienteEntity> findByConvenioId(Long convenioId) {
        return pacienteRepository.findByConvenioId(convenioId);
    }

    @Transactional(readOnly = true)
    public Optional<PacienteEntity> findById(Long id) {
        return pacienteRepository.findById(id);
    }

    @Transactional
    public PacienteEntity save(PacienteEntity paciente) {
        validarCpfDisponivel(paciente.getCpf(), null);
        return pacienteRepository.save(paciente);
    }

    @Transactional
    public Optional<PacienteEntity> update(Long id, PacienteEntity dadosAtualizados) {
        return pacienteRepository.findById(id).map(existing -> {
            validarCpfDisponivel(dadosAtualizados.getCpf(), id);
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

    private void validarCpfDisponivel(String cpf, Long idAtual) {
        pacienteRepository.findByCpf(cpf)
                .filter(paciente -> idAtual == null || !paciente.getId().equals(idAtual))
                .ifPresent(paciente -> {
                    throw new BusinessException("CPF ja cadastrado para outro paciente");
                });
    }

    // prioridade: nome > cpf > convenioId > sem filtro
    @Transactional(readOnly = true)
    public List<PacienteEntity> buscarComFiltros(String nome, String cpf, Long convenioId) {
        if (nome != null && !nome.isBlank()) {
            return pacienteRepository.findByNomeContainingIgnoreCase(nome);
        }
        if (cpf != null && !cpf.isBlank()) {
            return pacienteRepository.findByCpf(cpf)
                    .map(p -> List.of(p))
                    .orElse(List.of());
        }
        if (convenioId != null) {
            return pacienteRepository.findByConvenioId(convenioId);
        }
        return pacienteRepository.findAll();
    }

    @Transactional
    public boolean deleteById(Long id) {
        if (pacienteRepository.existsById(id)) {
            pacienteRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
