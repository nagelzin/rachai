package br.com.rachai.controller;

import br.com.rachai.dto.ParticipanteResponse;
import br.com.rachai.service.ParticipanteService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grupos/{grupoId}/participantes")
public class ParticipanteController {
    private final ParticipanteService service;

    public ParticipanteController(ParticipanteService service) {
        this.service = service;
    }

    @GetMapping
    public List<ParticipanteResponse> listar(@PathVariable Long grupoId) {
        return service.listar(grupoId);
    }

    @DeleteMapping("/{participanteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long grupoId, @PathVariable Long participanteId) {
        service.remover(grupoId, participanteId);
    }
}
