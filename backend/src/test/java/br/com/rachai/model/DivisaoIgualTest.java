package br.com.rachai.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

class DivisaoIgualTest {
    @Test
    void distribuiRestoPorIdIndependentementeDaOrdemDeSelecao() {
        var parcelas = DivisaoIgual.calcular(new BigDecimal("100.00"), List.of(30L, 10L, 20L));
        assertEquals(new BigDecimal("33.34"), parcelas.get(10L));
        assertEquals(new BigDecimal("33.33"), parcelas.get(20L));
        assertEquals(new BigDecimal("33.33"), parcelas.get(30L));
        assertEquals(
                parcelas, DivisaoIgual.calcular(new BigDecimal("100.00"), List.of(20L, 30L, 10L)));
    }

    @Test
    void conservaTodosOsCentavosDoMinimoAoMaximo() {
        for (String valor : List.of("0.01", "0.02", "1.00", "100.00", "9999999999.99")) {
            for (int n = 1; n <= 100; n++) {
                var ids = java.util.stream.LongStream.rangeClosed(1, n).boxed().toList();
                var parcelas = DivisaoIgual.calcular(new BigDecimal(valor), ids).values();
                assertEquals(
                        new BigDecimal(valor),
                        parcelas.stream().reduce(BigDecimal.ZERO, BigDecimal::add));
                assertTrue(parcelas.stream().allMatch(p -> p.signum() >= 0 && p.scale() == 2));
                assertTrue(
                        Collections.max(parcelas)
                                        .subtract(Collections.min(parcelas))
                                        .compareTo(new BigDecimal("0.01"))
                                <= 0);
            }
        }
    }

    @Test
    void rejeitaDadosInconsistentesSemArredondar() {
        assertThrows(
                ArithmeticException.class,
                () -> DivisaoIgual.calcular(new BigDecimal("1.001"), List.of(1L)));
        assertThrows(
                IllegalArgumentException.class,
                () -> DivisaoIgual.calcular(BigDecimal.ZERO, List.of(1L)));
        assertThrows(
                IllegalArgumentException.class,
                () -> DivisaoIgual.calcular(BigDecimal.ONE, List.of()));
        assertThrows(
                IllegalArgumentException.class,
                () -> DivisaoIgual.calcular(BigDecimal.ONE, List.of(1L, 1L)));
    }
}
