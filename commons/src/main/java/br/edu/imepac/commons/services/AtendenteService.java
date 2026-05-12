package br.edu.imepac.commons.services;

import br.edu.imepac.commons.entities.AtendenteEntity;
import br.edu.imepac.commons.repositories.AtendenteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AtendenteService {

    private final AtendenteRepository atendenteRepository;

    public AtendenteService(AtendenteRepository atendenteRepository) {
        this.atendenteRepository = atendenteRepository;
    }

    public List<AtendenteEntity> findAll() {
        return atendenteRepository.findAll();
    }

    public Optional<AtendenteEntity> findById(Long id) {
        return atendenteRepository.findById(id);
    }

    public Optional<AtendenteEntity> findByUsuario(String usuario) {
        return atendenteRepository.findByUsuario(usuario);
    }

    public AtendenteEntity save(AtendenteEntity atendente) {
        validarUsuarioDisponivel(atendente.getUsuario(), null);
        if (atendente.getAtivo() == null) {
            atendente.setAtivo(true);
        }
        return atendenteRepository.save(atendente);
    }

    public Optional<AtendenteEntity> update(Long id, AtendenteEntity dadosAtualizados) {
        return atendenteRepository.findById(id).map(existing -> {
            validarUsuarioDisponivel(dadosAtualizados.getUsuario(), id);
            existing.setNome(dadosAtualizados.getNome());
            existing.setUsuario(dadosAtualizados.getUsuario());
            existing.setSenha(dadosAtualizados.getSenha());
            return atendenteRepository.save(existing);
        });
    }

    public boolean deleteById(Long id) {
        if (atendenteRepository.existsById(id)) {
            atendenteRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private void validarUsuarioDisponivel(String usuario, Long idAtual) {
        atendenteRepository.findByUsuario(usuario)
                .filter(atendente -> idAtual == null || !atendente.getId().equals(idAtual))
                .ifPresent(atendente -> {
                    throw new IllegalStateException("Usuario ja cadastrado");
                });
    }
}
