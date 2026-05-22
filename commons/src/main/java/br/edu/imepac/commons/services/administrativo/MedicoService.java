package br.edu.imepac.commons.services.administrativo;

import br.edu.imepac.commons.entities.administrativo.EspecialidadeEntity;
import br.edu.imepac.commons.entities.administrativo.MedicoEntity;
import br.edu.imepac.commons.repositories.administrativo.EspecialidadeRepository;
import br.edu.imepac.commons.repositories.administrativo.MedicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MedicoService {

    private final MedicoRepository medicoRepository;
    private final EspecialidadeRepository especialidadeRepository;

    public MedicoService(MedicoRepository medicoRepository, EspecialidadeRepository especialidadeRepository) {
        this.medicoRepository = medicoRepository;
        this.especialidadeRepository = especialidadeRepository;
    }

    @Transactional(readOnly = true)
    public List<MedicoEntity> findAll() {
        return medicoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<MedicoEntity> findByAtivo(Boolean ativo) {
        return medicoRepository.findByAtivo(ativo);
    }

    // readOnly tambem aqui: MedicoResponse expoe especialidades (LAZY) — mantem a sessao
    // aberta no mapeamento e evita LazyInitializationException, igual em findAll/findByAtivo
    // readOnly: MedicoResponse expoe especialidades (LAZY) — mantem a sessao aberta no
    // mapeamento e evita LazyInitializationException, igual em findAll/findByAtivo
    @Transactional(readOnly = true)
    public Optional<MedicoEntity> findById(Long id) {
        return medicoRepository.findById(id);
    }

    @Transactional
    public MedicoEntity save(MedicoEntity medico) {
        // garante que nao cadastra dois medicos com o mesmo CRM
        validarCrmDisponivel(medico.getCrm(), null);
        return medicoRepository.save(medico);
    }

    @Transactional
    public Optional<MedicoEntity> update(Long id, MedicoEntity dadosAtualizados) {
        return medicoRepository.findById(id).map(existing -> {
            validarCrmDisponivel(dadosAtualizados.getCrm(), id);
            existing.setNome(dadosAtualizados.getNome());
            existing.setCrm(dadosAtualizados.getCrm());
            if (dadosAtualizados.getSenha() != null && !dadosAtualizados.getSenha().isBlank()) {
                existing.setSenha(dadosAtualizados.getSenha());
            }
            existing.setTelefone(dadosAtualizados.getTelefone());
            return medicoRepository.save(existing);
        });
    }

    @Transactional
    public Optional<MedicoEntity> inativar(Long id) {
        return medicoRepository.findById(id).map(medico -> {
            medico.setAtivo(false);
            return medicoRepository.save(medico);
        });
    }

    @Transactional
    public Optional<MedicoEntity> associarEspecialidade(Long medicoId, Long especialidadeId) {
        Optional<MedicoEntity> medicoOpt = medicoRepository.findById(medicoId);
        Optional<EspecialidadeEntity> especialidadeOpt = especialidadeRepository.findById(especialidadeId);
        if (medicoOpt.isEmpty() || especialidadeOpt.isEmpty()) {
            return Optional.empty();
        }
        MedicoEntity medico = medicoOpt.get();
        EspecialidadeEntity especialidade = especialidadeOpt.get();
        if (medico.getEspecialidades().contains(especialidade)) {
            throw new IllegalStateException("Especialidade ja associada ao medico");
        }
        medico.getEspecialidades().add(especialidade);
        return Optional.of(medicoRepository.save(medico));
    }

    @Transactional
    public Optional<MedicoEntity> removerEspecialidade(Long medicoId, Long especialidadeId) {
        Optional<MedicoEntity> medicoOpt = medicoRepository.findById(medicoId);
        Optional<EspecialidadeEntity> especialidadeOpt = especialidadeRepository.findById(especialidadeId);
        if (medicoOpt.isEmpty() || especialidadeOpt.isEmpty()) {
            return Optional.empty();
        }
        MedicoEntity medico = medicoOpt.get();
        medico.getEspecialidades().remove(especialidadeOpt.get());
        return Optional.of(medicoRepository.save(medico));
    }

    @Transactional
    public boolean deleteById(Long id) {
        if (medicoRepository.existsById(id)) {
            medicoRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // verifica se o CRM ja pertence a outro medico — mesma logica do usuario em AtendenteService
    private void validarCrmDisponivel(String crm, Long idAtual) {
        medicoRepository.findByCrm(crm)
                .filter(medico -> idAtual == null || !medico.getId().equals(idAtual))
                .ifPresent(medico -> {
                    throw new IllegalStateException("CRM ja cadastrado para outro medico");
                });
    }
}
