package br.com.rachai.controller;

import br.com.rachai.dto.AcertosResponse;
import br.com.rachai.service.AcertoService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/grupos/{grupoId}/acertos")
public class AcertoController {
    private final AcertoService service;

    public AcertoController(AcertoService service) {
        this.service = service;
    }

    @GetMapping("/sugestoes")
    public AcertosResponse consultar(@PathVariable Long grupoId) {
        return service.consultar(grupoId);
    }

    @PostMapping
    public org.springframework.http.ResponseEntity<br.com.rachai.dto.RegistroAcertoResponse>
            registrar(
                    @PathVariable Long grupoId,
                    @jakarta.validation.Valid @RequestBody
                            br.com.rachai.dto.RegistrarAcertoRequest request) {
        var registro = service.registrar(grupoId, request);
        return org.springframework.http.ResponseEntity.created(
                        java.net.URI.create("/api/grupos/" + grupoId + "/acertos/" + registro.id()))
                .body(registro);
    }

    public record CancelarRequest(
            @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.PositiveOrZero
                    Long versao) {}

    @PostMapping("/{id}/cancelamento")
    public br.com.rachai.dto.RegistroAcertoResponse cancelar(
            @PathVariable Long grupoId,
            @PathVariable Long id,
            @jakarta.validation.Valid @RequestBody CancelarRequest request) {
        return service.cancelar(grupoId, id, request.versao());
    }
}
