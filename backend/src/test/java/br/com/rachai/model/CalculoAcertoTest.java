package br.com.rachai.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

class CalculoAcertoTest {
    @Test
    void variosDevedoresCredoresCentavosEZero() {
        var saldos =
                Map.of(
                        1L,
                        new BigDecimal("-50.03"),
                        2L,
                        new BigDecimal("-25.02"),
                        3L,
                        new BigDecimal("60.00"),
                        4L,
                        new BigDecimal("15.05"),
                        5L,
                        new BigDecimal("0.00"));
        var acertos = CalculoAcerto.calcular(saldos);
        assertEquals(3, acertos.size());
        assertEquals(new BigDecimal("50.03"), acertos.getFirst().valor());
        var restantes = new HashMap<>(saldos);
        for (var a : acertos) {
            assertTrue(a.valor().signum() > 0);
            restantes.compute(a.devedorId(), (id, v) -> v.add(a.valor()));
            restantes.compute(a.recebedorId(), (id, v) -> v.subtract(a.valor()));
        }
        assertTrue(restantes.values().stream().allMatch(v -> v.signum() == 0));
        assertEquals(acertos, CalculoAcerto.calcular(new TreeMap<>(saldos)));
    }

    @Test
    void vazioQuitadoGrandesEInconsistentes() {
        assertTrue(CalculoAcerto.calcular(Map.of()).isEmpty());
        assertTrue(CalculoAcerto.calcular(Map.of(1L, new BigDecimal("0.00"))).isEmpty());
        assertEquals(
                new BigDecimal("19999999999.98"),
                CalculoAcerto.calcular(
                                Map.of(
                                        1L,
                                        new BigDecimal("-19999999999.98"),
                                        2L,
                                        new BigDecimal("19999999999.98")))
                        .getFirst()
                        .valor());
        assertThrows(
                IllegalArgumentException.class,
                () -> CalculoAcerto.calcular(Map.of(1L, BigDecimal.ONE)));
        assertThrows(
                ArithmeticException.class,
                () ->
                        CalculoAcerto.calcular(
                                Map.of(1L, new BigDecimal("0.001"), 2L, new BigDecimal("-0.001"))));
    }

    @Test
    void conservaSaldosEmDiversosGrupos() {
        var random = new Random(42);
        for (int rodada = 0; rodada < 100; rodada++) {
            var saldos = new HashMap<Long, BigDecimal>();
            var soma = BigDecimal.ZERO;
            for (long id = 1; id < 10; id++) {
                var valor = BigDecimal.valueOf(random.nextInt(20001) - 10000, 2);
                saldos.put(id, valor);
                soma = soma.add(valor);
            }
            saldos.put(10L, soma.negate());
            var acertos = CalculoAcerto.calcular(saldos);
            assertTrue(acertos.size() <= 9);
            for (var a : acertos) {
                saldos.compute(a.devedorId(), (id, v) -> v.add(a.valor()));
                saldos.compute(a.recebedorId(), (id, v) -> v.subtract(a.valor()));
            }
            assertTrue(saldos.values().stream().allMatch(v -> v.signum() == 0));
        }
    }
}
