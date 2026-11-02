package br.com.rachai.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/** Compensa devedores e credores em ordem de ID, sem movimentar dinheiro. */
public final class CalculoAcerto {
    private CalculoAcerto() {}

    public record Sugestao(Long devedorId, Long recebedorId, BigDecimal valor) {}

    public static List<Sugestao> calcular(Map<Long, BigDecimal> saldos) {
        var restantes = new TreeMap<Long, BigDecimal>();
        saldos.forEach(
                (id, valor) -> {
                    if (id == null || id <= 0 || valor == null)
                        throw new IllegalArgumentException("Saldo inválido.");
                    restantes.put(id, valor.setScale(2, RoundingMode.UNNECESSARY));
                });
        if (restantes.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add).signum() != 0)
            throw new IllegalArgumentException("Os saldos precisam somar zero.");
        var devedores =
                restantes.keySet().stream().filter(id -> restantes.get(id).signum() < 0).toList();
        var credores =
                restantes.keySet().stream().filter(id -> restantes.get(id).signum() > 0).toList();
        var resultado = new ArrayList<Sugestao>();
        int d = 0, c = 0;
        while (d < devedores.size() && c < credores.size()) {
            var devedor = devedores.get(d);
            var credor = credores.get(c);
            var valor = restantes.get(devedor).negate().min(restantes.get(credor));
            resultado.add(new Sugestao(devedor, credor, valor));
            restantes.put(devedor, restantes.get(devedor).add(valor));
            restantes.put(credor, restantes.get(credor).subtract(valor));
            if (restantes.get(devedor).signum() == 0) d++;
            if (restantes.get(credor).signum() == 0) c++;
        }
        return List.copyOf(resultado);
    }
}
