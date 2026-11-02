package br.com.rachai.controller;

import br.com.rachai.dto.SaldosResponse;
import br.com.rachai.service.SaldoService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/grupos/{grupoId}/saldos")
public class SaldoController {
    private final SaldoService service;

    public SaldoController(SaldoService service) {
        this.service = service;
    }

    @GetMapping
    public SaldosResponse consultar(@PathVariable Long grupoId) {
        return service.consultar(grupoId);
    }

}
