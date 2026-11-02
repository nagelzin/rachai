package br.com.rachai.dto;

import br.com.rachai.model.Convite;

import java.time.LocalDateTime;

public record ConviteResponse(
        Long id,
        Long grupoId,
        String grupoNome,
        String administradorNome,
        String destinatarioEmail,
        String status,
        LocalDateTime criadoEm) {
    public static ConviteResponse from(Convite c) {
        return new ConviteResponse(
                c.getId(),
                c.getGrupo().getId(),
                c.getGrupo().getNome(),
                c.getGrupo().getResponsavel().getNome(),
                c.getDestinatario().getEmail(),
                c.getStatus().name(),
                c.getCriadoEm());
    }
}
