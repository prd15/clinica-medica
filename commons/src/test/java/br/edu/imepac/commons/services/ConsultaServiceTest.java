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
}
