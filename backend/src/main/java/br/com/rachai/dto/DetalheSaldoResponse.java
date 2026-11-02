package br.com.rachai.dto;

import java.time.LocalDate;
import java.util.List;

public record DetalheSaldoResponse(
        boolean completo,
        List<Long> despesasPendentes,
        SaldoParticipanteResponse participante,
        List<Item> despesas) {
    public record Item(
            Long despesaId,
            String descricao,
            LocalDate data,
            String totalDespesa,
            String pagadorNome,
            String pago,
            String parte,
            String impacto) {}
}
