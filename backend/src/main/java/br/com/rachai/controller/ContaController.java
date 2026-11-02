package br.com.rachai.controller;

import br.com.rachai.dto.*;
import br.com.rachai.security.UsuarioAtual;
import br.com.rachai.service.ContaService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class ContaController {
    private final ContaService contas;
    private final UsuarioAtual atual;

    public ContaController(ContaService contas, UsuarioAtual atual) {
        this.contas = contas;
        this.atual = atual;
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("token", token.getToken(), "headerName", token.getHeaderName());
    }

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastrar(@Valid @RequestBody CadastroRequest r) {
        return contas.cadastrar(r);
    }

    @GetMapping("/eu")
    public UsuarioResponse eu() {
        return UsuarioResponse.from(atual.obter());
    }
}
