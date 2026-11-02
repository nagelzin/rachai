package br.com.rachai.model;

import java.math.BigDecimal;
import java.util.*;

/** Divisão exata em centavos; o menor ID de vínculo recebe o primeiro centavo restante. */
public final class DivisaoIgual {
    private DivisaoIgual() {}

    public static Map<Long, BigDecimal> calcular(BigDecimal total, List<Long> participantes) {
        long centavos = total.movePointRight(2).longValueExact();
        if (centavos <= 0
                || participantes.isEmpty()
                || participantes.stream().anyMatch(id -> id == null || id <= 0)
                || new HashSet<>(participantes).size() != participantes.size()) {
            throw new IllegalArgumentException(
                    "Valor positivo e participantes únicos são obrigatórios.");
        }
        var ids = participantes.stream().sorted().toList();
        long base = centavos / ids.size();
        long resto = centavos % ids.size();
        Map<Long, BigDecimal> parcelas = new LinkedHashMap<>();
        for (int i = 0; i < ids.size(); i++) {
            parcelas.put(ids.get(i), BigDecimal.valueOf(base + (i < resto ? 1 : 0), 2));
        }
        return parcelas;
    }
}
