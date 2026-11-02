package br.com.rachai.dto;

import br.com.rachai.model.CalculoSaldo;
import br.com.rachai.model.GrupoParticipante;

public record SaldoParticipanteResponse(
        Long participanteId,
        Long usuarioId,
        String nome,
        String totalPago,
        String totalDevido,
        String saldo,
        String situacao) {
    public static SaldoParticipanteResponse from(GrupoParticipante p, CalculoSaldo.Total total) {
        int sinal = total.saldo().signum();
        return new SaldoParticipanteResponse(
                p.getId(),
                p.getUsuario() == null ? null : p.getUsuario().getId(),
                p.getNome(),
                total.pago().toPlainString(),
                total.devido().toPlainString(),
                total.saldo().toPlainString(),
                sinal > 0 ? "RECEBER" : sinal < 0 ? "PAGAR" : "ZERADO");
    }
}
