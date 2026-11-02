package br.com.rachai.dto;

import jakarta.validation.constraints.NotNull;

public record ResponderConviteRequest(
        @NotNull(message = "Informe se deseja aceitar ou recusar.") Boolean aceitar) {}
