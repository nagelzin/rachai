package br.com.rachai.dto;

import br.com.rachai.model.Usuario;

public record UsuarioResponse(Long id, String nome, String email) {
    public static UsuarioResponse from(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail());
    }
}
