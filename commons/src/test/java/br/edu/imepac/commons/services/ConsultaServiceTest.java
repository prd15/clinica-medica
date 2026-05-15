package br.edu.imepac.commons.services;

import br.edu.imepac.commons.entities.ConsultaEntity;
import br.edu.imepac.commons.entities.StatusConsulta;
import br.edu.imepac.commons.repositories.ConsultaRepository;
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

    // helper pra montar entidade rapidinho — evita repeticao em todo teste
    private ConsultaEntity novaConsulta(Long id, StatusConsulta status) {
        LocalDateTime dataHora = LocalDateTime.of(2026, 8, 1, 10, 0);
        return new ConsultaEntity(id, 1L, 1L, 1L, dataHora, status, "consulta de rotina");
    }

    // caminho feliz do agendamento: sem conflito, salva com status PENDENTE
    @Test
    void testAgendar_SemConflito_RetornaConsultaSalva() {
        ConsultaEntity nova = novaConsulta(null, null);
        ConsultaEntity salva = novaConsulta(1L, StatusConsulta.PENDENTE);

        when(consultaRepository.existsByMedicoIdAndDataHoraAndStatusNot(
                eq(1L), any(LocalDateTime.class), eq(StatusConsulta.CANCELADA)))
                .thenReturn(false);
        when(consultaRepository.save(any(ConsultaEntity.class))).thenReturn(salva);

        ConsultaEntity resultado = consultaService.agendar(nova);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(StatusConsulta.PENDENTE, resultado.getStatus());
        verify(consultaRepository).save(any(ConsultaEntity.class));
    }

    // conflito de horario tem que abortar antes de chamar o save
    @Test
    void testAgendar_ComConflito_LancaException() {
        ConsultaEntity nova = novaConsulta(null, null);

        when(consultaRepository.existsByMedicoIdAndDataHoraAndStatusNot(
                eq(1L), any(LocalDateTime.class), eq(StatusConsulta.CANCELADA)))
                .thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> consultaService.agendar(nova));
        assertTrue(ex.getMessage().toLowerCase().contains("horario"));

        verify(consultaRepository, never()).save(any(ConsultaEntity.class));
    }

    // cancelar deve mudar status para CANCELADA quando a consulta existe
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

    // cancelar consulta inexistente devolve Optional.empty sem chamar save
    @Test
    void testCancelar_NaoEncontrado_RetornaEmpty() {
        when(consultaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<ConsultaEntity> resultado = consultaService.cancelar(99L);

        assertTrue(resultado.isEmpty());
        verify(consultaRepository).findById(99L);
        verify(consultaRepository, never()).save(any(ConsultaEntity.class));
    }

    // reagendar atualiza dataHora se a consulta existe e nao ha conflito no novo horario
    @Test
    void testReagendar_Encontrado_SemConflito() {
        ConsultaEntity existente = novaConsulta(1L, StatusConsulta.PENDENTE);
        LocalDateTime novaDataHora = LocalDateTime.of(2026, 8, 10, 14, 0);

        when(consultaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(consultaRepository.existsByMedicoIdAndDataHoraAndStatusNot(
                eq(1L), eq(novaDataHora), eq(StatusConsulta.CANCELADA)))
                .thenReturn(false);
        when(consultaRepository.save(any(ConsultaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<ConsultaEntity> resultado = consultaService.reagendar(1L, novaDataHora);

        assertTrue(resultado.isPresent());
        assertEquals(novaDataHora, resultado.get().getDataHora());
        verify(consultaRepository).save(existente);
    }

    // confirmar muda status de PENDENTE para CONFIRMADA
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

    // findByMedicoId delega direto pro repository — confirma que nao filtra nada
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

    // historico do paciente
    @Test
    void testFindByPacienteId_RetornaLista() {
        List<ConsultaEntity> consultas = List.of(novaConsulta(1L, StatusConsulta.REALIZADA));
        when(consultaRepository.findByPacienteId(1L)).thenReturn(consultas);

        List<ConsultaEntity> resultado = consultaService.findByPacienteId(1L);

        assertEquals(1, resultado.size());
        verify(consultaRepository).findByPacienteId(1L);
    }

    // minha-agenda traz somente PENDENTE — confirma que o service passa o status certo
    @Test
    void testFindMinhaAgenda_RetornaApenasPendentes() {
        List<ConsultaEntity> pendentes = List.of(
                novaConsulta(1L, StatusConsulta.PENDENTE),
                novaConsulta(2L, StatusConsulta.PENDENTE)
        );
        when(consultaRepository.findByMedicoIdAndStatus(1L, StatusConsulta.PENDENTE))
                .thenReturn(pendentes);

        List<ConsultaEntity> resultado = consultaService.findMinhaAgenda(1L);

        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(c -> c.getStatus() == StatusConsulta.PENDENTE));
        verify(consultaRepository).findByMedicoIdAndStatus(1L, StatusConsulta.PENDENTE);
    }

    // agendamento retroativo nao faz sentido — service tem que abortar antes de tocar no repository
    @Test
    void testAgendar_DataNoPassado_LancaException() {
        ConsultaEntity nova = novaConsulta(null, null);
        nova.setDataHora(LocalDateTime.now().minusDays(1));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> consultaService.agendar(nova));
        assertTrue(ex.getMessage().toLowerCase().contains("passado"));

        verify(consultaRepository, never()).existsByMedicoIdAndDataHoraAndStatusNot(any(), any(), any());
        verify(consultaRepository, never()).save(any(ConsultaEntity.class));
    }

    // tentativa de confirmar uma cancelada (ou qualquer status diferente de PENDENTE) deve falhar
    @Test
    void testConfirmar_ConsultaCancelada_LancaException() {
        ConsultaEntity cancelada = novaConsulta(1L, StatusConsulta.CANCELADA);
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(cancelada));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> consultaService.confirmar(1L));
        assertTrue(ex.getMessage().contains("CANCELADA"));

        verify(consultaRepository, never()).save(any(ConsultaEntity.class));
    }
}
