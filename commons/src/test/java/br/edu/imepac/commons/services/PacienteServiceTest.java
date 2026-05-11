package br.edu.imepac.commons.services;

import br.edu.imepac.commons.entities.PacienteEntity;
import br.edu.imepac.commons.repositories.PacienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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
                new PacienteEntity(1L, "Joao Silva", "123.456.789-00", LocalDate.of(1990, 5, 15), "(34)99999-0000", "joao@email.com", "Rua A, 100", 1L),
                new PacienteEntity(2L, "Maria Santos", "987.654.321-00", LocalDate.of(1985, 3, 20), "(34)88888-0000", "maria@email.com", "Rua B, 200", 2L)
        );
        when(pacienteRepository.findAll()).thenReturn(pacientes);

        List<PacienteEntity> resultado = pacienteService.findAll();

        assertEquals(2, resultado.size());
        assertEquals("Joao Silva", resultado.get(0).getNome());
        verify(pacienteRepository).findAll();
    }

    @Test
    void findByIdDeveRetornarPacienteQuandoExistir() {
        PacienteEntity paciente = new PacienteEntity(1L, "Joao Silva", "123.456.789-00", LocalDate.of(1990, 5, 15), "(34)99999-0000", "joao@email.com", "Rua A, 100", 1L);
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
        PacienteEntity novo = new PacienteEntity(null, "Joao Silva", "123.456.789-00", LocalDate.of(1990, 5, 15), "(34)99999-0000", "joao@email.com", "Rua A, 100", 1L);
        PacienteEntity salvo = new PacienteEntity(1L, "Joao Silva", "123.456.789-00", LocalDate.of(1990, 5, 15), "(34)99999-0000", "joao@email.com", "Rua A, 100", 1L);
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
        PacienteEntity existente = new PacienteEntity(1L, "Joao Silva", "123.456.789-00", LocalDate.of(1990, 5, 15), "(34)99999-0000", "joao@email.com", "Rua A, 100", 1L);
        PacienteEntity dadosAtualizados = new PacienteEntity(null, "Joao Atualizado", "123.456.789-00", LocalDate.of(1990, 5, 15), "(34)77777-0000", "joao.novo@email.com", "Rua C, 300", 2L);

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
        PacienteEntity dadosAtualizados = new PacienteEntity(null, "Joao Atualizado", "123.456.789-00", null, "(34)77777-0000", "joao@email.com", "Rua C, 300", 1L);
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

    // com nome informado, nao deve chamar findAll nem findByCpf
    @Test
    void buscarComFiltros_deveUsarNome_quandoNomeInformado() {
        List<PacienteEntity> lista = List.of(
                new PacienteEntity(1L, "Joao Silva", "123.456.789-00", LocalDate.of(1990, 5, 15), "(34)99999-0000", "joao@email.com", "Rua A", 1L)
        );
        when(pacienteRepository.findByNomeContainingIgnoreCase("joao")).thenReturn(lista);

        List<PacienteEntity> resultado = pacienteService.buscarComFiltros("joao", null, null);

        assertEquals(1, resultado.size());
        verify(pacienteRepository).findByNomeContainingIgnoreCase("joao");
        verify(pacienteRepository, never()).findByCpf(any());
        verify(pacienteRepository, never()).findAll();
    }

    @Test
    void buscarComFiltros_deveUsarCpf_quandoCpfInformado() {
        PacienteEntity paciente = new PacienteEntity(1L, "Joao Silva", "123.456.789-00", LocalDate.of(1990, 5, 15), "(34)99999-0000", "joao@email.com", "Rua A", 1L);
        when(pacienteRepository.findByCpf("123.456.789-00")).thenReturn(Optional.of(paciente));

        List<PacienteEntity> resultado = pacienteService.buscarComFiltros(null, "123.456.789-00", null);

        assertEquals(1, resultado.size());
        verify(pacienteRepository).findByCpf("123.456.789-00");
        verify(pacienteRepository, never()).findAll();
    }

    // convenioId como terceiro filtro na prioridade
    @Test
    void buscarComFiltros_deveUsarConvenioId_quandoConvenioIdInformado() {
        List<PacienteEntity> lista = List.of(
                new PacienteEntity(1L, "Joao Silva", "123.456.789-00", LocalDate.of(1990, 5, 15), "(34)99999-0000", "joao@email.com", "Rua A", 2L)
        );
        when(pacienteRepository.findByConvenioId(2L)).thenReturn(lista);

        List<PacienteEntity> resultado = pacienteService.buscarComFiltros(null, null, 2L);

        assertEquals(1, resultado.size());
        verify(pacienteRepository).findByConvenioId(2L);
        verify(pacienteRepository, never()).findAll();
    }

    // sem filtro nenhum, cai no findAll
    @Test
    void buscarComFiltros_deveListarTodos_quandoNenhumFiltroInformado() {
        List<PacienteEntity> todos = List.of(
                new PacienteEntity(1L, "Joao Silva", "123.456.789-00", LocalDate.of(1990, 5, 15), "(34)99999-0000", "joao@email.com", "Rua A", 1L)
        );
        when(pacienteRepository.findAll()).thenReturn(todos);

        List<PacienteEntity> resultado = pacienteService.buscarComFiltros(null, null, null);

        assertEquals(1, resultado.size());
        verify(pacienteRepository).findAll();
    }

    @Test
    void buscarComFiltros_deveRetornarVazio_quandoCpfNaoEncontrado() {
        when(pacienteRepository.findByCpf("000.000.000-00")).thenReturn(Optional.empty());

        List<PacienteEntity> resultado = pacienteService.buscarComFiltros(null, "000.000.000-00", null);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(pacienteRepository).findByCpf("000.000.000-00");
    }

    // busca parcial — "jo" deve trazer "Joao"
    @Test
    void buscarPorNomeDeveRetornarPacientesComNomeParcial_ignorandoCase() {
        List<PacienteEntity> resultado = List.of(
                new PacienteEntity(1L, "Joao Silva", "123.456.789-00", LocalDate.of(1990, 5, 15), "(34)99999-0000", "joao@email.com", "Rua A, 100", 1L)
        );
        when(pacienteRepository.findByNomeContainingIgnoreCase("joao")).thenReturn(resultado);

        List<PacienteEntity> encontrados = pacienteService.findByNome("joao");

        assertEquals(1, encontrados.size());
        assertEquals("Joao Silva", encontrados.get(0).getNome());
        verify(pacienteRepository).findByNomeContainingIgnoreCase("joao");
    }

    @Test
    void buscarPorCpfDeveRetornarPaciente_quandoExistir() {
        PacienteEntity paciente = new PacienteEntity(1L, "Joao Silva", "123.456.789-00", LocalDate.of(1990, 5, 15), "(34)99999-0000", "joao@email.com", "Rua A, 100", 1L);
        when(pacienteRepository.findByCpf("123.456.789-00")).thenReturn(Optional.of(paciente));

        Optional<PacienteEntity> encontrado = pacienteService.findByCpf("123.456.789-00");

        assertTrue(encontrado.isPresent());
        assertEquals("123.456.789-00", encontrado.get().getCpf());
        verify(pacienteRepository).findByCpf("123.456.789-00");
    }

    @Test
    void buscarPorCpfDeveRetornarVazio_quandoNaoExistir() {
        when(pacienteRepository.findByCpf("000.000.000-00")).thenReturn(Optional.empty());

        Optional<PacienteEntity> encontrado = pacienteService.findByCpf("000.000.000-00");

        assertTrue(encontrado.isEmpty());
        verify(pacienteRepository).findByCpf("000.000.000-00");
    }

    @Test
    void buscarPorConvenioDeveRetornarPacientes_quandoExistir() {
        List<PacienteEntity> pacientes = List.of(
                new PacienteEntity(1L, "Joao Silva", "123.456.789-00", LocalDate.of(1990, 5, 15), "(34)99999-0000", "joao@email.com", "Rua A, 100", 1L)
        );
        when(pacienteRepository.findByConvenioId(1L)).thenReturn(pacientes);

        List<PacienteEntity> resultado = pacienteService.findByConvenioId(1L);

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getConvenioId());
        verify(pacienteRepository).findByConvenioId(1L);
    }

    // convenio sem pacientes ainda — retorna vazio, nao null
    @Test
    void buscarPorConvenioDeveRetornarListaVazia_quandoNaoHaPacientesNoConvenio() {
        when(pacienteRepository.findByConvenioId(99L)).thenReturn(List.of());

        List<PacienteEntity> resultado = pacienteService.findByConvenioId(99L);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(pacienteRepository).findByConvenioId(99L);
    }
}
