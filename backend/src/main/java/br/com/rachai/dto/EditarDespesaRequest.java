package br.com.rachai.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record EditarDespesaRequest(
        @NotNull(message = "Reabra a despesa antes de salvar.") @PositiveOrZero Long versao,
        @NotNull @Valid CriarDespesaRequest dados) {}
