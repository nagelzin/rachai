package br.com.rachai.dto;

import java.util.List;

public record AcompanhamentoResponse(
        boolean completo,
        List<Long> despesasPendentes,
        Long quitados,
        Long pendentes,
        String percentualConcluido,
        String valorPago,
        String valorPendente,
        boolean semAcertos,
        List<Pessoa> participantes) {
    public record Pessoa(Long participanteId, String nome, String aPagar, String aReceber) {}
}
