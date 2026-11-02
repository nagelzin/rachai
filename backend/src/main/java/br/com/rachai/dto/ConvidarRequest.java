package br.com.rachai.dto;

import jakarta.validation.constraints.*;

public record ConvidarRequest(
        @NotBlank @Email(message = "Informe um e-mail válido.") @Size(max = 254) String email) {}
