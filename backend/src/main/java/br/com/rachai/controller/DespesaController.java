package br.com.rachai.controller;

import br.com.rachai.dto.*;
import br.com.rachai.service.DespesaService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/grupos/{grupoId}/despesas")
public class DespesaController {
    private final DespesaService service;

    public DespesaController(DespesaService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DespesaResponse cadastrar(
            @PathVariable Long grupoId, @Valid @RequestBody CriarDespesaRequest request) {
        return service.cadastrar(grupoId, request);
    }

    @GetMapping("/{id}")
    public DespesaResponse consultar(@PathVariable Long grupoId, @PathVariable Long id) {
        return service.consultar(grupoId, id);
    }

    @GetMapping
    public java.util.List<DespesaResponse> listar(@PathVariable Long grupoId) {
        return service.listar(grupoId);
    }

    @PostMapping("/previa-divisao")
    public java.util.List<ParticipacaoDespesaResponse> previa(
            @PathVariable Long grupoId, @Valid @RequestBody PreviaDivisaoRequest request) {
        return service.previa(grupoId, request);
    }

    @PutMapping("/{id}")
    public DespesaResponse editar(
            @PathVariable Long grupoId,
            @PathVariable Long id,
            @Valid @RequestBody EditarDespesaRequest request) {
        return service.editar(grupoId, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(
            @PathVariable Long grupoId, @PathVariable Long id, @RequestParam long versao) {
        service.excluir(grupoId, id, versao);
    }
}
