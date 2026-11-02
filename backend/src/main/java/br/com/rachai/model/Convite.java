package br.com.rachai.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "convites",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_convite_destinatario",
                        columnNames = {"grupo_id", "destinatario_id"}))
public class Convite {
    public enum Status {
        PENDENTE,
        ACEITO,
        RECUSADO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grupo_id", nullable = false)
    private Grupo grupo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private Usuario destinatario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Status status;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "respondido_em")
    private LocalDateTime respondidoEm;

    protected Convite() {}

    public Convite(Grupo grupo, Usuario destinatario) {
        this.grupo = grupo;
        this.destinatario = destinatario;
        reabrir();
    }

    public void reabrir() {
        status = Status.PENDENTE;
        criadoEm = LocalDateTime.now();
        respondidoEm = null;
    }

    public void responder(boolean aceitar) {
        status = aceitar ? Status.ACEITO : Status.RECUSADO;
        respondidoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public Usuario getDestinatario() {
        return destinatario;
    }

    public Status getStatus() {
        return status;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
