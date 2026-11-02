package br.com.rachai.model;

import java.math.*;
import java.util.*;

public final class DivisaoPersonalizada {
    private DivisaoPersonalizada() {}

    public static Map<Long, BigDecimal> calcular(
            BigDecimal total, List<Long> ids, Map<Long, BigDecimal> valores) {
        if (valores == null) return DivisaoIgual.calcular(total, ids);
        if (total == null
                || total.signum() <= 0
                || ids.isEmpty()
                || new HashSet<>(ids).size() != ids.size()
                || ids.stream().anyMatch(id -> id == null || id <= 0)
                || !valores.keySet().equals(new HashSet<>(ids)))
            throw new IllegalArgumentException(
                    "Informe exatamente uma parcela para cada participante selecionado.");
        var parcelas = new TreeMap<Long, BigDecimal>();
        try {
            total.setScale(2, RoundingMode.UNNECESSARY);
            for (var id : ids) {
                var valor = valores.get(id);
                if (valor == null || valor.signum() < 0)
                    throw new IllegalArgumentException("As parcelas devem ser zero ou positivas.");
                parcelas.put(id, valor.setScale(2, RoundingMode.UNNECESSARY));
            }
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Use no máximo duas casas decimais nas parcelas.");
        }
        if (parcelas.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add).compareTo(total)
                != 0)
            throw new IllegalArgumentException(
                    "A soma dos valores individuais deve ser igual ao total da despesa.");
        return Collections.unmodifiableMap(parcelas);
    }
}
