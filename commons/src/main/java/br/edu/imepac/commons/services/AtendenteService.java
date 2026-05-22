package br.edu.imepac.commons.services;

import br.edu.imepac.commons.entities.AtendenteEntity;
import br.edu.imepac.commons.repositories.AtendenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// servico de atendentes — valida unicidade de usuario antes de salvar ou atualizar
@Service
public class AtendenteService {

    private final AtendenteRepository atendenteRepository;

    public AtendenteService(AtendenteRepository atendenteRepository) {
        this.atendenteRepository = atendenteRepository;
    }

    @Transactional(readOnly = true)
    public List<AtendenteEntity> findAll() {
        return atendenteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<AtendenteEntity> findById(Long id) {
        return atendenteRepository.findById(id);
    }

    @Transactional
    public AtendenteEntity save(AtendenteEntity atendente) {
        validarUsuarioDisponivel(atendente.getUsuario(), null);
        if (atendente.getAtivo() == null) {
            atendente.setAtivo(true);
        }
        return atendenteRepository.save(atendente);
    }

    @Transactional
    public Optional<AtendenteEntity> update(Long id, AtendenteEntity dadosAtualizados) {
        return atendenteRepository.findById(id).map(existing -> {
            validarUsuarioDisponivel(dadosAtualizados.getUsuario(), id);
            existing.setNome(dadosAtualizados.getNome());
            existing.setUsuario(dadosAtualizados.getUsuario());
            if (dadosAtualizados.getSenha() != null && !dadosAtualizados.getSenha().isBlank()) {
                existing.setSenha(dadosAtualizados.getSenha());
            }
            return atendenteRepository.save(existing);
        });
    }

    @Transactional
    public boolean deleteById(Long id) {
        if (atendenteRepository.existsById(id)) {
            atendenteRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // idAtual == null no save (sem id ainda); no update passa o id para ignorar o proprio registro
    private void validarUsuarioDisponivel(String usuario, Long idAtual) {
        atendenteRepository.findByUsuario(usuario)
                .filter(atendente -> idAtual == null || !atendente.getId().equals(idAtual))
                .ifPresent(atendente -> {
                    throw new IllegalStateException("Usuario ja cadastrado");
                });
    }
}
