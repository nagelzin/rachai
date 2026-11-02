package br.com.rachai.dto;

import br.com.rachai.model.Grupo;

import java.time.LocalDateTime;

public record GrupoResponse(
        Long id,
        String nome,
        String descricao,
        Long usuarioResponsavelId,
        String responsavelNome,
        LocalDateTime criadoEm) {
    public static GrupoResponse from(Grupo grupo) {
        return new GrupoResponse(
                grupo.getId(),
                grupo.getNome(),
                grupo.getDescricao(),
                grupo.getResponsavel().getId(),
                grupo.getResponsavel().getNome(),
                grupo.getCriadoEm());
    }
}
