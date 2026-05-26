package br.edu.imepac.administrativo.atendente;

import br.edu.imepac.commons.exceptions.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AtendenteServiceTest {

    @Mock
    private AtendenteRepository atendenteRepository;

    @InjectMocks
    private AtendenteService atendenteService;

    @Test
    void findAllDeveRetornarListaDeAtendentes() {
        List<AtendenteEntity> atendentes = List.of(
                new AtendenteEntity(1L, "Ana Souza", "ana", "senha123", true),
                new AtendenteEntity(2L, "Bruno Lima", "bruno", "senha123", true)
        );
        when(atendenteRepository.findAll()).thenReturn(atendentes);

        List<AtendenteEntity> resultado = atendenteService.findAll();

        assertEquals(2, resultado.size());
        assertEquals("Ana Souza", resultado.get(0).getNome());
        verify(atendenteRepository).findAll();
    }

    @Test
    void findByIdDeveRetornarAtendenteQuandoExistir() {
        AtendenteEntity atendente = new AtendenteEntity(1L, "Ana Souza", "ana", "senha123", true);
        when(atendenteRepository.findById(1L)).thenReturn(Optional.of(atendente));

        Optional<AtendenteEntity> resultado = atendenteService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals("ana", resultado.get().getUsuario());
        verify(atendenteRepository).findById(1L);
    }

    @Test
    void findByIdDeveRetornarVazioQuandoNaoExistir() {
        when(atendenteRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<AtendenteEntity> resultado = atendenteService.findById(99L);

        assertTrue(resultado.isEmpty());
        verify(atendenteRepository).findById(99L);
    }

    @Test
    void saveDevePersistirAtendente() {
        AtendenteEntity novo = new AtendenteEntity(null, "Ana Souza", "ana", "senha123", null);
        AtendenteEntity salvo = new AtendenteEntity(1L, "Ana Souza", "ana", "senha123", true);
        when(atendenteRepository.findByUsuario("ana")).thenReturn(Optional.empty());
        when(atendenteRepository.save(any(AtendenteEntity.class))).thenReturn(salvo);

        AtendenteEntity resultado = atendenteService.save(novo);

        assertNotNull(resultado.getId());
        assertTrue(novo.getAtivo());
        assertEquals("ana", resultado.getUsuario());
        verify(atendenteRepository).findByUsuario("ana");
        verify(atendenteRepository).save(novo);
    }

    @Test
    void saveDeveLancarBusinessException_quandoUsuarioJaExistir() {
        AtendenteEntity novo = new AtendenteEntity(null, "Ana Souza", "ana", "senha123", true);
        AtendenteEntity existente = new AtendenteEntity(1L, "Outra Pessoa", "ana", "senha456", true);
        when(atendenteRepository.findByUsuario("ana")).thenReturn(Optional.of(existente));

        assertThrows(BusinessException.class, () -> atendenteService.save(novo));
        verify(atendenteRepository, never()).save(any());
    }

    @Test
    void updateDeveAtualizarAtendenteQuandoExistir() {
        AtendenteEntity existente = new AtendenteEntity(1L, "Ana Souza", "ana", "senha123", true);
        AtendenteEntity dados = new AtendenteEntity(null, "Ana Atualizada", "ana", "novaSenha", true);
        when(atendenteRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(atendenteRepository.findByUsuario("ana")).thenReturn(Optional.of(existente));
        when(atendenteRepository.save(any(AtendenteEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<AtendenteEntity> resultado = atendenteService.update(1L, dados);

        assertTrue(resultado.isPresent());
        assertEquals("Ana Atualizada", resultado.get().getNome());
        assertEquals("novaSenha", resultado.get().getSenha());
        verify(atendenteRepository).save(existente);
    }

    @Test
    void updateDeveRetornarVazioQuandoNaoExistir() {
        AtendenteEntity dados = new AtendenteEntity(null, "Ana Atualizada", "ana", "novaSenha", true);
        when(atendenteRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<AtendenteEntity> resultado = atendenteService.update(99L, dados);

        assertTrue(resultado.isEmpty());
        verify(atendenteRepository, never()).save(any());
    }

    @Test
    void updateNaoDeveSobrescreverSenhaQuandoVierNula() {
        AtendenteEntity existente = new AtendenteEntity(1L, "Ana Souza", "ana", "senhaOriginal", true);
        AtendenteEntity dados = new AtendenteEntity(null, "Ana Atualizada", "ana", null, true);
        when(atendenteRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(atendenteRepository.findByUsuario("ana")).thenReturn(Optional.of(existente));
        when(atendenteRepository.save(any(AtendenteEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<AtendenteEntity> resultado = atendenteService.update(1L, dados);

        assertTrue(resultado.isPresent());
        assertEquals("senhaOriginal", resultado.get().getSenha());
    }

    @Test
    void updateNaoDeveSobrescreverSenhaQuandoVierEmBranco() {
        AtendenteEntity existente = new AtendenteEntity(1L, "Ana Souza", "ana", "senhaOriginal", true);
        AtendenteEntity dados = new AtendenteEntity(null, "Ana Atualizada", "ana", "   ", true);
        when(atendenteRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(atendenteRepository.findByUsuario("ana")).thenReturn(Optional.of(existente));
        when(atendenteRepository.save(any(AtendenteEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<AtendenteEntity> resultado = atendenteService.update(1L, dados);

        assertTrue(resultado.isPresent());
        assertEquals("senhaOriginal", resultado.get().getSenha());
    }

    @Test
    void deleteByIdDeveExcluirQuandoExistir() {
        when(atendenteRepository.existsById(1L)).thenReturn(true);

        boolean removido = atendenteService.deleteById(1L);

        assertTrue(removido);
        verify(atendenteRepository).deleteById(1L);
    }

    @Test
    void deleteByIdNaoDeveExcluirQuandoNaoExistir() {
        when(atendenteRepository.existsById(99L)).thenReturn(false);

        boolean removido = atendenteService.deleteById(99L);

        assertFalse(removido);
        verify(atendenteRepository, never()).deleteById(anyLong());
    }
}
