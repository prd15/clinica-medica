package br.edu.imepac.atendimento.prontuario;

import br.edu.imepac.commons.exceptions.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProntuarioService {

    private final ProntuarioRepository prontuarioRepository;

    public ProntuarioService(ProntuarioRepository prontuarioRepository) {
        this.prontuarioRepository = prontuarioRepository;
    }

    @Transactional(readOnly = true)
    public ProntuarioEntity buscarProntuario(Long atendimentoId) {
        return prontuarioRepository
                .findByAtendimentoId(atendimentoId)
                .orElseThrow(() -> new EntityNotFoundException("Prontuario nao encontrado para atendimentoId: " + atendimentoId));
    }
}
