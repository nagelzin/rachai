package br.com.rachai.dto;

import java.util.List;

public record AcertosResponse(
        boolean completo,
        List<Long> despesasPendentes,
        List<Item> sugestoes,
        String resumo,
        String revisao,
        List<RegistroAcertoResponse> registros) {
    public record Item(
            Long devedorId,
            Long recebedorId,
            Long devedorUsuarioId,
            Long recebedorUsuarioId,
            String devedorNome,
            String recebedorNome,
            String valor) {}
}
