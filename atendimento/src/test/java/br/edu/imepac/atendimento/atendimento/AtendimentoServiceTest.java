package br.edu.imepac.atendimento.atendimento;

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
}
