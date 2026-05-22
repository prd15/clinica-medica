package br.edu.imepac.commons.services.administrativo;

import br.edu.imepac.commons.entities.administrativo.EspecialidadeEntity;
import br.edu.imepac.commons.repositories.administrativo.EspecialidadeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EspecialidadeServiceTest {

    @Mock
    private EspecialidadeRepository especialidadeRepository;

    @InjectMocks
    private EspecialidadeService especialidadeService;

    @Test
    void findAllDeveRetornarListaDeEspecialidades() {
        List<EspecialidadeEntity> especialidades = List.of(
                new EspecialidadeEntity(1L, "Cardiologia", "Doenças do coração"),
                new EspecialidadeEntity(2L, "Ortopedia", "Sistema musculoesquelético")
        );
        when(especialidadeRepository.findAll()).thenReturn(especialidades);

        List<EspecialidadeEntity> resultado = especialidadeService.findAll();

        assertEquals(2, resultado.size());
        assertEquals("Cardiologia", resultado.get(0).getNome());
        verify(especialidadeRepository).findAll();
    }

    @Test
    void findByIdDeveRetornarEspecialidadeQuandoExistir() {
        EspecialidadeEntity especialidade = new EspecialidadeEntity(1L, "Cardiologia", "Doenças do coração");
        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(especialidade));

        Optional<EspecialidadeEntity> resultado = especialidadeService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Cardiologia", resultado.get().getNome());
        verify(especialidadeRepository).findById(1L);
    }

    @Test
    void saveDevePersistirEspecialidade() {
        EspecialidadeEntity nova = new EspecialidadeEntity(null, "Cardiologia", "Doenças do coração");
        EspecialidadeEntity salva = new EspecialidadeEntity(1L, "Cardiologia", "Doenças do coração");
        when(especialidadeRepository.save(any(EspecialidadeEntity.class))).thenReturn(salva);

        EspecialidadeEntity resultado = especialidadeService.save(nova);

        assertNotNull(resultado);
        assertNotNull(resultado.getId());
        assertEquals(1L, resultado.getId());
        assertEquals("Cardiologia", resultado.getNome());
        assertEquals("Doenças do coração", resultado.getDescricao());
        verify(especialidadeRepository).save(nova);
    }

    @Test
    void updateDeveAtualizarEspecialidadeQuandoExistir() {
        EspecialidadeEntity existente = new EspecialidadeEntity(1L, "Cardiologia", "Antigo");
        EspecialidadeEntity dadosAtualizados = new EspecialidadeEntity(null, "Cardiologia Pediátrica", "Novo");

        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(especialidadeRepository.save(any(EspecialidadeEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<EspecialidadeEntity> resultado = especialidadeService.update(1L, dadosAtualizados);

        assertTrue(resultado.isPresent());
        assertEquals("Cardiologia Pediátrica", resultado.get().getNome());
        assertEquals("Novo", resultado.get().getDescricao());
        verify(especialidadeRepository).findById(1L);
        verify(especialidadeRepository).save(existente);
    }

    @Test
    void updateDeveRetornarVazioQuandoNaoExistir() {
        EspecialidadeEntity dadosAtualizados = new EspecialidadeEntity(null, "Cardiologia Pediátrica", "Novo");
        when(especialidadeRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<EspecialidadeEntity> resultado = especialidadeService.update(99L, dadosAtualizados);

        assertTrue(resultado.isEmpty());
        verify(especialidadeRepository).findById(99L);
        verify(especialidadeRepository, never()).save(any(EspecialidadeEntity.class));
    }

    @Test
    void deleteByIdDeveExcluirQuandoExistir() {
        when(especialidadeRepository.existsById(1L)).thenReturn(true);

        boolean removido = especialidadeService.deleteById(1L);

        assertTrue(removido);
        verify(especialidadeRepository).existsById(1L);
        verify(especialidadeRepository).deleteById(1L);
    }

    @Test
    void deleteByIdNaoDeveExcluirQuandoNaoExistir() {
        when(especialidadeRepository.existsById(99L)).thenReturn(false);

        boolean removido = especialidadeService.deleteById(99L);

        assertFalse(removido);
        verify(especialidadeRepository).existsById(99L);
        verify(especialidadeRepository, never()).deleteById(anyLong());
    }

    // save com nome ja em uso — deve lancar IllegalStateException
    @Test
    void saveDeveLancarQuandoNomeJaCadastrado() {
        EspecialidadeEntity nova = new EspecialidadeEntity(null, "Cardiologia", "Doencas do coracao");
        EspecialidadeEntity existente = new EspecialidadeEntity(7L, "Cardiologia", "Outra desc");
        when(especialidadeRepository.findByNome("Cardiologia")).thenReturn(Optional.of(existente));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> especialidadeService.save(nova));
        assertTrue(ex.getMessage().toLowerCase().contains("especialidade"));
        verify(especialidadeRepository, never()).save(any(EspecialidadeEntity.class));
    }

    // update mantendo o mesmo nome — deve passar
    @Test
    void updateDevePermitirManterMesmoNome() {
        EspecialidadeEntity existente = new EspecialidadeEntity(1L, "Cardiologia", "Antigo");
        EspecialidadeEntity dadosAtualizados = new EspecialidadeEntity(null, "Cardiologia", "Novo");

        when(especialidadeRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(especialidadeRepository.findByNome("Cardiologia")).thenReturn(Optional.of(existente));
        when(especialidadeRepository.save(any(EspecialidadeEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<EspecialidadeEntity> resultado = especialidadeService.update(1L, dadosAtualizados);

        assertTrue(resultado.isPresent());
        assertEquals("Novo", resultado.get().getDescricao());
    }
}
