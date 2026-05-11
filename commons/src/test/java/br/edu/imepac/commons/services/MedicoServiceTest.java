package br.edu.imepac.commons.services;

import br.edu.imepac.commons.entities.EspecialidadeEntity;
import br.edu.imepac.commons.entities.MedicoEntity;
import br.edu.imepac.commons.repositories.EspecialidadeRepository;
import br.edu.imepac.commons.repositories.MedicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicoServiceTest {

    @Mock
    private MedicoRepository medicoRepository;

    @Mock
    private EspecialidadeRepository especialidadeRepository;

    @InjectMocks
    private MedicoService medicoService;

    // ── CRUD ────────────────────────────────────────────────────────────────

    @Test
    void findAllDeveRetornarListaDeMedicos() {
        List<MedicoEntity> medicos = List.of(
                buildMedico(1L, "Dr. João", "CRM-1"),
                buildMedico(2L, "Dra. Ana", "CRM-2")
        );
        when(medicoRepository.findAll()).thenReturn(medicos);

        List<MedicoEntity> resultado = medicoService.findAll();

        assertEquals(2, resultado.size());
        assertEquals("Dr. João", resultado.get(0).getNome());
        verify(medicoRepository).findAll();
    }

    @Test
    void findByIdDeveRetornarMedicoQuandoExistir() {
        MedicoEntity medico = buildMedico(1L, "Dr. João", "CRM-1");
        when(medicoRepository.findById(1L)).thenReturn(Optional.of(medico));

        Optional<MedicoEntity> resultado = medicoService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Dr. João", resultado.get().getNome());
        verify(medicoRepository).findById(1L);
    }

    @Test
    void saveDevePersistirMedico() {
        MedicoEntity novo = buildMedico(null, "Dr. João", "CRM-1");
        MedicoEntity salvo = buildMedico(1L, "Dr. João", "CRM-1");
        when(medicoRepository.save(any(MedicoEntity.class))).thenReturn(salvo);

        MedicoEntity resultado = medicoService.save(novo);

        assertNotNull(resultado.getId());
        assertEquals("Dr. João", resultado.getNome());
        verify(medicoRepository).save(novo);
    }

    @Test
    void updateDeveAtualizarMedicoQuandoExistir() {
        MedicoEntity existente = buildMedico(1L, "Dr. João", "CRM-1");
        MedicoEntity dados = buildMedico(null, "Dr. João Silva", "CRM-1");

        when(medicoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(medicoRepository.save(any(MedicoEntity.class))).thenAnswer(i -> i.getArgument(0));

        Optional<MedicoEntity> resultado = medicoService.update(1L, dados);

        assertTrue(resultado.isPresent());
        assertEquals("Dr. João Silva", resultado.get().getNome());
        verify(medicoRepository).findById(1L);
        verify(medicoRepository).save(existente);
    }

    @Test
    void updateDeveRetornarVazioQuandoNaoExistir() {
        when(medicoRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<MedicoEntity> resultado = medicoService.update(99L, buildMedico(null, "X", "Y"));

        assertTrue(resultado.isEmpty());
        verify(medicoRepository, never()).save(any());
    }

    @Test
    void deleteByIdDeveExcluirQuandoExistir() {
        when(medicoRepository.existsById(1L)).thenReturn(true);

        boolean removido = medicoService.deleteById(1L);

        assertTrue(removido);
        verify(medicoRepository).deleteById(1L);
    }

    @Test
    void deleteByIdNaoDeveExcluirQuandoNaoExistir() {
        when(medicoRepository.existsById(99L)).thenReturn(false);

        boolean removido = medicoService.deleteById(99L);

        assertFalse(removido);
        verify(medicoRepository, never()).deleteById(anyLong());
    }

    // ── INATIVAR ─────────────────────────────────────────────────────────────

    @Test
    void inativarDeveDefinirAtivoComoFalso() {
        MedicoEntity medico = buildMedico(1L, "Dr. João", "CRM-1");
        medico.setAtivo(true);
        when(medicoRepository.findById(1L)).thenReturn(Optional.of(medico));
        when(medicoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Optional<MedicoEntity> resultado = medicoService.inativar(1L);

        assertTrue(resultado.isPresent());
        assertFalse(resultado.get().getAtivo());
        verify(medicoRepository).save(medico);
    }

    @Test
    void inativarDeveRetornarVazioQuandoNaoExistir() {
        when(medicoRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<MedicoEntity> resultado = medicoService.inativar(99L);

        assertTrue(resultado.isEmpty());
        verify(medicoRepository, never()).save(any());
    }

    // ── ESPECIALIDADES ────────────────────────────────────────────────────────

    @Test
    void associarEspecialidadeDeveAdicionarQuandoAmbosExistirem() {
        MedicoEntity medico = buildMedico(1L, "Dr. João", "CRM-1");
        EspecialidadeEntity especialidade = new EspecialidadeEntity(1L, "Cardiologia", "Desc");

        when(medicoRepository.findById(1L)).thenReturn(Optional.of(medico));
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(especialidade));
        when(medicoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Optional<MedicoEntity> resultado = medicoService.associarEspecialidade(1L, 1L);

        assertTrue(resultado.isPresent());
        assertTrue(resultado.get().getEspecialidades().contains(especialidade));
        verify(medicoRepository).save(medico);
    }

    @Test
    void associarEspecialidadeDeveRetornarVazioQuandoMedicoNaoExistir() {
        when(medicoRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<MedicoEntity> resultado = medicoService.associarEspecialidade(99L, 1L);

        assertTrue(resultado.isEmpty());
        verify(medicoRepository, never()).save(any());
    }

    @Test
    void removerEspecialidadeDeveRemoverQuandoAmbosExistirem() {
        EspecialidadeEntity especialidade = new EspecialidadeEntity(1L, "Cardiologia", "Desc");
        MedicoEntity medico = buildMedico(1L, "Dr. João", "CRM-1");
        medico.getEspecialidades().add(especialidade);

        when(medicoRepository.findById(1L)).thenReturn(Optional.of(medico));
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(especialidade));
        when(medicoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Optional<MedicoEntity> resultado = medicoService.removerEspecialidade(1L, 1L);

        assertTrue(resultado.isPresent());
        assertFalse(resultado.get().getEspecialidades().contains(especialidade));
        verify(medicoRepository).save(medico);
    }

    // ── HELPER ───────────────────────────────────────────────────────────────

    private static MedicoEntity buildMedico(Long id, String nome, String crm) {
        MedicoEntity m = new MedicoEntity();
        m.setId(id);
        m.setNome(nome);
        m.setCrm(crm);
        m.setSenha("senha123");
        m.setAtivo(true);
        m.setEspecialidades(new HashSet<>());
        return m;
    }
}
