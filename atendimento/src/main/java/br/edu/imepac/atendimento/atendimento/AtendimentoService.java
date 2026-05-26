package br.edu.imepac.atendimento.atendimento;

import br.edu.imepac.atendimento.outbox.OutboxEvent;
import br.edu.imepac.atendimento.outbox.OutboxEventRepository;
import br.edu.imepac.atendimento.prontuario.ProntuarioEntity;
import br.edu.imepac.atendimento.prontuario.ProntuarioRepository;
import br.edu.imepac.commons.exceptions.BusinessException;
import br.edu.imepac.commons.exceptions.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AtendimentoService {

    // evento de integracao gravado no outbox quando um atendimento e registrado
    private static final String AGGREGATE_CONSULTA = "CONSULTA";
    private static final String EVENT_CONFIRMACAO_REALIZACAO = "CONFIRMACAO_REALIZACAO";

    private final AtendimentoRepository atendimentoRepository;
    private final ProntuarioRepository prontuarioRepository;
    private final OutboxEventRepository outboxEventRepository;

    public AtendimentoService(AtendimentoRepository atendimentoRepository,
                              ProntuarioRepository prontuarioRepository,
                              OutboxEventRepository outboxEventRepository) {
        this.atendimentoRepository = atendimentoRepository;
        this.prontuarioRepository = prontuarioRepository;
        this.outboxEventRepository = outboxEventRepository;
    }

    @Transactional
    public AtendimentoEntity registrar(AtendimentoEntity atendimento,
                                       String descricao,
                                       String diagnostico,
                                       String observacoes) {
        // idempotencia: uma consulta so pode ter um atendimento
        if (atendimentoRepository.existsByConsultaId(atendimento.getConsultaId())) {
            throw new BusinessException(
                    "Ja existe atendimento registrado para a consulta " + atendimento.getConsultaId());
        }
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

        // Outbox: enfileira a notificacao de "consulta realizada" na MESMA transacao.
        // Se o commit acontece, o evento existe e sera entregue pelo OutboxScheduler;
        // se a transacao reverte, o evento some junto — nada de notificacao orfa.
        Long consultaId = salvo.getConsultaId();
        outboxEventRepository.save(OutboxEvent.pendente(
                AGGREGATE_CONSULTA,
                String.valueOf(consultaId),
                EVENT_CONFIRMACAO_REALIZACAO,
                "{\"consultaId\":" + consultaId + "}"));

        return salvo;
    }

    @Transactional(readOnly = true)
    public List<AtendimentoEntity> buscarHistoricoPorPaciente(Long pacienteId) {
        return atendimentoRepository.findByPacienteId(pacienteId);
    }

    @Transactional(readOnly = true)
    public AtendimentoEntity buscarPorConsulta(Long consultaId) {
        return atendimentoRepository.findByConsultaId(consultaId)
                .orElseThrow(() -> new EntityNotFoundException("Atendimento nao encontrado para consultaId: " + consultaId));
    }
}
