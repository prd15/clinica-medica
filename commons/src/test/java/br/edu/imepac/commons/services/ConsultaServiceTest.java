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
}
