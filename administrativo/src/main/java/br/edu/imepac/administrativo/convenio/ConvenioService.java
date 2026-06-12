package br.edu.imepac.administrativo.convenio;

import br.edu.imepac.commons.exceptions.BusinessException;
import br.edu.imepac.commons.exceptions.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    @Transactional(readOnly = true)
    public List<ConvenioEntity> findByAtivo(Boolean ativo) {
        return convenioRepository.findByAtivo(ativo);
    }

    @Transactional
    public ConvenioEntity save(ConvenioEntity convenio) {
        validarNomeDisponivel(convenio.getNome(), null);
        return convenioRepository.save(convenio);
    }

    @Transactional
    public Optional<ConvenioEntity> update(Long id, ConvenioEntity dadosAtualizados) {
        return convenioRepository.findById(id).map(existing -> {
            validarNomeDisponivel(dadosAtualizados.getNome(), id);
            existing.setNome(dadosAtualizados.getNome());
            existing.setDescricao(dadosAtualizados.getDescricao());
            existing.setCnpj(dadosAtualizados.getCnpj());
            existing.setTelefone(dadosAtualizados.getTelefone());
            if (dadosAtualizados.getAtivo() != null) {
                existing.setAtivo(dadosAtualizados.getAtivo());
            }
            return convenioRepository.save(existing);
        });
    }

    private void validarNomeDisponivel(String nome, Long idAtual) {
        convenioRepository.findByNomeIgnoreCase(nome)
                .filter(c -> idAtual == null || !c.getId().equals(idAtual))
                .ifPresent(c -> {
                    throw new BusinessException("Convenio ja cadastrado com este nome");
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

    @Transactional
    public ConvenioEntity alterarStatus(Long id, Boolean ativo) {
        ConvenioEntity convenio = convenioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Convenio", id));
        convenio.setAtivo(ativo);
        return convenioRepository.save(convenio);
    }
}
