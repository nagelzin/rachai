package br.com.rachai.dto;

import br.com.rachai.model.GrupoParticipante;

public record PessoaDespesaResponse(Long id, String nome, Long usuarioId) {
    public static PessoaDespesaResponse from(GrupoParticipante p) {
        return p == null
                ? null
                : new PessoaDespesaResponse(
                        p.getId(),
                        p.getNome(),
                        p.getUsuario() == null ? null : p.getUsuario().getId());
    }
}
