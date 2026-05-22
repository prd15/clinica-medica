package br.edu.imepac.commons.services.agendamento;

import br.edu.imepac.commons.entities.agendamento.ConsultaEntity;
import br.edu.imepac.commons.entities.agendamento.StatusConsulta;
import br.edu.imepac.commons.repositories.agendamento.ConsultaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultaServiceTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @InjectMocks
    private ConsultaService consultaService;

    private ConsultaEntity novaConsulta(Long id, StatusConsulta status) {
        LocalDateTime dataHora = LocalDateTime.now().plusDays(30).withSecond(0).withNano(0);
        return new ConsultaEntity(id, 1L, 1L, 1L, dataHora, status, "consulta de rotina");
    }

    @Test
    void testAgendar_SemConflito_RetornaConsultaSalva() {
        ConsultaEntity nova = novaConsulta(null, null);
        ConsultaEntity salva = novaConsulta(1L, StatusConsulta.PENDENTE);

        when(consultaRepository.findByMedicoIdAndDataHoraBetweenAndStatusNot(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), eq(StatusConsulta.CANCELADA)))
                .thenReturn(List.of());
        when(consultaRepository.save(any(ConsultaEntity.class))).thenReturn(salva);

        ConsultaEntity resultado = consultaService.agendar(nova);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(StatusConsulta.PENDENTE, resultado.getStatus());
        verify(consultaRepository).save(any(ConsultaEntity.class));
    }

    @Test
    void testAgendar_ComConflito_LancaException() {
        ConsultaEntity nova = novaConsulta(null, null);
        ConsultaEntity existente = novaConsulta(5L, StatusConsulta.PENDENTE);

        when(consultaRepository.findByMedicoIdAndDataHoraBetweenAndStatusNot(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), eq(StatusConsulta.CANCELADA)))
                .thenReturn(List.of(existente));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> consultaService.agendar(nova));
        assertTrue(ex.getMessage().toLowerCase().contains("horario"));

        verify(consultaRepository, never()).save(any(ConsultaEntity.class));
    }

    @Test
    void testAgendar_ConflitoDentroDoSlotDe30Minutos_LancaException() {
        ConsultaEntity nova = novaConsulta(null, null);
        ConsultaEntity proxima = novaConsulta(7L, StatusConsulta.PENDENTE);
        proxima.setDataHora(nova.getDataHora().plusMinutes(15));

        when(consultaRepository.findByMedicoIdAndDataHoraBetweenAndStatusNot(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), eq(StatusConsulta.CANCELADA)))
                .thenReturn(List.of(proxima));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> consultaService.agendar(nova));
        assertTrue(ex.getMessage().toLowerCase().contains("horario"));

        verify(consultaRepository, never()).save(any(ConsultaEntity.class));
    }

    @Test
    void testCancelar_Encontrado_RetornaCancelada() {
        ConsultaEntity existente = novaConsulta(1L, StatusConsulta.PENDENTE);
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(consultaRepository.save(any(ConsultaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<ConsultaEntity> resultado = consultaService.cancelar(1L);

        assertTrue(resultado.isPresent());
        assertEquals(StatusConsulta.CANCELADA, resultado.get().getStatus());
        verify(consultaRepository).findById(1L);
        verify(consultaRepository).save(existente);
    }

    @Test
    void testCancelar_NaoEncontrado_RetornaEmpty() {
        when(consultaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<ConsultaEntity> resultado = consultaService.cancelar(99L);

        assertTrue(resultado.isEmpty());
        verify(consultaRepository).findById(99L);
        verify(consultaRepository, never()).save(any(ConsultaEntity.class));
    }

    @Test
    void testReagendar_Encontrado_SemConflito() {
        ConsultaEntity existente = novaConsulta(1L, StatusConsulta.PENDENTE);
        LocalDateTime novaDataHora = LocalDateTime.now().plusDays(45).withSecond(0).withNano(0);

        when(consultaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(consultaRepository.findByMedicoIdAndDataHoraBetweenAndStatusNot(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), eq(StatusConsulta.CANCELADA)))
                .thenReturn(List.of());
        when(consultaRepository.save(any(ConsultaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<ConsultaEntity> resultado = consultaService.reagendar(1L, novaDataHora);

        assertTrue(resultado.isPresent());
        assertEquals(novaDataHora, resultado.get().getDataHora());
        verify(consultaRepository).save(existente);
    }

    @Test
    void testReagendar_IgnoraProprioRegistroNaCheckDeConflito() {
        ConsultaEntity existente = novaConsulta(1L, StatusConsulta.PENDENTE);
        LocalDateTime novaDataHora = existente.getDataHora().plusMinutes(10);

        when(consultaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(consultaRepository.findByMedicoIdAndDataHoraBetweenAndStatusNot(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), eq(StatusConsulta.CANCELADA)))
                .thenReturn(List.of(existente));
        when(consultaRepository.save(any(ConsultaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<ConsultaEntity> resultado = consultaService.reagendar(1L, novaDataHora);

        assertTrue(resultado.isPresent());
        assertEquals(novaDataHora, resultado.get().getDataHora());
        verify(consultaRepository).save(existente);
    }

    @Test
    void testConfirmar_Encontrado_RetornaConfirmada() {
        ConsultaEntity existente = novaConsulta(1L, StatusConsulta.PENDENTE);
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(consultaRepository.save(any(ConsultaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<ConsultaEntity> resultado = consultaService.confirmar(1L);

        assertTrue(resultado.isPresent());
        assertEquals(StatusConsulta.CONFIRMADA, resultado.get().getStatus());
        verify(consultaRepository).save(existente);
    }

    @Test
    void testFindByMedicoId_RetornaLista() {
        List<ConsultaEntity> consultas = List.of(
                novaConsulta(1L, StatusConsulta.PENDENTE),
                novaConsulta(2L, StatusConsulta.CONFIRMADA)
        );
        when(consultaRepository.findByMedicoId(1L)).thenReturn(consultas);

        List<ConsultaEntity> resultado = consultaService.findByMedicoId(1L);

        assertEquals(2, resultado.size());
        verify(consultaRepository).findByMedicoId(1L);
    }

    @Test
    void testFindByMedicoIdAndData_RetornaConsultasDoDia() {
        java.time.LocalDate dia = java.time.LocalDate.now().plusDays(30);
        List<ConsultaEntity> doDia = List.of(
                novaConsulta(1L, StatusConsulta.PENDENTE),
                novaConsulta(2L, StatusConsulta.CONFIRMADA)
        );
        when(consultaRepository.findByMedicoIdAndDataHoraBetween(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(doDia);

        List<ConsultaEntity> resultado = consultaService.findByMedicoIdAndData(1L, dia);

        assertEquals(2, resultado.size());
        verify(consultaRepository).findByMedicoIdAndDataHoraBetween(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testFindByPacienteId_RetornaLista() {
        List<ConsultaEntity> consultas = List.of(novaConsulta(1L, StatusConsulta.REALIZADA));
        when(consultaRepository.findByPacienteId(1L)).thenReturn(consultas);

        List<ConsultaEntity> resultado = consultaService.findByPacienteId(1L);

        assertEquals(1, resultado.size());
        verify(consultaRepository).findByPacienteId(1L);
    }

    @Test
    void testFindMinhaAgenda_RetornaPendentesEConfirmadas() {
        List<ConsultaEntity> agenda = List.of(
                novaConsulta(1L, StatusConsulta.PENDENTE),
                novaConsulta(2L, StatusConsulta.CONFIRMADA)
        );
        when(consultaRepository.findByMedicoIdAndStatusIn(
                1L, List.of(StatusConsulta.PENDENTE, StatusConsulta.CONFIRMADA)))
                .thenReturn(agenda);

        List<ConsultaEntity> resultado = consultaService.findMinhaAgenda(1L);

        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().noneMatch(
                c -> c.getStatus() == StatusConsulta.CANCELADA || c.getStatus() == StatusConsulta.REALIZADA));
        verify(consultaRepository).findByMedicoIdAndStatusIn(
                1L, List.of(StatusConsulta.PENDENTE, StatusConsulta.CONFIRMADA));
    }

    @Test
    void testAgendar_DataNoPassado_LancaException() {
        ConsultaEntity nova = novaConsulta(null, null);
        nova.setDataHora(LocalDateTime.now().minusDays(1));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> consultaService.agendar(nova));
        assertTrue(ex.getMessage().toLowerCase().contains("passado"));

        verify(consultaRepository, never())
                .findByMedicoIdAndDataHoraBetweenAndStatusNot(any(), any(), any(), any());
        verify(consultaRepository, never()).save(any(ConsultaEntity.class));
    }

    @Test
    void testConfirmar_ConsultaCancelada_LancaException() {
        ConsultaEntity cancelada = novaConsulta(1L, StatusConsulta.CANCELADA);
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(cancelada));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> consultaService.confirmar(1L));
        assertTrue(ex.getMessage().contains("CANCELADA"));

        verify(consultaRepository, never()).save(any(ConsultaEntity.class));
    }

    @Test
    void testCancelar_ConsultaRealizada_LancaException() {
        ConsultaEntity realizada = novaConsulta(1L, StatusConsulta.REALIZADA);
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(realizada));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> consultaService.cancelar(1L));
        assertTrue(ex.getMessage().toLowerCase().contains("realizada"));

        verify(consultaRepository, never()).save(any(ConsultaEntity.class));
    }

    @Test
    void testReagendar_ConsultaCancelada_LancaException() {
        ConsultaEntity cancelada = novaConsulta(1L, StatusConsulta.CANCELADA);
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(cancelada));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> consultaService.reagendar(1L, LocalDateTime.now().plusDays(5)));
        assertTrue(ex.getMessage().toLowerCase().contains("nao pode ser reagendada"));

        verify(consultaRepository, never())
                .findByMedicoIdAndDataHoraBetweenAndStatusNot(any(), any(), any(), any());
        verify(consultaRepository, never()).save(any(ConsultaEntity.class));
    }

    @Test
    void testRealizar_ConsultaConfirmada_RetornaRealizada() {
        ConsultaEntity confirmada = novaConsulta(1L, StatusConsulta.CONFIRMADA);
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(confirmada));
        when(consultaRepository.save(any(ConsultaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<ConsultaEntity> resultado = consultaService.realizar(1L);

        assertTrue(resultado.isPresent());
        assertEquals(StatusConsulta.REALIZADA, resultado.get().getStatus());
        verify(consultaRepository).save(confirmada);
    }

    @Test
    void testRealizar_ConsultaPendente_RetornaRealizada() {
        ConsultaEntity pendente = novaConsulta(1L, StatusConsulta.PENDENTE);
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(pendente));
        when(consultaRepository.save(any(ConsultaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<ConsultaEntity> resultado = consultaService.realizar(1L);

        assertTrue(resultado.isPresent());
        assertEquals(StatusConsulta.REALIZADA, resultado.get().getStatus());
        verify(consultaRepository).save(pendente);
    }

    @Test
    void testRealizar_ConsultaCancelada_LancaException() {
        ConsultaEntity cancelada = novaConsulta(1L, StatusConsulta.CANCELADA);
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(cancelada));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> consultaService.realizar(1L));
        assertTrue(ex.getMessage().contains("CANCELADA"));

        verify(consultaRepository, never()).save(any(ConsultaEntity.class));
    }

    @Test
    void testRealizar_ConsultaJaRealizada_LancaException() {
        ConsultaEntity realizada = novaConsulta(1L, StatusConsulta.REALIZADA);
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(realizada));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> consultaService.realizar(1L));
        assertTrue(ex.getMessage().contains("REALIZADA"));

        verify(consultaRepository, never()).save(any(ConsultaEntity.class));
    }

    @Test
    void testRealizar_ConsultaInexistente_RetornaVazio() {
        when(consultaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<ConsultaEntity> resultado = consultaService.realizar(99L);

        assertTrue(resultado.isEmpty());
        verify(consultaRepository, never()).save(any(ConsultaEntity.class));
    }
}
