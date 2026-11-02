package br.com.rachai.dto;

import br.com.rachai.model.Acerto;

import java.time.LocalDateTime;

public record RegistroAcertoResponse(
        Long id,
        Long devedorId,
        Long recebedorId,
        Long devedorUsuarioId,
        Long recebedorUsuarioId,
        String devedorNome,
        String recebedorNome,
        String valor,
        String status,
        Long registradoPorId,
        LocalDateTime criadoEm,
        LocalDateTime canceladoEm,
        long versao) {
    public static RegistroAcertoResponse from(Acerto a) {
        return new RegistroAcertoResponse(
                a.getId(),
                a.getDevedor().getId(),
                a.getRecebedor().getId(),
                a.getDevedor().getUsuario().getId(),
                a.getRecebedor().getUsuario().getId(),
                a.getDevedor().getNome(),
                a.getRecebedor().getNome(),
                a.getValor().setScale(2).toPlainString(),
                a.getStatus(),
                a.getRegistradoPor().getId(),
                a.getCriadoEm(),
                a.getCanceladoEm(),
                a.getVersao());
    }
}
