package br.com.rachai.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CriarDespesaRequest(
        @NotBlank(message = "Informe a descrição da despesa.")
                @Size(max = 200, message = "Use até 200 caracteres na descrição.")
                String descricao,
        @NotNull(message = "Informe o valor da despesa.")
                @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero.")
                @Digits(
                        integer = 10,
                        fraction = 2,
                        message = "Use até 10 dígitos inteiros e 2 casas decimais no valor.")
                BigDecimal valorTotal,
        @NotNull(message = "Informe a data da despesa.") LocalDate data,
        @NotNull(message = "Selecione quem pagou.") @Positive Long pagadorId,
        @NotEmpty(message = "Selecione pelo menos um participante da despesa.")
                List<@NotNull @Positive Long> participanteIds) {}
