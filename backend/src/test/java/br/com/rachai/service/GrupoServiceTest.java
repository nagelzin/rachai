package br.com.rachai.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import br.com.rachai.dto.CriarGrupoRequest;
import br.com.rachai.exception.NaoEncontradoException;
import br.com.rachai.model.*;
import br.com.rachai.repository.*;
import br.com.rachai.security.UsuarioAtual;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class GrupoServiceTest {
    @Mock GrupoRepository grupos;
    @Mock GrupoParticipanteRepository membros;
    @Mock UsuarioAtual atual;
    @InjectMocks GrupoService service;
    Usuario usuario;

    @BeforeEach
    void setup() {
        usuario = new Usuario("Ana", "ana@example.test", "hash");
        ReflectionTestUtils.setField(usuario, "id", 1L);
        when(atual.obter()).thenReturn(usuario);
    }

    @Test
    void criadorDaSessaoTambemEMembro() {
        when(grupos.save(any())).thenAnswer(i -> i.getArgument(0));
        var response = service.criar(new CriarGrupoRequest(" Viagem ", " Praia "));
        assertEquals("Viagem", response.nome());
        assertEquals("Praia", response.descricao());
        assertEquals(1L, response.usuarioResponsavelId());
        var captor = ArgumentCaptor.forClass(GrupoParticipante.class);
        verify(membros).saveAndFlush(captor.capture());
        assertSame(usuario, captor.getValue().getUsuario());
    }

    @Test
    void naoMembroNaoConsultaGrupo() {
        assertThrows(NaoEncontradoException.class, () -> service.buscar(8L));
        verifyNoInteractions(grupos);
    }

    @Test
    void membroNaoAdministradorNaoGerencia() {
        var outro = new Usuario("Outro");
        ReflectionTestUtils.setField(outro, "id", 2L);
        when(membros.existsByGrupoIdAndUsuarioId(8L, 1L)).thenReturn(true);
        when(grupos.findById(8L)).thenReturn(Optional.of(new Grupo("Viagem", null, outro)));
        assertThrows(AccessDeniedException.class, () -> service.administrador(8L));
    }
}
