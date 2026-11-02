package br.com.rachai.dto;

import jakarta.validation.constraints.*;

public record CadastroRequest(
        @NotBlank(message = "Informe seu nome.") @Size(max = 120) String nome,
        @NotBlank(message = "Informe seu e-mail.")
                @Email(message = "Informe um e-mail válido.")
                @Size(max = 254)
                String email,
        @NotBlank(message = "Informe uma senha.")
                @Size(min = 15, max = 128, message = "Use de 15 a 128 caracteres na senha.")
                String senha) {}
