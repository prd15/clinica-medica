package br.edu.imepac.atendimento.exame;

import br.edu.imepac.atendimento.atendimento.AtendimentoEntity;
import br.edu.imepac.atendimento.atendimento.AtendimentoRepository;
import br.edu.imepac.commons.exceptions.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExameServiceTest {

    @Mock
    private AtendimentoRepository atendimentoRepository;

    @Mock
    private SolicitacaoExameRepository exameRepository;

    @InjectMocks
    private ExameService exameService;

    @Test
    void deveSolicitarExameQuandoAtendimentoExiste() {
        AtendimentoEntity atendimento = new AtendimentoEntity();
        atendimento.setId(8L);
        SolicitacaoExameEntity salvo = new SolicitacaoExameEntity();
        salvo.setId(30L);
        salvo.setTipo("LABORATORIAL");

        when(atendimentoRepository.findById(8L)).thenReturn(Optional.of(atendimento));
        when(exameRepository.save(any(SolicitacaoExameEntity.class))).thenReturn(salvo);

        SolicitacaoExameEntity resultado = exameService.solicitarExame(8L, "Hemograma", "LABORATORIAL");

        assertNotNull(resultado.getId());
        assertEquals("LABORATORIAL", resultado.getTipo());
        verify(exameRepository).save(any(SolicitacaoExameEntity.class));
    }

    @Test
    void deveLancarEntityNotFoundException_aoSolicitarExameQuandoAtendimentoNaoExiste() {
        when(atendimentoRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> exameService.solicitarExame(99L, "Hemograma", "LABORATORIAL"));

        assertTrue(ex.getMessage().contains("99"));
        verify(atendimentoRepository).findById(99L);
        verify(exameRepository, never()).save(any());
    }

    @Test
    void deveListarExamesQuandoAtendimentoExiste() {
        AtendimentoEntity atendimento = new AtendimentoEntity();
        atendimento.setId(8L);
        SolicitacaoExameEntity e1 = new SolicitacaoExameEntity();
        e1.setId(1L);

        when(atendimentoRepository.findById(8L)).thenReturn(Optional.of(atendimento));
        when(exameRepository.findByAtendimentoId(8L)).thenReturn(List.of(e1));

        List<SolicitacaoExameEntity> resultado = exameService.listarExames(8L);

        assertEquals(1, resultado.size());
        verify(exameRepository).findByAtendimentoId(8L);
    }

    @Test
    void deveLancarEntityNotFoundException_aoListarExamesQuandoAtendimentoNaoExiste() {
        when(atendimentoRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> exameService.listarExames(99L));

        assertTrue(ex.getMessage().contains("99"));
        verify(exameRepository, never()).findByAtendimentoId(any());
    }
}
