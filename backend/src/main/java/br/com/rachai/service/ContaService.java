package br.com.rachai.service;

import br.com.rachai.dto.*;
import br.com.rachai.exception.ConflitoException;
import br.com.rachai.model.Usuario;
import br.com.rachai.repository.UsuarioRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContaService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder senhas;

    public ContaService(UsuarioRepository usuarios, PasswordEncoder senhas) {
        this.usuarios = usuarios;
        this.senhas = senhas;
    }

    @Transactional
    public UsuarioResponse cadastrar(CadastroRequest r) {
        String email = Usuario.normalizarEmail(r.email());
        if (usuarios.existsByEmail(email))
            throw new ConflitoException(
                    "Não foi possível cadastrar com esse e-mail. Tente entrar na sua conta.");
        return UsuarioResponse.from(
                usuarios.saveAndFlush(new Usuario(r.nome(), email, senhas.encode(r.senha()))));
    }
}
