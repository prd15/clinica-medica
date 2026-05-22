package br.edu.imepac.commons.services;

import br.edu.imepac.commons.entities.AnotacaoEntity;
import br.edu.imepac.commons.entities.AtendimentoEntity;
import br.edu.imepac.commons.entities.ProntuarioEntity;
import br.edu.imepac.commons.entities.SolicitacaoExameEntity;
import br.edu.imepac.commons.entities.StatusAtendimento;
import br.edu.imepac.commons.repositories.AnotacaoRepository;
import br.edu.imepac.commons.repositories.AtendimentoRepository;
import br.edu.imepac.commons.repositories.ProntuarioRepository;
import br.edu.imepac.commons.repositories.SolicitacaoExameRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AtendimentoServiceTest {

    @Mock
    private AtendimentoRepository atendimentoRepository;

    @Mock
    private ProntuarioRepository prontuarioRepository;

    @Mock
    private AnotacaoRepository anotacaoRepository;

    @Mock
    private SolicitacaoExameRepository exameRepository;

    @InjectMocks
    private AtendimentoService atendimentoService;

    @Test
    void deveRegistrarAtendimentoComProntuario() {
        AtendimentoEntity entrada = new AtendimentoEntity();
        entrada.setConsultaId(1L);
        entrada.setMedicoId(2L);
        entrada.setPacienteId(3L);

        AtendimentoEntity salvo = new AtendimentoEntity();
        salvo.setId(10L);
        salvo.setStatus(StatusAtendimento.REALIZADO);

        when(atendimentoRepository.save(any())).thenReturn(salvo);
        when(prontuarioRepository.save(any())).thenReturn(new ProntuarioEntity());

        AtendimentoEntity resultado = atendimentoService.registrar(entrada, "descricao", "diagnostico", "obs");

        assertEquals(StatusAtendimento.REALIZADO, resultado.getStatus());
        assertNotNull(resultado.getId());
        verify(atendimentoRepository, times(1)).save(any());
        verify(prontuarioRepository, times(1)).save(any());
    }

    @Test
    void deveLancarExcecaoAoRegistrarAtendimentoDuplicadoParaMesmaConsulta() {
        AtendimentoEntity entrada = new AtendimentoEntity();
        entrada.setConsultaId(1L);
        entrada.setMedicoId(2L);
        entrada.setPacienteId(3L);

        when(atendimentoRepository.existsByConsultaId(1L)).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> atendimentoService.registrar(entrada, "descricao", "diagnostico", "obs"));

        assertTrue(ex.getMessage().contains("1"));
        verify(atendimentoRepository, never()).save(any());
        verify(prontuarioRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoConsultaNaoEncontrada() {
        when(atendimentoRepository.findByConsultaId(99L)).thenReturn(Optional.empty());

        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> atendimentoService.buscarPorConsulta(99L));

        assertTrue(ex.getMessage().contains("99"));
        verify(atendimentoRepository).findByConsultaId(99L);
    }

    @Test
    void deveRetornarAtendimentoQuandoConsultaExiste() {
        AtendimentoEntity atendimento = new AtendimentoEntity();
        atendimento.setId(1L);
        atendimento.setConsultaId(5L);

        when(atendimentoRepository.findByConsultaId(5L)).thenReturn(Optional.of(atendimento));

        AtendimentoEntity resultado = atendimentoService.buscarPorConsulta(5L);

        assertEquals(1L, resultado.getId());
        assertEquals(5L, resultado.getConsultaId());
        verify(atendimentoRepository).findByConsultaId(5L);
    }

    @Test
    void deveAdicionarAnotacaoQuandoProntuarioExiste() {
        ProntuarioEntity prontuario = new ProntuarioEntity();
        prontuario.setId(7L);
        AnotacaoEntity salva = new AnotacaoEntity();
        salva.setId(20L);
        salva.setTexto("anotacao");

        when(prontuarioRepository.findFirstByAtendimentoIdOrderByIdDesc(3L))
                .thenReturn(Optional.of(prontuario));
        when(anotacaoRepository.save(any(AnotacaoEntity.class))).thenReturn(salva);

        AnotacaoEntity resultado = atendimentoService.adicionarAnotacao(3L, "anotacao");

        assertNotNull(resultado.getId());
        assertEquals("anotacao", resultado.getTexto());
        verify(anotacaoRepository).save(any(AnotacaoEntity.class));
    }

    @Test
    void deveLancarExcecaoQuandoProntuarioNaoExiste() {
        when(prontuarioRepository.findFirstByAtendimentoIdOrderByIdDesc(5L)).thenReturn(Optional.empty());

        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> atendimentoService.adicionarAnotacao(5L, "algum texto"));

        assertTrue(ex.getMessage().contains("5"));
        verify(prontuarioRepository).findFirstByAtendimentoIdOrderByIdDesc(5L);
        verify(anotacaoRepository, never()).save(any());
    }

    @Test
    void deveSolicitarExameQuandoAtendimentoExiste() {
        AtendimentoEntity atendimento = new AtendimentoEntity();
        atendimento.setId(8L);
        SolicitacaoExameEntity salvo = new SolicitacaoExameEntity();
        salvo.setId(30L);
        salvo.setTipo("LABORATORIAL");

        when(atendimentoRepository.findById(8L)).thenReturn(Optional.of(atendimento));
        when(exameRepository.save(any(SolicitacaoExameEntity.class))).thenReturn(salvo);

        SolicitacaoExameEntity resultado = atendimentoService.solicitarExame(8L, "Hemograma", "LABORATORIAL");

        assertNotNull(resultado.getId());
        assertEquals("LABORATORIAL", resultado.getTipo());
        verify(exameRepository).save(any(SolicitacaoExameEntity.class));
    }

    @Test
    void deveLancarExcecaoAoSolicitarExameQuandoAtendimentoNaoExiste() {
        when(atendimentoRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> atendimentoService.solicitarExame(99L, "Hemograma", "LABORATORIAL"));

        assertTrue(ex.getMessage().contains("99"));
        verify(atendimentoRepository).findById(99L);
        verify(exameRepository, never()).save(any());
    }

    @Test
    void deveRetornarHistoricoQuandoPacientePossuiAtendimentos() {
        AtendimentoEntity a1 = new AtendimentoEntity();
        a1.setId(1L);
        a1.setPacienteId(10L);
        AtendimentoEntity a2 = new AtendimentoEntity();
        a2.setId(2L);
        a2.setPacienteId(10L);

        when(atendimentoRepository.findByPacienteId(10L)).thenReturn(List.of(a1, a2));

        List<AtendimentoEntity> resultado = atendimentoService.buscarHistoricoPorPaciente(10L);

        assertEquals(2, resultado.size());
        verify(atendimentoRepository).findByPacienteId(10L);
    }

    @Test
    void deveRetornarHistoricoVazioQuandoPacienteSemAtendimentos() {
        when(atendimentoRepository.findByPacienteId(50L)).thenReturn(List.of());

        List<AtendimentoEntity> resultado = atendimentoService.buscarHistoricoPorPaciente(50L);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(atendimentoRepository).findByPacienteId(50L);
    }

    @Test
    void deveListarAnotacoesQuandoProntuarioExiste() {
        ProntuarioEntity prontuario = new ProntuarioEntity();
        prontuario.setId(7L);
        AnotacaoEntity a1 = new AnotacaoEntity();
        a1.setId(1L);
        AnotacaoEntity a2 = new AnotacaoEntity();
        a2.setId(2L);

        when(prontuarioRepository.findFirstByAtendimentoIdOrderByIdDesc(3L))
                .thenReturn(Optional.of(prontuario));
        when(anotacaoRepository.findByProntuarioId(7L)).thenReturn(List.of(a1, a2));

        List<AnotacaoEntity> resultado = atendimentoService.listarAnotacoes(3L);

        assertEquals(2, resultado.size());
        verify(anotacaoRepository).findByProntuarioId(7L);
    }

    @Test
    void deveListarExamesQuandoAtendimentoExiste() {
        AtendimentoEntity atendimento = new AtendimentoEntity();
        atendimento.setId(8L);
        SolicitacaoExameEntity e1 = new SolicitacaoExameEntity();
        e1.setId(1L);

        when(atendimentoRepository.findById(8L)).thenReturn(Optional.of(atendimento));
        when(exameRepository.findByAtendimentoId(8L)).thenReturn(List.of(e1));

        List<SolicitacaoExameEntity> resultado = atendimentoService.listarExames(8L);

        assertEquals(1, resultado.size());
        verify(exameRepository).findByAtendimentoId(8L);
    }

    @Test
    void deveLancarExcecaoAoListarExamesQuandoAtendimentoNaoExiste() {
        when(atendimentoRepository.findById(99L)).thenReturn(Optional.empty());

        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> atendimentoService.listarExames(99L));

        assertTrue(ex.getMessage().contains("99"));
        verify(exameRepository, never()).findByAtendimentoId(any());
    }
}
