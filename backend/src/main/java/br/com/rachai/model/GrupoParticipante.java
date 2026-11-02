package br.com.rachai.model;

import jakarta.persistence.*;

import java.util.Locale;

@Entity
@Table(
        name = "grupo_participantes",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_participante_usuario",
                        columnNames = {"grupo_id", "usuario_id"}))
public class GrupoParticipante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grupo_id", nullable = false)
    private Grupo grupo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(name = "nome_normalizado", nullable = false, length = 360)
    private String nomeNormalizado;

    protected GrupoParticipante() {}

    public GrupoParticipante(Grupo grupo, Usuario usuario) {
        this.grupo = grupo;
        this.usuario = usuario;
        this.nome = limparNome(usuario.getNome());
        this.nomeNormalizado = normalizarNome(nome);
    }

    public static String limparNome(String nome) {
        return nome.strip().replaceAll("(?U)\\s+", " ");
    }

    public static String normalizarNome(String nome) {
        return limparNome(nome).toLowerCase(Locale.ROOT);
    }

    public Long getId() {
        return id;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getNome() {
        return nome;
    }
}
