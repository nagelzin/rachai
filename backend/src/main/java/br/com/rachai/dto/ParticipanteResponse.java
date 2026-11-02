package br.com.rachai.dto;

import br.com.rachai.model.GrupoParticipante;

public record ParticipanteResponse(Long id, String nome, Long usuarioId, boolean administrador) {
    public static ParticipanteResponse from(GrupoParticipante p) {
        Long usuarioId = p.getUsuario() == null ? null : p.getUsuario().getId();
        return new ParticipanteResponse(
                p.getId(),
                p.getNome(),
                usuarioId,
                p.getGrupo().getResponsavel().getId().equals(usuarioId));
    }
}
