package br.edu.imepac.commons.services;

import br.edu.imepac.commons.entities.ConvenioEntity;
import br.edu.imepac.commons.repositories.ConvenioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConvenioService {

    private final ConvenioRepository convenioRepository;

    public ConvenioService(ConvenioRepository convenioRepository) {
        this.convenioRepository = convenioRepository;
    }

    public List<ConvenioEntity> findAll() {
        return convenioRepository.findAll();
    }

    // filtra so os ativos (ou inativos) — depende do que o front precisar
    public List<ConvenioEntity> findByAtivo(Boolean ativo) {
        return convenioRepository.findByAtivo(ativo);
    }

    public Optional<ConvenioEntity> findById(Long id) {
        return convenioRepository.findById(id);
    }

    public ConvenioEntity save(ConvenioEntity convenio) {
        return convenioRepository.save(convenio);
    }

    // atualiza tudo exceto o id — ativo so muda se vier no request
    public Optional<ConvenioEntity> update(Long id, ConvenioEntity dadosAtualizados) {
        return convenioRepository.findById(id).map(existing -> {
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

    public boolean deleteById(Long id) {
        if (convenioRepository.existsById(id)) {
            convenioRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public ConvenioEntity alterarStatus(Long id, Boolean ativo) {
        ConvenioEntity convenio = convenioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Convenio nao encontrado com id: " + id));
        convenio.setAtivo(ativo);
        return convenioRepository.save(convenio);
    }
}

