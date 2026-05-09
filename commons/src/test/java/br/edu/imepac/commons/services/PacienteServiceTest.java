package br.edu.imepac.commons.services;

import br.edu.imepac.commons.entities.PacienteEntity;
import br.edu.imepac.commons.repositories.PacienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// testes do PacienteService — mesma estrutura do ConvenioServiceTest
@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @InjectMocks
    private PacienteService pacienteService;

    @Test
    void findAllDeveRetornarListaDePacientes() {
        List<PacienteEntity> pacientes = List.of(
                new PacienteEntity(1L, "Joao Silva", "123.456.789-00", "(34)99999-0000", "joao@email.com", "Rua A, 100", 1L),
                new PacienteEntity(2L, "Maria Santos", "987.654.321-00", "(34)88888-0000", "maria@email.com", "Rua B, 200", 2L)
        );
        when(pacienteRepository.findAll()).thenReturn(pacientes);

        List<PacienteEntity> resultado = pacienteService.findAll();

        assertEquals(2, resultado.size());
        assertEquals("Joao Silva", resultado.get(0).getNome());
        verify(pacienteRepository).findAll();
    }

    @Test
    void findByIdDeveRetornarPacienteQuandoExistir() {
        PacienteEntity paciente = new PacienteEntity(1L, "Joao Silva", "123.456.789-00", "(34)99999-0000", "joao@email.com", "Rua A, 100", 1L);
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));

        Optional<PacienteEntity> resultado = pacienteService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Joao Silva", resultado.get().getNome());
        verify(pacienteRepository).findById(1L);
    }

    @Test
    void findByIdDeveRetornarVazioQuandoNaoExistir() {
        when(pacienteRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<PacienteEntity> resultado = pacienteService.findById(99L);

        assertTrue(resultado.isEmpty());
        verify(pacienteRepository).findById(99L);
    }

    @Test
    void saveDevePersistirPaciente() {
        PacienteEntity novo = new PacienteEntity(null, "Joao Silva", "123.456.789-00", "(34)99999-0000", "joao@email.com", "Rua A, 100", 1L);
        PacienteEntity salvo = new PacienteEntity(1L, "Joao Silva", "123.456.789-00", "(34)99999-0000", "joao@email.com", "Rua A, 100", 1L);
        when(pacienteRepository.save(any(PacienteEntity.class))).thenReturn(salvo);

        PacienteEntity resultado = pacienteService.save(novo);

        assertNotNull(resultado);
        assertNotNull(resultado.getId());
        assertEquals(1L, resultado.getId());
        assertEquals("Joao Silva", resultado.getNome());
        assertEquals("123.456.789-00", resultado.getCpf());
        verify(pacienteRepository).save(novo);
    }

    @Test
    void updateDeveAtualizarPacienteQuandoExistir() {
        PacienteEntity existente = new PacienteEntity(1L, "Joao Silva", "123.456.789-00", "(34)99999-0000", "joao@email.com", "Rua A, 100", 1L);
        PacienteEntity dadosAtualizados = new PacienteEntity(null, "Joao Atualizado", "123.456.789-00", "(34)77777-0000", "joao.novo@email.com", "Rua C, 300", 2L);

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(pacienteRepository.save(any(PacienteEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<PacienteEntity> resultado = pacienteService.update(1L, dadosAtualizados);

        assertTrue(resultado.isPresent());
        assertEquals("Joao Atualizado", resultado.get().getNome());
        assertEquals("(34)77777-0000", resultado.get().getTelefone());
        assertEquals("joao.novo@email.com", resultado.get().getEmail());
        verify(pacienteRepository).findById(1L);
        verify(pacienteRepository).save(existente);
    }

    @Test
    void updateDeveRetornarVazioQuandoNaoExistir() {
        PacienteEntity dadosAtualizados = new PacienteEntity(null, "Joao Atualizado", "123.456.789-00", "(34)77777-0000", "joao@email.com", "Rua C, 300", 1L);
        when(pacienteRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<PacienteEntity> resultado = pacienteService.update(99L, dadosAtualizados);

        assertTrue(resultado.isEmpty());
        verify(pacienteRepository).findById(99L);
        verify(pacienteRepository, never()).save(any(PacienteEntity.class));
    }

    @Test
    void deleteByIdDeveExcluirQuandoExistir() {
        when(pacienteRepository.existsById(1L)).thenReturn(true);

        boolean removido = pacienteService.deleteById(1L);

        assertTrue(removido);
        verify(pacienteRepository).existsById(1L);
        verify(pacienteRepository).deleteById(1L);
    }

    @Test
    void deleteByIdNaoDeveExcluirQuandoNaoExistir() {
        when(pacienteRepository.existsById(99L)).thenReturn(false);

        boolean removido = pacienteService.deleteById(99L);

        assertFalse(removido);
        verify(pacienteRepository).existsById(99L);
        verify(pacienteRepository, never()).deleteById(anyLong());
    }
}
