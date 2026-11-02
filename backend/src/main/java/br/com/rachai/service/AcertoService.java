package br.com.rachai.service;

import br.com.rachai.dto.*;
import br.com.rachai.model.CalculoAcerto;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AcertoService {
    private final SaldoService saldos;
    private final GrupoService grupos;
    private final DespesaService despesas;
    private final br.com.rachai.repository.AcertoRepository registros;
    private final br.com.rachai.repository.GrupoParticipanteRepository membros;
    private final br.com.rachai.security.UsuarioAtual atual;

    public AcertoService(
            SaldoService saldos,
            GrupoService grupos,
            DespesaService despesas,
            br.com.rachai.repository.AcertoRepository registros,
            br.com.rachai.repository.GrupoParticipanteRepository membros,
            br.com.rachai.security.UsuarioAtual atual) {
        this.saldos = saldos;
        this.grupos = grupos;
        this.despesas = despesas;
        this.registros = registros;
        this.membros = membros;
        this.atual = atual;
    }

    @Transactional
    public AcertosResponse consultar(Long grupoId) {
        var base = saldos.consultar(grupoId);
        var historico =
                registros.findByGrupoIdOrderByIdAsc(grupoId).stream()
                        .map(RegistroAcertoResponse::from)
                        .toList();
        var revisao = revisao(base.toString() + historico.toString());
        if (!base.completo())
            return new AcertosResponse(
                    false, base.despesasPendentes(), List.of(), null, revisao, historico);
        var pessoas =
                base.participantes().stream()
                        .collect(
                                Collectors.toMap(
                                        SaldoParticipanteResponse::participanteId, p -> p));
        var valores =
                base.participantes().stream()
                        .collect(
                                Collectors.toMap(
                                        SaldoParticipanteResponse::participanteId,
                                        p -> new BigDecimal(p.saldo())));
        for (var a : historico)
            if (a.status().equals("PAGO")) {
                valores.compute(a.devedorId(), (id, v) -> v.add(new BigDecimal(a.valor())));
                valores.compute(a.recebedorId(), (id, v) -> v.subtract(new BigDecimal(a.valor())));
            }
        var sugestoes =
                CalculoAcerto.calcular(valores).stream()
                        .map(
                                a -> {
                                    var d = pessoas.get(a.devedorId());
                                    var r = pessoas.get(a.recebedorId());
                                    return new AcertosResponse.Item(
                                            a.devedorId(),
                                            a.recebedorId(),
                                            d.usuarioId(),
                                            r.usuarioId(),
                                            d.nome(),
                                            r.nome(),
                                            a.valor().toPlainString());
                                })
                        .toList();
        var texto =
                new StringBuilder("Rachaí — ")
                        .append(grupos.consultar(grupoId).nome())
                        .append("\n\nDespesas\n");
        var lista = despesas.listar(grupoId);
        if (lista.isEmpty()) texto.append("Nenhuma despesa.\n");
        for (var d : lista) {
            texto.append("#")
                    .append(d.id())
                    .append(" · ")
                    .append(d.descricao())
                    .append(" · ")
                    .append(d.data())
                    .append(" · R$ ")
                    .append(d.valorTotal().replace('.', ','))
                    .append(" · pago por ")
                    .append(d.pagador().nome())
                    .append("\n");
            for (var p : d.participantes())
                texto.append("  ")
                        .append(p.nome())
                        .append(" (#")
                        .append(p.id())
                        .append("): R$ ")
                        .append(p.valorParte().replace('.', ','))
                        .append("\n");
        }
        texto.append("\nSaldos das despesas (positivo = receber; negativo = pagar)\n");
        for (var p : base.participantes())
            texto.append(p.nome())
                    .append(" (#")
                    .append(p.participanteId())
                    .append("): R$ ")
                    .append(p.saldo().replace('.', ','))
                    .append("\n");
        texto.append("\nPagamentos registrados\n");
        for (var a : historico)
            texto.append(a.devedorNome())
                    .append(" → ")
                    .append(a.recebedorNome())
                    .append(": R$ ")
                    .append(a.valor().replace('.', ','))
                    .append(" · ")
                    .append(a.status())
                    .append("\n");
        texto.append("\nAcertos ainda necessários (consideram os pagamentos registrados)\n");
        if (sugestoes.isEmpty()) texto.append("Nenhum acerto necessário.\n");
        for (var a : sugestoes)
            texto.append(a.devedorNome())
                    .append(" (#")
                    .append(a.devedorId())
                    .append(") → ")
                    .append(a.recebedorNome())
                    .append(" (#")
                    .append(a.recebedorId())
                    .append("): R$ ")
                    .append(a.valor().replace('.', ','))
                    .append("\n");
        texto.append(
                "\n"
                    + "Resumo dos dados consultados; alterações posteriores podem mudar os valores."
                    + " O Rachaí não realiza transferências.");
        return new AcertosResponse(
                true, List.of(), sugestoes, texto.toString(), revisao, historico);
    }

    @Transactional
    public RegistroAcertoResponse registrar(Long grupoId, RegistrarAcertoRequest r) {
        var consulta = consultar(grupoId);
        if (!consulta.completo() || !consulta.revisao().equals(r.revisao()))
            throw new br.com.rachai.exception.ConflitoException(
                    "As contas mudaram. Atualize os acertos e confira novamente.");
        var sugestao =
                consulta.sugestoes().stream()
                        .filter(
                                a ->
                                        a.devedorId().equals(r.devedorId())
                                                && a.recebedorId().equals(r.recebedorId())
                                                && new BigDecimal(a.valor()).compareTo(r.valor())
                                                        == 0)
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new br.com.rachai.exception.ConflitoException(
                                                "Este acerto não corresponde às sugestões atuais."
                                                    + " Atualize a lista."));
        autorizar(sugestao.devedorUsuarioId(), sugestao.recebedorUsuarioId());
        return RegistroAcertoResponse.from(
                registros.saveAndFlush(
                        new br.com.rachai.model.Acerto(
                                grupos.buscar(grupoId),
                                membros.findByIdAndGrupoId(r.devedorId(), grupoId).orElseThrow(),
                                membros.findByIdAndGrupoId(r.recebedorId(), grupoId).orElseThrow(),
                                r.valor(),
                                atual.obter())));
    }

    @Transactional
    public RegistroAcertoResponse cancelar(Long grupoId, Long id, long versao) {
        grupos.buscar(grupoId);
        grupos.bloquear(grupoId);
        grupos.buscar(grupoId);
        var a =
                registros
                        .findByIdAndGrupoId(id, grupoId)
                        .orElseThrow(
                                () ->
                                        new br.com.rachai.exception.NaoEncontradoException(
                                                "Acerto não encontrado neste grupo."));
        autorizar(a.getDevedor().getUsuario().getId(), a.getRecebedor().getUsuario().getId());
        if (a.getVersao() != versao || !a.getStatus().equals("PAGO"))
            throw new br.com.rachai.exception.ConflitoException(
                    "Este registro mudou. Atualize os acertos.");
        a.cancelar();
        return RegistroAcertoResponse.from(registros.saveAndFlush(a));
    }

    private void autorizar(Long devedor, Long recebedor) {
        if (devedor == null || recebedor == null)
            throw new br.com.rachai.exception.ConflitoException(
                    "Corrija os vínculos legados das despesas antes de registrar pagamentos entre"
                        + " contas.");
        var id = atual.obter().getId();
        if (!id.equals(devedor) && !id.equals(recebedor))
            throw new org.springframework.security.access.AccessDeniedException(
                    "Somente quem pagou ou recebeu pode registrar ou cancelar este pagamento.");
    }

    private String revisao(String texto) {
        try {
            return HexFormat.of()
                    .formatHex(
                            java.security.MessageDigest.getInstance("SHA-256")
                                    .digest(
                                            texto.getBytes(
                                                    java.nio.charset.StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
