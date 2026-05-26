package br.edu.imepac.administrativo.atendente;

import br.edu.imepac.commons.exceptions.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
        // normaliza usuario para evitar duplicatas tipo "maria" vs "Maria"
        if (atendente.getUsuario() != null) {
            atendente.setUsuario(atendente.getUsuario().toLowerCase());
        }
        validarUsuarioDisponivel(atendente.getUsuario(), null);
        if (atendente.getAtivo() == null) {
            atendente.setAtivo(true);
        }
        return atendenteRepository.save(atendente);
    }

    @Transactional
    public Optional<AtendenteEntity> update(Long id, AtendenteEntity dadosAtualizados) {
        return atendenteRepository.findById(id).map(existing -> {
            String usuarioNormalizado = dadosAtualizados.getUsuario() != null
                    ? dadosAtualizados.getUsuario().toLowerCase()
                    : null;
            validarUsuarioDisponivel(usuarioNormalizado, id);
            existing.setNome(dadosAtualizados.getNome());
            existing.setUsuario(usuarioNormalizado);
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
                    throw new BusinessException("Usuario ja cadastrado");
                });
    }
}
