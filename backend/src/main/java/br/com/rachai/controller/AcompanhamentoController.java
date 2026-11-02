package br.com.rachai.controller;

import br.com.rachai.dto.AcompanhamentoResponse;
import br.com.rachai.service.AcompanhamentoService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grupos/{grupoId}")
public class AcompanhamentoController {
    private final AcompanhamentoService service;

    public AcompanhamentoController(AcompanhamentoService service) {
        this.service = service;
    }

    @GetMapping("/progresso")
    public AcompanhamentoResponse consultar(@PathVariable Long grupoId) {
        return service.consultar(grupoId);
    }

    public record PendenciasResponse(
            boolean completo,
            List<Long> despesasPendentes,
            List<AcompanhamentoResponse.Pessoa> participantes) {}

    @GetMapping("/pendencias")
    public PendenciasResponse pendencias(@PathVariable Long grupoId) {
        var r = service.consultar(grupoId);
        return new PendenciasResponse(r.completo(), r.despesasPendentes(), r.participantes());
    }
}
