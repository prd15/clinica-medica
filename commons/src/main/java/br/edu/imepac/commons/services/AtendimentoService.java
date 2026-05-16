package br.edu.imepac.commons.services;

import br.edu.imepac.commons.entities.AtendimentoEntity;
import br.edu.imepac.commons.entities.ProntuarioEntity;
import br.edu.imepac.commons.repositories.AtendimentoRepository;
import br.edu.imepac.commons.repositories.ProntuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AtendimentoService {

    private final AtendimentoRepository atendimentoRepository;
    private final ProntuarioRepository prontuarioRepository;

    public AtendimentoService(AtendimentoRepository atendimentoRepository,
                              ProntuarioRepository prontuarioRepository) {
        this.atendimentoRepository = atendimentoRepository;
        this.prontuarioRepository = prontuarioRepository;
    }

    @Transactional
    public AtendimentoEntity registrar(AtendimentoEntity atendimento,
                                       String descricao,
                                       String diagnostico,
                                       String observacoes) {
        atendimento.setDataHora(LocalDateTime.now());
        atendimento.setDescricao(descricao);
        atendimento.setDiagnostico(diagnostico);
        atendimento.setObservacoes(observacoes);
        return atendimentoRepository.save(atendimento);
    }

    public ProntuarioEntity buscarProntuario(Long atendimentoId) {
        AtendimentoEntity atendimento = atendimentoRepository.findById(atendimentoId)
                .orElseThrow(() -> new RuntimeException("Atendimento nao encontrado"));

        ProntuarioEntity prontuario = new ProntuarioEntity();
        prontuario.setAtendimentoId(atendimentoId);
        prontuario.setPacienteId(atendimento.getPacienteId());
        prontuario.setDataCriacao(LocalDateTime.now());
        prontuario.setHistorico(atendimento.getDescricao() + " - " + atendimento.getDiagnostico());
        return prontuarioRepository.save(prontuario);
    }
}
