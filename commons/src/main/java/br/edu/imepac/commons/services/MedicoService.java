package br.edu.imepac.commons.services;

import br.edu.imepac.commons.entities.EspecialidadeEntity;
import br.edu.imepac.commons.entities.MedicoEntity;
import br.edu.imepac.commons.repositories.EspecialidadeRepository;
import br.edu.imepac.commons.repositories.MedicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MedicoService {

    private final MedicoRepository medicoRepository;
    private final EspecialidadeRepository especialidadeRepository;

    public MedicoService(MedicoRepository medicoRepository, EspecialidadeRepository especialidadeRepository) {
        this.medicoRepository = medicoRepository;
        this.especialidadeRepository = especialidadeRepository;
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
            existing.setSenha(dadosAtualizados.getSenha());
            existing.setTelefone(dadosAtualizados.getTelefone());
            return medicoRepository.save(existing);
        });
    }

    public Optional<MedicoEntity> inativar(Long id) {
        return medicoRepository.findById(id).map(medico -> {
            medico.setAtivo(false);
            return medicoRepository.save(medico);
        });
    }

    public Optional<MedicoEntity> associarEspecialidade(Long medicoId, Long especialidadeId) {
        Optional<MedicoEntity> medicoOpt = medicoRepository.findById(medicoId);
        Optional<EspecialidadeEntity> especialidadeOpt = especialidadeRepository.findById(especialidadeId);
        if (medicoOpt.isEmpty() || especialidadeOpt.isEmpty()) {
            return Optional.empty();
        }
        MedicoEntity medico = medicoOpt.get();
        EspecialidadeEntity especialidade = especialidadeOpt.get();
        if (medico.getEspecialidades().contains(especialidade)) {
            return Optional.of(medico);
        }
        medico.getEspecialidades().add(especialidade);
        return Optional.of(medicoRepository.save(medico));
    }

    public Optional<MedicoEntity> removerEspecialidade(Long medicoId, Long especialidadeId) {
        Optional<MedicoEntity> medicoOpt = medicoRepository.findById(medicoId);
        Optional<EspecialidadeEntity> especialidadeOpt = especialidadeRepository.findById(especialidadeId);
        if (medicoOpt.isEmpty() || especialidadeOpt.isEmpty()) {
            return Optional.empty();
        }
        MedicoEntity medico = medicoOpt.get();
        medico.getEspecialidades().remove(especialidadeOpt.get());
        return Optional.of(medicoRepository.save(medico));
    }

    public boolean deleteById(Long id) {
        if (medicoRepository.existsById(id)) {
            medicoRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
