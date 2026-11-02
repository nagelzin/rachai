package br.com.rachai.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import br.com.rachai.exception.*;
import br.com.rachai.model.*;
import br.com.rachai.repository.GrupoParticipanteRepository;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

@ExtendWith(MockitoExtension.class)
class ParticipanteServiceTest {
    @Mock GrupoService grupos;
    @Mock br.com.rachai.repository.DespesaParticipanteRepository envolvidos;
    @Mock br.com.rachai.repository.DespesaRepository despesas;
    @Mock GrupoParticipanteRepository participantes;
    @InjectMocks ParticipanteService service;
    Grupo grupo;
    Usuario admin;

    @BeforeEach
    void setup() {
        admin = new Usuario("Admin");
        ReflectionTestUtils.setField(admin, "id", 1L);
        grupo = new Grupo("Viagem", null, admin);
    }

    @Test
    void naoRemoveParticipanteDeOutroGrupo() {
        when(grupos.administrador(1L)).thenReturn(grupo);
        when(participantes.findByIdAndGrupoId(8L, 1L)).thenReturn(Optional.empty());
        assertThrows(NaoEncontradoException.class, () -> service.remover(1L, 8L));
        verify(participantes, never()).delete(any());
    }

    @Test
    void removeMembroDoGrupo() {
        var u = new Usuario("Ana");
        ReflectionTestUtils.setField(u, "id", 2L);
        var p = new GrupoParticipante(grupo, u);
        when(grupos.administrador(1L)).thenReturn(grupo);
        when(participantes.findByIdAndGrupoId(8L, 1L)).thenReturn(Optional.of(p));
        service.remover(1L, 8L);
        verify(participantes).delete(p);
        verify(grupos).bloquear(1L);
    }

    @Test
    void administradorNaoPodeSerRemovido() {
        when(grupos.administrador(1L)).thenReturn(grupo);
        when(participantes.findByIdAndGrupoId(8L, 1L))
                .thenReturn(Optional.of(new GrupoParticipante(grupo, admin)));
        assertThrows(ConflitoException.class, () -> service.remover(1L, 8L));
        verify(participantes, never()).delete(any());
    }

    @Test
    void grupoInacessivelNaoRetornaListaVazia() {
        when(grupos.buscar(99L)).thenThrow(new NaoEncontradoException("Grupo não encontrado."));
        assertThrows(NaoEncontradoException.class, () -> service.listar(99L));
        verifyNoInteractions(participantes);
    }
}
