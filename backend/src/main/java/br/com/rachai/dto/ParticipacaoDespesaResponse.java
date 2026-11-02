package br.com.rachai.dto;

import br.com.rachai.model.GrupoParticipante;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record ParticipacaoDespesaResponse(Long id, String nome, Long usuarioId, String valorParte) {
    public static ParticipacaoDespesaResponse from(GrupoParticipante p, BigDecimal valor) {
        return new ParticipacaoDespesaResponse(
                p.getId(),
                p.getNome(),
                p.getUsuario() == null ? null : p.getUsuario().getId(),
                valor == null ? null : valor.setScale(2, RoundingMode.UNNECESSARY).toPlainString());
    }
}
