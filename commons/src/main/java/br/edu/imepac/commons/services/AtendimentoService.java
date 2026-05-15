package br.edu.imepac.commons.services;

import br.edu.imepac.commons.entities.AtendimentoEntity;
import br.edu.imepac.commons.entities.AnotacaoEntity;
import br.edu.imepac.commons.entities.ProntuarioEntity;
import br.edu.imepac.commons.entities.SolicitacaoExameEntity;
import br.edu.imepac.commons.repositories.AtendimentoRepository;
import br.edu.imepac.commons.repositories.AnotacaoRepository;
import br.edu.imepac.commons.repositories.ProntuarioRepository;
import br.edu.imepac.commons.repositories.SolicitacaoExameRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AtendimentoService {

    private final AtendimentoRepository atendimentoRepository;
    private final ProntuarioRepository prontuarioRepository;
    private final AnotacaoRepository anotacaoRepository;
    private final SolicitacaoExameRepository exameRepository;

    public AtendimentoService(AtendimentoRepository atendimentoRepository,
                              ProntuarioRepository prontuarioRepository,
                              AnotacaoRepository anotacaoRepository,
                              SolicitacaoExameRepository exameRepository) {
        this.atendimentoRepository = atendimentoRepository;
        this.prontuarioRepository = prontuarioRepository;
        this.anotacaoRepository = anotacaoRepository;
        this.exameRepository = exameRepository;
    }

    // salva o atendimento e cria o prontuário na mesma transação
    @Transactional
    public AtendimentoEntity registrar(AtendimentoEntity atendimento,
                                       String descricao,
                                       String diagnostico,
                                       String observacoes) {
        atendimento.setDataHora(LocalDateTime.now());
        atendimento.setStatus("REALIZADO");
        AtendimentoEntity salvo = atendimentoRepository.save(atendimento);

        ProntuarioEntity prontuario = new ProntuarioEntity();
        prontuario.setAtendimentoId(salvo.getId());
        prontuario.setDescricao(descricao);
        prontuario.setDiagnostico(diagnostico);
        prontuario.setObservacoes(observacoes);
        prontuario.setDataCriacao(LocalDateTime.now());
        prontuarioRepository.save(prontuario);

        return salvo;
    }

    public List<AtendimentoEntity> buscarHistoricoPorPaciente(Long pacienteId) {
        return atendimentoRepository.findByPacienteId(pacienteId);
    }
}
