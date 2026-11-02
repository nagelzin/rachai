package br.com.rachai.controller;

import br.com.rachai.dto.DespesaResponse;
import br.com.rachai.service.ComprovanteService;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/grupos/{grupoId}/despesas/{id}/comprovante")
public class ComprovanteController {
    private final ComprovanteService service;

    public ComprovanteController(ComprovanteService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DespesaResponse enviar(
            @PathVariable Long grupoId,
            @PathVariable Long id,
            @RequestParam long versao,
            @RequestParam MultipartFile arquivo) {
        return service.salvar(grupoId, id, versao, arquivo);
    }

    @DeleteMapping
    public DespesaResponse remover(
            @PathVariable Long grupoId, @PathVariable Long id, @RequestParam long versao) {
        return service.remover(grupoId, id, versao);
    }

    @GetMapping
    public ResponseEntity<byte[]> consultar(
            @PathVariable Long grupoId,
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean baixar) {
        var arquivo = service.consultar(grupoId, id);
        // PDF é baixado para o leitor do dispositivo; imagens podem ser abertas no navegador.
        var disposicao =
                baixar || arquivo.tipo().equals("application/pdf")
                        ? ContentDisposition.attachment()
                        : ContentDisposition.inline();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(arquivo.tipo()))
                .contentLength(arquivo.bytes().length)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        disposicao
                                .filename(arquivo.nome(), StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .header("X-Content-Type-Options", "nosniff")
                .header(
                        "Content-Security-Policy",
                        "sandbox; default-src 'none'; frame-ancestors 'none'")
                .cacheControl(CacheControl.noStore())
                .body(arquivo.bytes());
    }
}
