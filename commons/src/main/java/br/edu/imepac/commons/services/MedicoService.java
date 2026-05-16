package br.edu.imepac.commons.services;

import br.edu.imepac.commons.entities.EspecialidadeEntity;
import br.edu.imepac.commons.entities.MedicoEntity;
import br.edu.imepac.commons.repositories.EspecialidadeRepository;
import br.edu.imepac.commons.repositories.MedicoRepository;
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

    public Optional<MedicoEntity> findById(Long id) {
        return medicoRepository.findById(id);
    }

    public Optional<MedicoEntity> findByCrm(String crm) {
        return medicoRepository.findByCrm(crm);
    }

    public MedicoEntity save(MedicoEntity medico) {
        // garante que nao cadastra dois medicos com o mesmo CRM
        validarCrmDisponivel(medico.getCrm(), null);
        return medicoRepository.save(medico);
    }

    public Optional<MedicoEntity> update(Long id, MedicoEntity dadosAtualizados) {
        return medicoRepository.findById(id).map(existing -> {
            // valida CRM mas ignora o proprio registro (permite manter o mesmo CRM)
            validarCrmDisponivel(dadosAtualizados.getCrm(), id);
            existing.setNome(dadosAtualizados.getNome());
            existing.setCrm(dadosAtualizados.getCrm());
            existing.setSenha(dadosAtualizados.getSenha());
            existing.setTelefone(dadosAtualizados.getTelefone());
            return medicoRepository.save(existing);
        });
    }

    public Optional<MedicoEntity> inativar(Long id) {
        return medicoRepository.findById(id).map(medico -> {
            medico.setAtivo(false);
            return medicoRepository.save(medico);
        });
    }

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
