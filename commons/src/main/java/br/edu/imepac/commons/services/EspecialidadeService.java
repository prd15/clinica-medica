package br.edu.imepac.commons.services;

import br.edu.imepac.commons.entities.EspecialidadeEntity;
import br.edu.imepac.commons.repositories.EspecialidadeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EspecialidadeService {

    private final EspecialidadeRepository especialidadeRepository;

    public EspecialidadeService(EspecialidadeRepository especialidadeRepository) {
        this.especialidadeRepository = especialidadeRepository;
    }

    @Transactional(readOnly = true)
    public List<EspecialidadeEntity> findAll() {
        return especialidadeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<EspecialidadeEntity> findById(Long id) {
        return especialidadeRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<EspecialidadeEntity> findByNome(String nome) {
        return especialidadeRepository.findByNome(nome);
    }

    public EspecialidadeEntity save(EspecialidadeEntity especialidade) {
        return especialidadeRepository.save(especialidade);
    }

    public Optional<EspecialidadeEntity> update(Long id, EspecialidadeEntity dadosAtualizados) {
        return especialidadeRepository.findById(id).map(existing -> {
            existing.setNome(dadosAtualizados.getNome());
            existing.setDescricao(dadosAtualizados.getDescricao());
            return especialidadeRepository.save(existing);
        });
    }

    public boolean deleteById(Long id) {
        if (especialidadeRepository.existsById(id)) {
            especialidadeRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
