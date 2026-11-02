package br.com.rachai.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Entity
@Table(name = "despesas")
public class Despesa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grupo_id", nullable = false)
    private Grupo grupo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pagador_participante_id")
    private GrupoParticipante pagador;

    @OneToMany(mappedBy = "despesa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DespesaParticipante> envolvidos = new ArrayList<>();

    @Column(nullable = false, length = 200)
    private String descricao;

    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "data_despesa", nullable = false)
    private LocalDate data;

    @Column(nullable = false)
    private long versao;

    @Column(name = "tipo_divisao", nullable = false, length = 15)
    private String tipoDivisao = "IGUAL";

    public String getTipoDivisao() {
        return tipoDivisao;
    }

    public void personalizar(Map<Long, BigDecimal> valores) {
        if (valores == null) {
            tipoDivisao = "IGUAL";
            return;
        }
        var parcelas =
                DivisaoPersonalizada.calcular(
                        valorTotal,
                        envolvidos.stream().map(v -> v.getParticipante().getId()).toList(),
                        valores);
        envolvidos.forEach(v -> v.definirValorParte(parcelas.get(v.getParticipante().getId())));
        tipoDivisao = "PERSONALIZADA";
    }

    @Column(name = "comprovante_nome", length = 120)
    private String comprovanteNome;

    @Column(name = "comprovante_tipo", length = 100)
    private String comprovanteTipo;

    @Column(name = "comprovante_tamanho")
    private Long comprovanteTamanho;

    public String getComprovanteNome() {
        return comprovanteNome;
    }

    public String getComprovanteTipo() {
        return comprovanteTipo;
    }

    public Long getComprovanteTamanho() {
        return comprovanteTamanho;
    }

    public void definirComprovante(String nome, String tipo, Long tamanho) {
        comprovanteNome = nome;
        comprovanteTipo = tipo;
        comprovanteTamanho = tamanho;
        versao++;
    }

    protected Despesa() {}

    public Despesa(Grupo grupo, String descricao, BigDecimal valorTotal, LocalDate data) {
        this.grupo = grupo;
        this.descricao = descricao;
        this.valorTotal = valorTotal.setScale(2, RoundingMode.UNNECESSARY);
        this.data = data;
    }

    public Long getId() {
        return id;
    }

    public long getVersao() {
        return versao;
    }

    // O service confere a versão e mantém o lock do grupo até o commit.
    // Incrementa inclusive quando somente a lista de envolvidos foi alterada.
    public void corrigir(
            String descricao,
            BigDecimal valor,
            LocalDate data,
            GrupoParticipante pagador,
            List<GrupoParticipante> participantes) {
        this.descricao = descricao;
        this.valorTotal = valor.setScale(2, RoundingMode.UNNECESSARY);
        this.data = data;
        this.pagador = pagador;
        definirEnvolvidos(participantes);
        versao++;
    }

    public GrupoParticipante getPagador() {
        return pagador;
    }

    public void definirPagador(GrupoParticipante pagador) {
        this.pagador = pagador;
    }

    public List<DespesaParticipante> getEnvolvidos() {
        return envolvidos;
    }

    public void definirEnvolvidos(List<GrupoParticipante> participantes) {
        var ids = participantes.stream().map(GrupoParticipante::getId).toList();
        envolvidos.removeIf(v -> !ids.contains(v.getParticipante().getId()));
        var existentes = envolvidos.stream().map(v -> v.getParticipante().getId()).toList();
        participantes.stream()
                .filter(p -> !existentes.contains(p.getId()))
                .forEach(p -> envolvidos.add(new DespesaParticipante(this, p)));
        var parcelas = DivisaoIgual.calcular(valorTotal, ids);
        envolvidos.forEach(v -> v.definirValorParte(parcelas.get(v.getParticipante().getId())));
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public LocalDate getData() {
        return data;
    }
}
