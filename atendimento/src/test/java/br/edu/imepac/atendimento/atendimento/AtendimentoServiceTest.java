package br.edu.imepac.atendimento.atendimento;

import br.edu.imepac.atendimento.anotacao.AnotacaoEntity;
import br.edu.imepac.atendimento.anotacao.AnotacaoRepository;
import br.edu.imepac.atendimento.exame.SolicitacaoExameEntity;
import br.edu.imepac.atendimento.exame.SolicitacaoExameRepository;
import br.edu.imepac.atendimento.outbox.OutboxEvent;
import br.edu.imepac.atendimento.outbox.OutboxEventRepository;
import br.edu.imepac.atendimento.outbox.OutboxStatus;
import br.edu.imepac.atendimento.prontuario.ProntuarioEntity;
import br.edu.imepac.atendimento.prontuario.ProntuarioRepository;
import br.edu.imepac.commons.exceptions.BusinessException;
import br.edu.imepac.commons.exceptions.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
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

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @InjectMocks
    private AtendimentoService atendimentoService;

    @Test
    void deveRegistrarAtendimentoComProntuarioEEnfileirarEventoOutbox() {
        AtendimentoEntity entrada = new AtendimentoEntity();
        entrada.setConsultaId(1L);
        entrada.setMedicoId(2L);
        entrada.setPacienteId(3L);

        AtendimentoEntity salvo = new AtendimentoEntity();
        salvo.setId(10L);
        salvo.setConsultaId(1L);
        salvo.setStatus(StatusAtendimento.REALIZADO);

        when(atendimentoRepository.save(any())).thenReturn(salvo);
        when(prontuarioRepository.save(any())).thenReturn(new ProntuarioEntity());

        AtendimentoEntity resultado = atendimentoService.registrar(entrada, "descricao", "diagnostico", "obs");

        assertEquals(StatusAtendimento.REALIZADO, resultado.getStatus());
        assertNotNull(resultado.getId());
        verify(atendimentoRepository, times(1)).save(any());
        verify(prontuarioRepository, times(1)).save(any());

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository, times(1)).save(captor.capture());
        OutboxEvent evento = captor.getValue();
        assertEquals(OutboxStatus.PENDENTE, evento.getStatus());
        assertEquals("CONFIRMACAO_REALIZACAO", evento.getEventType());
        assertEquals("1", evento.getAggregateId());
        assertEquals(0, evento.getTentativas());
    }

    @Test
    void deveLancarBusinessException_aoRegistrarAtendimentoDuplicadoParaMesmaConsulta() {
        AtendimentoEntity entrada = new AtendimentoEntity();
        entrada.setConsultaId(1L);
        entrada.setMedicoId(2L);
        entrada.setPacienteId(3L);

        when(atendimentoRepository.existsByConsultaId(1L)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> atendimentoService.registrar(entrada, "descricao", "diagnostico", "obs"));

        assertTrue(ex.getMessage().contains("1"));
        verify(atendimentoRepository, never()).save(any());
        verify(prontuarioRepository, never()).save(any());
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void deveLancarEntityNotFoundException_quandoConsultaNaoEncontrada() {
        when(atendimentoRepository.findByConsultaId(99L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
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

        when(prontuarioRepository.findByAtendimentoId(3L))
                .thenReturn(Optional.of(prontuario));
        when(anotacaoRepository.save(any(AnotacaoEntity.class))).thenReturn(salva);

        AnotacaoEntity resultado = atendimentoService.adicionarAnotacao(3L, "anotacao");

        assertNotNull(resultado.getId());
        assertEquals("anotacao", resultado.getTexto());
        verify(anotacaoRepository).save(any(AnotacaoEntity.class));
    }

    @Test
    void deveLancarEntityNotFoundException_quandoProntuarioNaoExiste() {
        when(prontuarioRepository.findByAtendimentoId(5L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> atendimentoService.adicionarAnotacao(5L, "algum texto"));

        assertTrue(ex.getMessage().contains("5"));
        verify(prontuarioRepository).findByAtendimentoId(5L);
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
    void deveLancarEntityNotFoundException_aoSolicitarExameQuandoAtendimentoNaoExiste() {
        when(atendimentoRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
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

        when(prontuarioRepository.findByAtendimentoId(3L))
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
    void deveLancarEntityNotFoundException_aoListarExamesQuandoAtendimentoNaoExiste() {
        when(atendimentoRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> atendimentoService.listarExames(99L));

        assertTrue(ex.getMessage().contains("99"));
        verify(exameRepository, never()).findByAtendimentoId(any());
    }
}
