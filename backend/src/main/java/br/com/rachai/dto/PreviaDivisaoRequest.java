package br.com.rachai.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record PreviaDivisaoRequest(
        @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal valorTotal,
        @NotEmpty List<@NotNull @Positive Long> participanteIds,
        java.util.Map<
                        @NotNull @Positive Long,
                        @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal>
                valoresIndividuais) {
    public PreviaDivisaoRequest(BigDecimal valorTotal, List<Long> participanteIds) {
        this(valorTotal, participanteIds, null);
    }
}
