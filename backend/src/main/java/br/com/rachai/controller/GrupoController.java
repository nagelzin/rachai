package br.com.rachai.controller;

import br.com.rachai.dto.*;
import br.com.rachai.service.GrupoService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/grupos")
public class GrupoController {
    private final GrupoService service;

    public GrupoController(GrupoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<GrupoResponse> criar(@Valid @RequestBody CriarGrupoRequest request) {
        var grupo = service.criar(request);
        return ResponseEntity.created(URI.create("/api/grupos/" + grupo.id())).body(grupo);
    }

    @GetMapping
    public java.util.List<GrupoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public GrupoResponse consultar(@PathVariable Long id) {
        return service.consultar(id);
    }

}
