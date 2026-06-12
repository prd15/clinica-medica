package br.edu.imepac.atendimento.anotacao;

import br.edu.imepac.atendimento.prontuario.ProntuarioEntity;
import br.edu.imepac.atendimento.prontuario.ProntuarioRepository;
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
class AnotacaoServiceTest {

    @Mock
    private ProntuarioRepository prontuarioRepository;

    @Mock
    private AnotacaoRepository anotacaoRepository;

    @InjectMocks
    private AnotacaoService anotacaoService;

    @Test
    void deveAdicionarAnotacaoQuandoProntuarioExiste() {
        ProntuarioEntity prontuario = new ProntuarioEntity();
        prontuario.setId(7L);
        AnotacaoEntity salva = new AnotacaoEntity();
        salva.setId(20L);
        salva.setTexto("anotacao");

        when(prontuarioRepository.findByAtendimentoId(3L)).thenReturn(Optional.of(prontuario));
        when(anotacaoRepository.save(any(AnotacaoEntity.class))).thenReturn(salva);

        AnotacaoEntity resultado = anotacaoService.adicionarAnotacao(3L, "anotacao");

        assertNotNull(resultado.getId());
        assertEquals("anotacao", resultado.getTexto());
        verify(anotacaoRepository).save(any(AnotacaoEntity.class));
    }

    @Test
    void deveLancarEntityNotFoundException_quandoProntuarioNaoExiste() {
        when(prontuarioRepository.findByAtendimentoId(5L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> anotacaoService.adicionarAnotacao(5L, "algum texto"));

        assertTrue(ex.getMessage().contains("5"));
        verify(prontuarioRepository).findByAtendimentoId(5L);
        verify(anotacaoRepository, never()).save(any());
    }

    @Test
    void deveListarAnotacoesQuandoProntuarioExiste() {
        ProntuarioEntity prontuario = new ProntuarioEntity();
        prontuario.setId(7L);
        AnotacaoEntity a1 = new AnotacaoEntity();
        a1.setId(1L);
        AnotacaoEntity a2 = new AnotacaoEntity();
        a2.setId(2L);

        when(prontuarioRepository.findByAtendimentoId(3L)).thenReturn(Optional.of(prontuario));
        when(anotacaoRepository.findByProntuarioId(7L)).thenReturn(List.of(a1, a2));

        List<AnotacaoEntity> resultado = anotacaoService.listarAnotacoes(3L);

        assertEquals(2, resultado.size());
        verify(anotacaoRepository).findByProntuarioId(7L);
    }
}
