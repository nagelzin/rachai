package br.com.rachai.service;

import br.com.rachai.dto.*;
import br.com.rachai.model.CalculoSaldo;
import br.com.rachai.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SaldoService {
    private final GrupoService grupos;
    private final GrupoParticipanteRepository participantes;
    private final DespesaRepository despesas;

    public SaldoService(
            GrupoService grupos,
            GrupoParticipanteRepository participantes,
            DespesaRepository despesas) {
        this.grupos = grupos;
        this.participantes = participantes;
        this.despesas = despesas;
    }

    @Transactional
    public DetalheSaldoResponse detalhar(Long grupoId, Long participanteId) {
        var saldos = consultar(grupoId);
        participantes
                .findByIdAndGrupoId(participanteId, grupoId)
                .orElseThrow(
                        () ->
                                new br.com.rachai.exception.NaoEncontradoException(
                                        "Participante não encontrado neste grupo."));
        if (!saldos.completo())
            return new DetalheSaldoResponse(false, saldos.despesasPendentes(), null, List.of());
        var participante =
                saldos.participantes().stream()
                        .filter(p -> p.participanteId().equals(participanteId))
                        .findFirst()
                        .orElseThrow();
        var itens =
                despesas.findByGrupoIdOrderByDataDescIdDesc(grupoId).stream()
                        .filter(
                                d ->
                                        d.getPagador().getId().equals(participanteId)
                                                || d.getEnvolvidos().stream()
                                                        .anyMatch(
                                                                p ->
                                                                        p.getParticipante()
                                                                                .getId()
                                                                                .equals(
                                                                                        participanteId)))
                        .map(
                                d -> {
                                    var zero = new java.math.BigDecimal("0.00");
                                    var pago =
                                            d.getPagador().getId().equals(participanteId)
                                                    ? d.getValorTotal()
                                                    : zero;
                                    var parte =
                                            d.getEnvolvidos().stream()
                                                    .filter(
                                                            p ->
                                                                    p.getParticipante()
                                                                            .getId()
                                                                            .equals(participanteId))
                                                    .map(p -> p.getValorParte())
                                                    .findFirst()
                                                    .orElse(zero);
                                    return new DetalheSaldoResponse.Item(
                                            d.getId(),
                                            d.getDescricao(),
                                            d.getData(),
                                            d.getValorTotal().setScale(2).toPlainString(),
                                            d.getPagador().getNome(),
                                            pago.setScale(2).toPlainString(),
                                            parte.setScale(2).toPlainString(),
                                            pago.subtract(parte).setScale(2).toPlainString());
                                })
                        .toList();
        return new DetalheSaldoResponse(true, List.of(), participante, itens);
    }

    @Transactional
    public SaldosResponse consultar(Long grupoId) {
        grupos.buscar(grupoId);
        // O mesmo lock das mutações impede misturar membros/despesas de instantes diferentes.
        // Não grava saldos nem altera versões. Revalida o acesso depois de obter o lock.
        grupos.bloquear(grupoId);
        grupos.buscar(grupoId);
        var membros = participantes.findByGrupoIdOrderByIdAsc(grupoId);
        var lancamentos =
                despesas.findByGrupoIdOrderByDataDescIdDesc(grupoId).stream()
                        .map(
                                d ->
                                        new CalculoSaldo.Lancamento(
                                                d.getId(),
                                                d.getPagador() == null
                                                        ? null
                                                        : d.getPagador().getId(),
                                                d.getValorTotal(),
                                                d.getEnvolvidos().stream()
                                                        .map(
                                                                p ->
                                                                        new CalculoSaldo.Parcela(
                                                                                p.getParticipante()
                                                                                        .getId(),
                                                                                p.getValorParte()))
                                                        .toList()))
                        .toList();
        var resultado =
                CalculoSaldo.calcular(membros.stream().map(p -> p.getId()).toList(), lancamentos);
        if (!resultado.despesasPendentes().isEmpty())
            return new SaldosResponse(false, resultado.despesasPendentes(), List.of());
        return new SaldosResponse(
                true,
                List.of(),
                membros.stream()
                        .map(
                                p ->
                                        SaldoParticipanteResponse.from(
                                                p, resultado.totais().get(p.getId())))
                        .toList());
    }
}
