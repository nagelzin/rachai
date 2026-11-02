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
