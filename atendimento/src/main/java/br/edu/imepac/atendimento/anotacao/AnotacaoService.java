package br.edu.imepac.atendimento.anotacao;

import br.edu.imepac.atendimento.prontuario.ProntuarioEntity;
import br.edu.imepac.atendimento.prontuario.ProntuarioRepository;
import br.edu.imepac.commons.exceptions.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnotacaoService {

    private final ProntuarioRepository prontuarioRepository;
    private final AnotacaoRepository anotacaoRepository;

    public AnotacaoService(ProntuarioRepository prontuarioRepository,
                           AnotacaoRepository anotacaoRepository) {
        this.prontuarioRepository = prontuarioRepository;
        this.anotacaoRepository = anotacaoRepository;
    }

    @Transactional
    public AnotacaoEntity adicionarAnotacao(Long atendimentoId, String texto) {
        ProntuarioEntity prontuario = prontuarioRepository
                .findByAtendimentoId(atendimentoId)
                .orElseThrow(() -> new EntityNotFoundException("Prontuario nao encontrado para atendimentoId: " + atendimentoId));

        AnotacaoEntity anotacao = new AnotacaoEntity();
        anotacao.setProntuarioId(prontuario.getId());
        anotacao.setTexto(texto);
        anotacao.setDataCriacao(LocalDateTime.now());
        return anotacaoRepository.save(anotacao);
    }

    @Transactional(readOnly = true)
    public List<AnotacaoEntity> listarAnotacoes(Long atendimentoId) {
        ProntuarioEntity prontuario = prontuarioRepository
                .findByAtendimentoId(atendimentoId)
                .orElseThrow(() -> new EntityNotFoundException("Prontuario nao encontrado para atendimentoId: " + atendimentoId));
        return anotacaoRepository.findByProntuarioId(prontuario.getId());
    }
}
