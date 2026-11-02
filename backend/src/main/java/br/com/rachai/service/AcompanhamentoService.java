package br.com.rachai.service;

import br.com.rachai.dto.AcompanhamentoResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.*;
import java.util.*;

@Service
public class AcompanhamentoService {
    private final AcertoService acertos;

    public AcompanhamentoService(AcertoService acertos) {
        this.acertos = acertos;
    }

    @Transactional
    public AcompanhamentoResponse consultar(Long grupoId) {
        var dados = acertos.consultar(grupoId);
        if (!dados.completo())
            return new AcompanhamentoResponse(
                    false,
                    dados.despesasPendentes(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    false,
                    List.of());
        var pagos = dados.registros().stream().filter(a -> a.status().equals("PAGO")).toList();
        long quitados = pagos.size(), pendentes = dados.sugestoes().size();
        var percentual =
                quitados + pendentes == 0
                        ? new BigDecimal("100.00")
                        : BigDecimal.valueOf(quitados)
                                .multiply(new BigDecimal("100"))
                                .divide(
                                        BigDecimal.valueOf(quitados + pendentes),
                                        2,
                                        RoundingMode.DOWN);
        var valorPago =
                pagos.stream()
                        .map(a -> new BigDecimal(a.valor()))
                        .reduce(new BigDecimal("0.00"), BigDecimal::add);
        var valorPendente =
                dados.sugestoes().stream()
                        .map(a -> new BigDecimal(a.valor()))
                        .reduce(new BigDecimal("0.00"), BigDecimal::add);
        var nomes = new TreeMap<Long, String>();
        var pagar = new HashMap<Long, BigDecimal>();
        var receber = new HashMap<Long, BigDecimal>();
        for (var a : dados.sugestoes()) {
            nomes.put(a.devedorId(), a.devedorNome());
            nomes.put(a.recebedorId(), a.recebedorNome());
            pagar.merge(a.devedorId(), new BigDecimal(a.valor()), BigDecimal::add);
            receber.merge(a.recebedorId(), new BigDecimal(a.valor()), BigDecimal::add);
        }
        var pessoas =
                nomes.entrySet().stream()
                        .map(
                                p ->
                                        new AcompanhamentoResponse.Pessoa(
                                                p.getKey(),
                                                p.getValue(),
                                                pagar.getOrDefault(
                                                                p.getKey(), new BigDecimal("0.00"))
                                                        .toPlainString(),
                                                receber.getOrDefault(
                                                                p.getKey(), new BigDecimal("0.00"))
                                                        .toPlainString()))
                        .toList();
        return new AcompanhamentoResponse(
                true,
                List.of(),
                quitados,
                pendentes,
                percentual.toPlainString(),
                valorPago.toPlainString(),
                valorPendente.toPlainString(),
                quitados + pendentes == 0,
                pessoas);
    }
}
