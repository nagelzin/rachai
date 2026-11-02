package br.com.rachai.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "acertos")
public class Acerto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grupo_id")
    private Grupo grupo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "devedor_id")
    private GrupoParticipante devedor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recebedor_id")
    private GrupoParticipante recebedor;

    @Column(nullable = false, precision = 28, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false, length = 15)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registrado_por_id")
    private Usuario registradoPor;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "cancelado_em")
    private LocalDateTime canceladoEm;

    @Column(nullable = false)
    private long versao;

    protected Acerto() {}

    public Acerto(
            Grupo grupo,
            GrupoParticipante devedor,
            GrupoParticipante recebedor,
            BigDecimal valor,
            Usuario registradoPor) {
        this.grupo = grupo;
        this.devedor = devedor;
        this.recebedor = recebedor;
        this.valor = valor.setScale(2, java.math.RoundingMode.UNNECESSARY);
        this.registradoPor = registradoPor;
        status = "PAGO";
        criadoEm = LocalDateTime.now();
    }

    public void cancelar() {
        status = "CANCELADO";
        canceladoEm = LocalDateTime.now();
        versao++;
    }

    public Long getId() {
        return id;
    }

    public GrupoParticipante getDevedor() {
        return devedor;
    }

    public GrupoParticipante getRecebedor() {
        return recebedor;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public String getStatus() {
        return status;
    }

    public Usuario getRegistradoPor() {
        return registradoPor;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getCanceladoEm() {
        return canceladoEm;
    }

    public long getVersao() {
        return versao;
    }
}
