package br.com.rachai.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

class DivisaoPersonalizadaTest {
    @Test
    void valoresDesiguaisZeroECentavosPreservados() {
        var parcelas =
                Map.of(
                        1L,
                        new BigDecimal("0"),
                        2L,
                        new BigDecimal("25.01"),
                        3L,
                        new BigDecimal("75.02"));
        var r =
                DivisaoPersonalizada.calcular(
                        new BigDecimal("100.03"), List.of(3L, 1L, 2L), parcelas);
        assertEquals(new BigDecimal("0.00"), r.get(1L));
        assertEquals(
                new BigDecimal("100.03"),
                r.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add));
        assertEquals(List.of(1L, 2L, 3L), new ArrayList<>(r.keySet()));
    }

    @Test
    void rejeitaSomaDiferenteIdsNegativoNuloEDecimaisExcessivos() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        DivisaoPersonalizada.calcular(
                                BigDecimal.TEN, List.of(1L), Map.of(1L, BigDecimal.ONE)));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        DivisaoPersonalizada.calcular(
                                BigDecimal.TEN, List.of(1L), Map.of(2L, BigDecimal.TEN)));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        DivisaoPersonalizada.calcular(
                                BigDecimal.TEN, List.of(1L, 1L), Map.of(1L, BigDecimal.TEN)));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        DivisaoPersonalizada.calcular(
                                BigDecimal.TEN,
                                List.of(1L, 2L),
                                Map.of(1L, new BigDecimal("-1"), 2L, new BigDecimal("11"))));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        DivisaoPersonalizada.calcular(
                                BigDecimal.TEN,
                                List.of(1L, 2L),
                                Map.of(1L, new BigDecimal("0.001"), 2L, new BigDecimal("9.999"))));
        var nulos = new HashMap<Long, BigDecimal>();
        nulos.put(1L, null);
        assertThrows(
                IllegalArgumentException.class,
                () -> DivisaoPersonalizada.calcular(BigDecimal.TEN, List.of(1L), nulos));
    }

    @Test
    void divisaoIgualContinuaCompativel() {
        assertEquals(
                DivisaoIgual.calcular(BigDecimal.TEN, List.of(1L, 2L, 3L)),
                DivisaoPersonalizada.calcular(BigDecimal.TEN, List.of(1L, 2L, 3L), null));
    }
}
