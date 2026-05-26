package br.edu.imepac.atendimento.exame;

import br.edu.imepac.atendimento.atendimento.AtendimentoRepository;
import br.edu.imepac.commons.exceptions.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExameService {

    private final AtendimentoRepository atendimentoRepository;
    private final SolicitacaoExameRepository exameRepository;

    public ExameService(AtendimentoRepository atendimentoRepository,
                        SolicitacaoExameRepository exameRepository) {
        this.atendimentoRepository = atendimentoRepository;
        this.exameRepository = exameRepository;
    }

    @Transactional
    public SolicitacaoExameEntity solicitarExame(Long atendimentoId, String descricao, String tipo) {
        atendimentoRepository.findById(atendimentoId)
                .orElseThrow(() -> new EntityNotFoundException("Atendimento", atendimentoId));

        SolicitacaoExameEntity exame = new SolicitacaoExameEntity();
        exame.setAtendimentoId(atendimentoId);
        exame.setDescricao(descricao);
        exame.setTipo(tipo);
        exame.setDataSolicitacao(LocalDateTime.now());
        return exameRepository.save(exame);
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoExameEntity> listarExames(Long atendimentoId) {
        atendimentoRepository.findById(atendimentoId)
                .orElseThrow(() -> new EntityNotFoundException("Atendimento", atendimentoId));
        return exameRepository.findByAtendimentoId(atendimentoId);
    }
}
