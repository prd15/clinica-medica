package br.edu.imepac.administrativo.convenio;

import br.edu.imepac.commons.exceptions.BusinessException;
import br.edu.imepac.commons.exceptions.EntityNotFoundException;
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
class ConvenioServiceTest {

    @Mock
    private ConvenioRepository convenioRepository;

    @InjectMocks
    private ConvenioService convenioService;

    @Test
    void findAllDeveRetornarListaDeConvenios() {
        List<ConvenioEntity> convenios = List.of(
                new ConvenioEntity(1L, "Unimed", "Plano regional", "12.345.678/0001-99", "(34)99999-0000", true),
                new ConvenioEntity(2L, "Amil", "Plano nacional", "98.765.432/0001-11", "(34)88888-0000", true)
        );
        when(convenioRepository.findAll()).thenReturn(convenios);

        List<ConvenioEntity> resultado = convenioService.findAll();

        assertEquals(2, resultado.size());
        assertEquals("Unimed", resultado.get(0).getNome());
        verify(convenioRepository).findAll();
    }

    @Test
    void findByIdDeveRetornarConvenioQuandoExistir() {
        ConvenioEntity convenio = new ConvenioEntity(1L, "Unimed", "Plano regional", "12.345.678/0001-99", "(34)99999-0000", true);
        when(convenioRepository.findById(1L)).thenReturn(Optional.of(convenio));

        Optional<ConvenioEntity> resultado = convenioService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Unimed", resultado.get().getNome());
        verify(convenioRepository).findById(1L);
    }

    @Test
    void saveDevePersistirConvenio() {
        ConvenioEntity novo = new ConvenioEntity(null, "Unimed", "Plano regional", "12.345.678/0001-99", "(34)99999-0000", true);
        ConvenioEntity salvo = new ConvenioEntity(1L, "Unimed", "Plano regional", "12.345.678/0001-99", "(34)99999-0000", true);
        when(convenioRepository.save(any(ConvenioEntity.class))).thenReturn(salvo);

        ConvenioEntity resultado = convenioService.save(novo);

        assertNotNull(resultado);
        assertNotNull(resultado.getId());
        assertEquals(1L, resultado.getId());
        verify(convenioRepository).save(novo);
        assertEquals("Unimed", resultado.getNome());
    }

    @Test
    void updateDeveAtualizarConvenioQuandoExistir() {
        ConvenioEntity existente = new ConvenioEntity(1L, "Unimed", "Antigo", "12.345.678/0001-99", "(34)99999-0000", true);
        ConvenioEntity dadosAtualizados = new ConvenioEntity(null, "Unimed Atualizado", "Novo", "12.345.678/0001-99", "(34)77777-0000", true);

        when(convenioRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(convenioRepository.save(any(ConvenioEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<ConvenioEntity> resultado = convenioService.update(1L, dadosAtualizados);

        assertTrue(resultado.isPresent());
        assertEquals("Unimed Atualizado", resultado.get().getNome());
        verify(convenioRepository).findById(1L);
        verify(convenioRepository).save(existente);
    }

    @Test
    void updateDeveRetornarVazioQuandoNaoExistir() {
        ConvenioEntity dadosAtualizados = new ConvenioEntity(null, "Unimed Atualizado", "Novo", "12.345.678/0001-99", null, true);
        when(convenioRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<ConvenioEntity> resultado = convenioService.update(99L, dadosAtualizados);

        assertTrue(resultado.isEmpty());
        verify(convenioRepository, never()).save(any(ConvenioEntity.class));
    }

    @Test
    void deleteByIdDeveExcluirQuandoExistir() {
        when(convenioRepository.existsById(1L)).thenReturn(true);

        boolean removido = convenioService.deleteById(1L);

        assertTrue(removido);
        verify(convenioRepository).deleteById(1L);
    }

    @Test
    void deleteByIdNaoDeveExcluirQuandoNaoExistir() {
        when(convenioRepository.existsById(99L)).thenReturn(false);

        boolean removido = convenioService.deleteById(99L);

        assertFalse(removido);
        verify(convenioRepository, never()).deleteById(anyLong());
    }

    @Test
    void findByAtivoDeveRetornarApenasAtivos() {
        List<ConvenioEntity> ativos = List.of(
                new ConvenioEntity(1L, "Unimed", "Plano regional", "12.345.678/0001-99", "(34)99999-0000", true)
        );
        when(convenioRepository.findByAtivo(true)).thenReturn(ativos);

        List<ConvenioEntity> resultado = convenioService.findByAtivo(true);

        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getAtivo());
    }

    @Test
    void alterarStatusDeveAtivarConvenio() {
        ConvenioEntity convenio = new ConvenioEntity(1L, "Unimed", "Plano", "12.345.678/0001-99", "(34)99999-0000", false);
        when(convenioRepository.findById(1L)).thenReturn(Optional.of(convenio));
        when(convenioRepository.save(any(ConvenioEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConvenioEntity resultado = convenioService.alterarStatus(1L, true);

        assertTrue(resultado.getAtivo());
    }

    @Test
    void alterarStatusDeveLancarEntityNotFound_quandoConvenioNaoEncontrado() {
        when(convenioRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> convenioService.alterarStatus(99L, false));

        assertTrue(ex.getMessage().contains("99"));
        verify(convenioRepository, never()).save(any(ConvenioEntity.class));
    }

    @Test
    void saveDeveLancarBusinessException_quandoNomeJaCadastrado() {
        ConvenioEntity novo = new ConvenioEntity(null, "Unimed", "Plano", "12.345.678/0001-99", null, true);
        ConvenioEntity existente = new ConvenioEntity(7L, "Unimed", "Outro", "99.999.999/0001-99", null, true);
        when(convenioRepository.findByNomeIgnoreCase("Unimed")).thenReturn(Optional.of(existente));

        assertThrows(BusinessException.class, () -> convenioService.save(novo));
        verify(convenioRepository, never()).save(any(ConvenioEntity.class));
    }
}
