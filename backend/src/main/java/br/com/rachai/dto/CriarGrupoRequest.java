package br.com.rachai.dto;

import jakarta.validation.constraints.*;

public record CriarGrupoRequest(
        @NotBlank(message = "Informe o nome do grupo.")
                @Size(max = 120, message = "Use até 120 caracteres no nome.")
                String nome,
        @Size(max = 500, message = "Use até 500 caracteres na descrição.") String descricao) {}
