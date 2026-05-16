package br.edu.imepac.commons.services;

import br.edu.imepac.commons.entities.MedicoEntity;
import br.edu.imepac.commons.repositories.MedicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MedicoService {

    private final MedicoRepository medicoRepository;

    public MedicoService(MedicoRepository medicoRepository) {
        this.medicoRepository = medicoRepository;
    }

    @Transactional(readOnly = true)
    public List<MedicoEntity> findAll() {
        return medicoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<MedicoEntity> findByAtivo(Boolean ativo) {
        return medicoRepository.findByAtivo(ativo);
    }

    public Optional<MedicoEntity> findById(Long id) {
        return medicoRepository.findById(id);
    }

    public MedicoEntity save(MedicoEntity medico) {
        return medicoRepository.save(medico);
    }

    public Optional<MedicoEntity> update(Long id, MedicoEntity dadosAtualizados) {
        return medicoRepository.findById(id).map(existing -> {
            existing.setNome(dadosAtualizados.getNome());
            existing.setCrm(dadosAtualizados.getCrm());
            existing.setTelefone(dadosAtualizados.getTelefone());
            existing.setEmail(dadosAtualizados.getEmail());
            existing.setAtivo(dadosAtualizados.getAtivo());
            return medicoRepository.save(existing);
        });
    }

    public boolean deleteById(Long id) {
        if (medicoRepository.existsById(id)) {
            medicoRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
