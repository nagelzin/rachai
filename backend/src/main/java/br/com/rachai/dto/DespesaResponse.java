package br.com.rachai.dto;

import br.com.rachai.model.Despesa;

import java.time.LocalDate;
import java.util.*;

// Dinheiro como string decimal no JSON preserva os centavos no JavaScript.
public record DespesaResponse(
        Long id,
        Long grupoId,
        String descricao,
        String valorTotal,
        LocalDate data,
        PessoaDespesaResponse pagador,
        List<ParticipacaoDespesaResponse> participantes,
        long versao,
        ComprovanteResponse comprovante) {
    public static DespesaResponse from(Despesa despesa) {
        return new DespesaResponse(
                despesa.getId(),
                despesa.getGrupo().getId(),
                despesa.getDescricao(),
                despesa.getValorTotal()
                        .setScale(2, java.math.RoundingMode.UNNECESSARY)
                        .toPlainString(),
                despesa.getData(),
                PessoaDespesaResponse.from(despesa.getPagador()),
                despesa.getEnvolvidos().stream()
                        .map(
                                v ->
                                        ParticipacaoDespesaResponse.from(
                                                v.getParticipante(), v.getValorParte()))
                        .sorted(Comparator.comparing(ParticipacaoDespesaResponse::id))
                        .toList(),
                despesa.getVersao(),
                despesa.getComprovanteNome() == null
                        ? null
                        : new ComprovanteResponse(
                                despesa.getComprovanteNome(),
                                despesa.getComprovanteTipo(),
                                despesa.getComprovanteTamanho()));
    }
}
