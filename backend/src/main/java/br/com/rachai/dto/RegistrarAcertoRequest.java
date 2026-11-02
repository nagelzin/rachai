package br.com.rachai.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record RegistrarAcertoRequest(
        @NotNull @Positive Long devedorId,
        @NotNull @Positive Long recebedorId,
        @NotNull @DecimalMin("0.01") @Digits(integer = 26, fraction = 2) BigDecimal valor,
        @NotBlank @Size(max = 64) String revisao) {}
