package br.com.rachai.controller;

import br.com.rachai.dto.*;
import br.com.rachai.service.ConviteService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ConviteController {
    private final ConviteService service;

    public ConviteController(ConviteService service) {
        this.service = service;
    }

    @PostMapping("/grupos/{grupoId}/convites")
    @ResponseStatus(HttpStatus.CREATED)
    public ConviteResponse convidar(
            @PathVariable Long grupoId, @Valid @RequestBody ConvidarRequest r) {
        return service.convidar(grupoId, r);
    }

    @GetMapping("/grupos/{grupoId}/convites")
    public List<ConviteResponse> enviados(@PathVariable Long grupoId) {
        return service.enviados(grupoId);
    }

    @GetMapping("/convites")
    public List<ConviteResponse> recebidos() {
        return service.recebidos();
    }

    @PostMapping("/convites/{id}/resposta")
    public ConviteResponse responder(
            @PathVariable Long id, @Valid @RequestBody ResponderConviteRequest r) {
        return service.responder(id, r.aceitar());
    }
}
