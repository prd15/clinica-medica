package br.edu.imepac.commons.services.administrativo;

import br.edu.imepac.commons.entities.administrativo.EspecialidadeEntity;
import br.edu.imepac.commons.entities.administrativo.MedicoEntity;
import br.edu.imepac.commons.repositories.administrativo.EspecialidadeRepository;
import br.edu.imepac.commons.repositories.administrativo.MedicoRepository;
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
    void findByIdDeveRetornarVazioQuandoNaoExistir() {
        when(medicoRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<MedicoEntity> resultado = medicoService.findById(99L);

        assertTrue(resultado.isEmpty());
        verify(medicoRepository).findById(99L);
    }

    @Test
    void saveDevePersistirMedico() {
        MedicoEntity novo = buildMedico(null, "Dr. João", "CRM-1");
        MedicoEntity salvo = buildMedico(1L, "Dr. João", "CRM-1");
        when(medicoRepository.findByCrm("CRM-1")).thenReturn(Optional.empty());
        when(medicoRepository.save(any(MedicoEntity.class))).thenReturn(salvo);

        MedicoEntity resultado = medicoService.save(novo);

        assertNotNull(resultado.getId());
        assertEquals("Dr. João", resultado.getNome());
        verify(medicoRepository).save(novo);
    }

    @Test
    void saveDeveLancarExcecaoQuandoCrmJaCadastrado() {
        MedicoEntity existente = buildMedico(2L, "Outro", "CRM-1");
        MedicoEntity novo = buildMedico(null, "Dr. João", "CRM-1");
        when(medicoRepository.findByCrm("CRM-1")).thenReturn(Optional.of(existente));

        assertThrows(IllegalStateException.class, () -> medicoService.save(novo));
        verify(medicoRepository, never()).save(any());
    }

    @Test
    void updateDeveAtualizarMedicoQuandoExistir() {
        MedicoEntity existente = buildMedico(1L, "Dr. João", "CRM-1");
        MedicoEntity dados = buildMedico(null, "Dr. João Silva", "CRM-1");

        when(medicoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(medicoRepository.findByCrm("CRM-1")).thenReturn(Optional.of(existente));
        when(medicoRepository.save(any(MedicoEntity.class))).thenAnswer(i -> i.getArgument(0));

        Optional<MedicoEntity> resultado = medicoService.update(1L, dados);

        assertTrue(resultado.isPresent());
        assertEquals("Dr. João Silva", resultado.get().getNome());
        verify(medicoRepository).findById(1L);
        verify(medicoRepository).save(existente);
    }

    @Test
    void updateDevePermitirMesmosCrmDoProprioMedico() {
        // atualizar nome mantendo o mesmo CRM nao deve lancar excecao
        MedicoEntity existente = buildMedico(1L, "Dr. João", "CRM-1");
        MedicoEntity dados = buildMedico(null, "Dr. João Novo", "CRM-1");

        when(medicoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(medicoRepository.findByCrm("CRM-1")).thenReturn(Optional.of(existente));
        when(medicoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Optional<MedicoEntity> resultado = medicoService.update(1L, dados);

        assertTrue(resultado.isPresent());
        assertEquals("Dr. João Novo", resultado.get().getNome());
    }

    @Test
    void updateDeveLancarExcecaoQuandoCrmPertenceAOutroMedico() {
        MedicoEntity existente = buildMedico(1L, "Dr. João", "CRM-1");
        MedicoEntity outraMedico = buildMedico(2L, "Dra. Ana", "CRM-2");
        // tenta mudar o CRM-1 para CRM-2, mas CRM-2 ja pertence a outraMedico
        MedicoEntity dados = buildMedico(null, "Dr. João", "CRM-2");

        when(medicoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(medicoRepository.findByCrm("CRM-2")).thenReturn(Optional.of(outraMedico));

        assertThrows(IllegalStateException.class, () -> medicoService.update(1L, dados));
        verify(medicoRepository, never()).save(any());
    }

    @Test
    void updateDeveRetornarVazioQuandoNaoExistir() {
        when(medicoRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<MedicoEntity> resultado = medicoService.update(99L, buildMedico(null, "X", "Y"));

        assertTrue(resultado.isEmpty());
        verify(medicoRepository, never()).save(any());
    }

    @Test
    void updateNaoDeveSobrescreverSenhaQuandoVierNula() {
        MedicoEntity existente = buildMedico(1L, "Dr. João", "CRM-1");
        existente.setSenha("senhaOriginal");
        MedicoEntity dados = buildMedico(null, "Dr. João Atualizado", "CRM-1");
        dados.setSenha(null);

        when(medicoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(medicoRepository.findByCrm("CRM-1")).thenReturn(Optional.of(existente));
        when(medicoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Optional<MedicoEntity> resultado = medicoService.update(1L, dados);

        assertTrue(resultado.isPresent());
        assertEquals("senhaOriginal", resultado.get().getSenha());
    }

    @Test
    void updateNaoDeveSobrescreverSenhaQuandoVierEmBranco() {
        MedicoEntity existente = buildMedico(1L, "Dr. João", "CRM-1");
        existente.setSenha("senhaOriginal");
        MedicoEntity dados = buildMedico(null, "Dr. João Atualizado", "CRM-1");
        dados.setSenha("   ");

        when(medicoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(medicoRepository.findByCrm("CRM-1")).thenReturn(Optional.of(existente));
        when(medicoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Optional<MedicoEntity> resultado = medicoService.update(1L, dados);

        assertTrue(resultado.isPresent());
        assertEquals("senhaOriginal", resultado.get().getSenha());
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

    // ── FIND BY ATIVO ─────────────────────────────────────────────────────────

    @Test
    void findByAtivoDeveRetornarApenasMedicosAtivos() {
        List<MedicoEntity> ativos = List.of(buildMedico(1L, "Dr. João", "CRM-1"));
        when(medicoRepository.findByAtivo(true)).thenReturn(ativos);

        List<MedicoEntity> resultado = medicoService.findByAtivo(true);

        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getAtivo());
        verify(medicoRepository).findByAtivo(true);
    }

    @Test
    void findByAtivoDeveRetornarApenasMedicosInativos() {
        MedicoEntity inativo = buildMedico(2L, "Dra. Ana", "CRM-2");
        inativo.setAtivo(false);
        when(medicoRepository.findByAtivo(false)).thenReturn(List.of(inativo));

        List<MedicoEntity> resultado = medicoService.findByAtivo(false);

        assertEquals(1, resultado.size());
        assertFalse(resultado.get(0).getAtivo());
        verify(medicoRepository).findByAtivo(false);
    }

    @Test
    void findByAtivoDeveRetornarListaVaziaQuandoNenhumMedico() {
        when(medicoRepository.findByAtivo(true)).thenReturn(List.of());

        List<MedicoEntity> resultado = medicoService.findByAtivo(true);

        assertTrue(resultado.isEmpty());
        verify(medicoRepository).findByAtivo(true);
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
    void associarEspecialidadeDeveLancarExcecaoQuandoJaAssociada() {
        EspecialidadeEntity especialidade = new EspecialidadeEntity(1L, "Cardiologia", "Desc");
        MedicoEntity medico = buildMedico(1L, "Dr. João", "CRM-1");
        medico.getEspecialidades().add(especialidade);

        when(medicoRepository.findById(1L)).thenReturn(Optional.of(medico));
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(especialidade));

        assertThrows(IllegalStateException.class,
                () -> medicoService.associarEspecialidade(1L, 1L));
        verify(medicoRepository, never()).save(any());
    }

    @Test
    void associarEspecialidadeDeveRetornarVazioQuandoMedicoNaoExistir() {
        when(medicoRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<MedicoEntity> resultado = medicoService.associarEspecialidade(99L, 1L);

        assertTrue(resultado.isEmpty());
        verify(medicoRepository, never()).save(any());
    }

    @Test
    void associarEspecialidadeDeveRetornarVazioQuandoEspecialidadeNaoExistir() {
        MedicoEntity medico = buildMedico(1L, "Dr. João", "CRM-1");
        when(medicoRepository.findById(1L)).thenReturn(Optional.of(medico));
        when(especialidadeRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<MedicoEntity> resultado = medicoService.associarEspecialidade(1L, 99L);

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

    @Test
    void removerEspecialidadeDeveRetornarVazioQuandoEspecialidadeNaoExistir() {
        MedicoEntity medico = buildMedico(1L, "Dr. João", "CRM-1");
        when(medicoRepository.findById(1L)).thenReturn(Optional.of(medico));
        when(especialidadeRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<MedicoEntity> resultado = medicoService.removerEspecialidade(1L, 99L);

        assertTrue(resultado.isEmpty());
        verify(medicoRepository, never()).save(any());
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
