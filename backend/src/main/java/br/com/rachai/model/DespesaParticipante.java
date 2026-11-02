package br.com.rachai.model;

import jakarta.persistence.*;

@Entity
@Table(
        name = "despesa_participantes",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_despesa_participante",
                        columnNames = {"despesa_id", "participante_id"}))
public class DespesaParticipante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "despesa_id", nullable = false)
    private Despesa despesa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participante_id", nullable = false)
    private GrupoParticipante participante;

    @Column(name = "grupo_id", nullable = false)
    private Long grupoId;

    @Column(name = "valor_parte", precision = 12, scale = 2)
    private java.math.BigDecimal valorParte;

    public java.math.BigDecimal getValorParte() {
        return valorParte;
    }

    public void definirValorParte(java.math.BigDecimal valor) {
        valorParte = valor;
    }

    protected DespesaParticipante() {}

    public DespesaParticipante(Despesa despesa, GrupoParticipante participante) {
        this.despesa = despesa;
        this.participante = participante;
        this.grupoId = despesa.getGrupo().getId();
    }

    public GrupoParticipante getParticipante() {
        return participante;
    }
}
