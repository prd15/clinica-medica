package br.edu.imepac.commons.services;

import br.edu.imepac.commons.entities.MedicoEntity;
import br.edu.imepac.commons.repositories.MedicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MedicoService {

    private final MedicoRepository medicoRepository;

    public MedicoService(MedicoRepository medicoRepository) {
        this.medicoRepository = medicoRepository;
    }

    public List<MedicoEntity> findAll() {
        return medicoRepository.findAll();
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
            existing.setEmail(dadosAtualizados.getEmail());
            existing.setTelefone(dadosAtualizados.getTelefone());
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
