package br.com.rachai.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "grupos")
public class Grupo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_responsavel_id", nullable = false)
    private Usuario responsavel;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    protected Grupo() {}

    public Grupo(String nome, String descricao, Usuario responsavel) {
        this.nome = nome;
        this.descricao = descricao;
        this.responsavel = responsavel;
        this.criadoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public Usuario getResponsavel() {
        return responsavel;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
