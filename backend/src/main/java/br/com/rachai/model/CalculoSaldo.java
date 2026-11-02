package br.com.rachai.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/** Cálculo derivado das parcelas persistidas, sem acesso ao banco ou arredondamento. */
public final class CalculoSaldo {
    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private CalculoSaldo() {}

    public record Parcela(Long participanteId, BigDecimal valor) {}

    public record Lancamento(Long id, Long pagadorId, BigDecimal valor, List<Parcela> parcelas) {}

    public record Total(BigDecimal pago, BigDecimal devido) {
        public BigDecimal saldo() {
            return pago.subtract(devido);
        }
    }

    public record Resultado(Map<Long, Total> totais, List<Long> despesasPendentes) {}

    public static Resultado calcular(List<Long> participantes, List<Lancamento> despesas) {
        var ids = new HashSet<>(participantes);
        if (ids.size() != participantes.size()
                || ids.stream().anyMatch(id -> id == null || id <= 0))
            throw new IllegalArgumentException("Participantes inválidos.");
        var pendentes =
                despesas.stream()
                        .filter(d -> !completa(d, ids))
                        .map(Lancamento::id)
                        .sorted()
                        .toList();
        // Não apresenta um saldo parcial como se incluísse todas as despesas do grupo.
        if (!pendentes.isEmpty()) return new Resultado(Map.of(), pendentes);
        var totais = new LinkedHashMap<Long, Total>();
        participantes.stream().sorted().forEach(id -> totais.put(id, new Total(ZERO, ZERO)));
        for (var despesa : despesas) {
            var pagador = totais.get(despesa.pagadorId());
            totais.put(
                    despesa.pagadorId(),
                    new Total(pagador.pago().add(dinheiro(despesa.valor())), pagador.devido()));
            for (var parcela : despesa.parcelas()) {
                var participante = totais.get(parcela.participanteId());
                totais.put(
                        parcela.participanteId(),
                        new Total(
                                participante.pago(),
                                participante.devido().add(dinheiro(parcela.valor()))));
            }
        }
        return new Resultado(Collections.unmodifiableMap(totais), List.of());
    }

    private static boolean completa(Lancamento d, Set<Long> participantes) {
        if (!participantes.contains(d.pagadorId())
                || d.valor() == null
                || d.valor().signum() <= 0
                || d.parcelas() == null
                || d.parcelas().isEmpty()) return false;
        var vistos = new HashSet<Long>();
        BigDecimal soma = ZERO;
        try {
            dinheiro(d.valor());
            for (var p : d.parcelas()) {
                if (!participantes.contains(p.participanteId())
                        || !vistos.add(p.participanteId())
                        || p.valor() == null
                        || p.valor().signum() < 0) return false;
                soma = soma.add(dinheiro(p.valor()));
            }
            return soma.compareTo(d.valor()) == 0;
        } catch (ArithmeticException e) {
            return false;
        }
    }

    private static BigDecimal dinheiro(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.UNNECESSARY);
    }
}
