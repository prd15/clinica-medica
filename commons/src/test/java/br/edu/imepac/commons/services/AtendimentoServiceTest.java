package br.edu.imepac.commons.services;

import br.edu.imepac.commons.entities.AtendimentoEntity;
import br.edu.imepac.commons.entities.ProntuarioEntity;
import br.edu.imepac.commons.repositories.AnotacaoRepository;
import br.edu.imepac.commons.repositories.AtendimentoRepository;
import br.edu.imepac.commons.repositories.ProntuarioRepository;
import br.edu.imepac.commons.repositories.SolicitacaoExameRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        salvo.setStatus("REALIZADO");

        when(atendimentoRepository.save(any())).thenReturn(salvo);
        when(prontuarioRepository.save(any())).thenReturn(new ProntuarioEntity());

        AtendimentoEntity resultado = atendimentoService.registrar(entrada, "descricao", "diagnostico", "obs");

        assertEquals("REALIZADO", resultado.getStatus());
        assertNotNull(resultado.getId());
        verify(atendimentoRepository, times(1)).save(any());
        verify(prontuarioRepository, times(1)).save(any());
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
    void deveLancarExcecaoQuandoProntuarioNaoExiste() {
        when(prontuarioRepository.findFirstByAtendimentoIdOrderByIdDesc(5L)).thenReturn(Optional.empty());

        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> atendimentoService.adicionarAnotacao(5L, "algum texto"));

        assertTrue(ex.getMessage().contains("5"));
        verify(prontuarioRepository).findFirstByAtendimentoIdOrderByIdDesc(5L);
        verify(anotacaoRepository, never()).save(any());
    }
}
