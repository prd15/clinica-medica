package br.edu.imepac.commons.services;

import br.edu.imepac.commons.entities.AtendimentoEntity;
import br.edu.imepac.commons.entities.AnotacaoEntity;
import br.edu.imepac.commons.entities.ProntuarioEntity;
import br.edu.imepac.commons.entities.SolicitacaoExameEntity;
import br.edu.imepac.commons.entities.StatusAtendimento;
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

    @Transactional
    public AtendimentoEntity registrar(AtendimentoEntity atendimento,
                                       String descricao,
                                       String diagnostico,
                                       String observacoes) {
        atendimento.setDataHora(LocalDateTime.now());
        atendimento.setStatus(StatusAtendimento.REALIZADO);
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

    @Transactional(readOnly = true)
    public List<AtendimentoEntity> buscarHistoricoPorPaciente(Long pacienteId) {
        return atendimentoRepository.findByPacienteId(pacienteId);
    }

    @Transactional(readOnly = true)
    public AtendimentoEntity buscarPorConsulta(Long consultaId) {
        return atendimentoRepository.findByConsultaId(consultaId)
                .orElseThrow(() -> new NoSuchElementException("Atendimento nao encontrado para consultaId: " + consultaId));
    }

    public AnotacaoEntity adicionarAnotacao(Long atendimentoId, String texto) {
        ProntuarioEntity prontuario = prontuarioRepository
                .findFirstByAtendimentoIdOrderByIdDesc(atendimentoId)
                .orElseThrow(() -> new NoSuchElementException("Prontuario nao encontrado para atendimentoId: " + atendimentoId));

        AnotacaoEntity anotacao = new AnotacaoEntity();
        anotacao.setProntuarioId(prontuario.getId());
        anotacao.setTexto(texto);
        anotacao.setDataCriacao(LocalDateTime.now());
        return anotacaoRepository.save(anotacao);
    }

    public SolicitacaoExameEntity solicitarExame(Long atendimentoId, String descricao, String tipo) {
        atendimentoRepository.findById(atendimentoId)
                .orElseThrow(() -> new NoSuchElementException("Atendimento nao encontrado: " + atendimentoId));

        SolicitacaoExameEntity exame = new SolicitacaoExameEntity();
        exame.setAtendimentoId(atendimentoId);
        exame.setDescricao(descricao);
        exame.setTipo(tipo);
        exame.setDataSolicitacao(LocalDateTime.now());
        return exameRepository.save(exame);
    }

    @Transactional(readOnly = true)
    public ProntuarioEntity buscarProntuario(Long atendimentoId) {
        return prontuarioRepository
                .findFirstByAtendimentoIdOrderByIdDesc(atendimentoId)
                .orElseThrow(() -> new NoSuchElementException("Prontuario nao encontrado para atendimentoId: " + atendimentoId));
    }

    @Transactional(readOnly = true)
    public List<AnotacaoEntity> listarAnotacoes(Long atendimentoId) {
        ProntuarioEntity prontuario = prontuarioRepository
                .findFirstByAtendimentoIdOrderByIdDesc(atendimentoId)
                .orElseThrow(() -> new NoSuchElementException("Prontuario nao encontrado para atendimentoId: " + atendimentoId));
        return anotacaoRepository.findByProntuarioId(prontuario.getId());
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoExameEntity> listarExames(Long atendimentoId) {
        atendimentoRepository.findById(atendimentoId)
                .orElseThrow(() -> new NoSuchElementException("Atendimento nao encontrado: " + atendimentoId));
        return exameRepository.findByAtendimentoId(atendimentoId);
    }
}
