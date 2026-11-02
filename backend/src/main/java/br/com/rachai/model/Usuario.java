package br.com.rachai.model;

import jakarta.persistence.*;

import java.util.Locale;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 254, unique = true)
    private String email;

    @Column(name = "senha_hash", length = 255)
    private String senhaHash;

    protected Usuario() {}

    public Usuario(String nome) {
        this.nome = nome;
    }

    public Usuario(String nome, String email, String senhaHash) {
        this.nome = nome.strip();
        this.email = normalizarEmail(email);
        this.senhaHash = senhaHash;
    }

    public static String normalizarEmail(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }
}
