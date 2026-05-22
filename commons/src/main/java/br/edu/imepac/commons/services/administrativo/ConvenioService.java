package br.edu.imepac.commons.services.administrativo;

import br.edu.imepac.commons.entities.administrativo.ConvenioEntity;
import br.edu.imepac.commons.repositories.administrativo.ConvenioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ConvenioService {

    private final ConvenioRepository convenioRepository;

    public ConvenioService(ConvenioRepository convenioRepository) {
        this.convenioRepository = convenioRepository;
    }

    @Transactional(readOnly = true)
    public List<ConvenioEntity> findAll() {
        return convenioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<ConvenioEntity> findById(Long id) {
        return convenioRepository.findById(id);
    }

    // filtra so os ativos (ou inativos) — depende do que o front precisar
    @Transactional(readOnly = true)
    public List<ConvenioEntity> findByAtivo(Boolean ativo) {
        return convenioRepository.findByAtivo(ativo);
    }

    @Transactional
    public ConvenioEntity save(ConvenioEntity convenio) {
        validarNomeDisponivel(convenio.getNome(), null);
        return convenioRepository.save(convenio);
    }

    // atualiza tudo exceto o id — ativo so muda se vier no request
    @Transactional
    public Optional<ConvenioEntity> update(Long id, ConvenioEntity dadosAtualizados) {
        return convenioRepository.findById(id).map(existing -> {
            validarNomeDisponivel(dadosAtualizados.getNome(), id);
            existing.setNome(dadosAtualizados.getNome());
            existing.setDescricao(dadosAtualizados.getDescricao());
            existing.setCnpj(dadosAtualizados.getCnpj());
            existing.setTelefone(dadosAtualizados.getTelefone());
            // nao deixa setar null no ativo via PUT
            if (dadosAtualizados.getAtivo() != null) {
                existing.setAtivo(dadosAtualizados.getAtivo());
            }
            return convenioRepository.save(existing);
        });
    }

    // garante que nao cadastra dois convenios com o mesmo nome (case-insensitive)
    private void validarNomeDisponivel(String nome, Long idAtual) {
        convenioRepository.findByNomeIgnoreCase(nome)
                .filter(c -> idAtual == null || !c.getId().equals(idAtual))
                .ifPresent(c -> {
                    throw new IllegalStateException("Convenio ja cadastrado com este nome");
                });
    }

    @Transactional
    public boolean deleteById(Long id) {
        if (convenioRepository.existsById(id)) {
            convenioRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // PATCH especifico pra status — evita mandar o objeto inteiro so pra ativar/desativar
    @Transactional
    public ConvenioEntity alterarStatus(Long id, Boolean ativo) {
        ConvenioEntity convenio = convenioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Convenio nao encontrado com id: " + id));
        convenio.setAtivo(ativo);
        return convenioRepository.save(convenio);
    }
}

