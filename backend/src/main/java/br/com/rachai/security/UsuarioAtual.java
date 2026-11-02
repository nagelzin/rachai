package br.com.rachai.security;

import br.com.rachai.model.Usuario;
import br.com.rachai.repository.UsuarioRepository;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UsuarioAtual {
    private final UsuarioRepository usuarios;

    public UsuarioAtual(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public Usuario obter() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated())
            throw new AuthenticationCredentialsNotFoundException("Entre na sua conta.");
        return usuarios.findByEmail(Usuario.normalizarEmail(auth.getName()))
                .orElseThrow(
                        () ->
                                new AuthenticationCredentialsNotFoundException(
                                        "Entre na sua conta."));
    }
}
