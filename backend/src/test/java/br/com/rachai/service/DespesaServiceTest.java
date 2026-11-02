package br.com.rachai.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import br.com.rachai.dto.CriarDespesaRequest;
import br.com.rachai.exception.NaoEncontradoException;
import br.com.rachai.model.*;
import br.com.rachai.repository.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class DespesaServiceTest {
    @Mock GrupoService grupos;
    @Mock DespesaRepository despesas;
    @Mock GrupoParticipanteRepository participantes;
    @InjectMocks DespesaService service;

    @Test
    void persisteGrupoDataECentavosSemArredondar() {
        var grupo = new Grupo("Viagem", null, new Usuario("Equipe"));
        when(grupos.buscar(1L)).thenReturn(grupo);
        var participante = new GrupoParticipante(grupo, new Usuario("Ana"));
        org.springframework.test.util.ReflectionTestUtils.setField(participante, "id", 5L);
        when(participantes.findByIdAndGrupoId(5L, 1L)).thenReturn(Optional.of(participante));
        when(despesas.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var data = LocalDate.of(2026, 10, 12);
        var response =
                service.cadastrar(
                        1L,
                        new CriarDespesaRequest(
                                " Mercado ", new BigDecimal("0.01"), data, 5L, List.of(5L)));
        assertEquals("0.01", response.valorTotal());
        assertEquals("Mercado", response.descricao());
        assertEquals(data, response.data());
        var captor = ArgumentCaptor.forClass(Despesa.class);
        verify(despesas).save(captor.capture());
        assertSame(grupo, captor.getValue().getGrupo());
    }

    @Test
    void grupoAusenteNaoPersisteDespesa() {
        when(grupos.buscar(99L)).thenThrow(new NaoEncontradoException("Grupo não encontrado."));
        assertThrows(
                NaoEncontradoException.class,
                () ->
                        service.cadastrar(
                                99L,
                                new CriarDespesaRequest(
                                        "Mercado",
                                        new BigDecimal("10.00"),
                                        LocalDate.now(),
                                        5L,
                                        List.of(5L))));
        verifyNoInteractions(despesas);
    }
}
