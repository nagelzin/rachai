package br.com.rachai.dto;

import java.util.List;

public record SaldosResponse(
        boolean completo,
        List<Long> despesasPendentes,
        List<SaldoParticipanteResponse> participantes) {}
