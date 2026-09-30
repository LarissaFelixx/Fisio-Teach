package com.app.fisiotech.paciente.entity;

import com.app.fisiotech.profissional.entity.Profissional;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@Table(
        name = "pacientes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_paciente_email", columnNames = "email")
        }
)
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Column(name = "email", nullable = false, length = 120)
    private String email;

    @Column(name = "senha", nullable = false, length = 255)
    private String senha;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    // Nullable: paciente pode se autocadastrar sem medico ainda (fica vinculado
    // quando marcar a primeira consulta).
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "profissional_id", nullable = true)
    private Profissional profissional;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(name = "sexo", length = 30)
    private String sexo;

    @Column(name = "profissao", length = 120)
    private String profissao;

    @Column(name = "telefone", length = 30)
    private String telefone;

    @Column(name = "endereco", length = 200)
    private String endereco;

    @Column(name = "bairro", length = 120)
    private String bairro;

    @Column(name = "foto")
    private String foto;

    protected Paciente() {
    }

    public Paciente(String nome, String email, String senha, Profissional profissional) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.profissional = profissional;
    }

    @PrePersist
    public void prePersist() {
        this.dataCriacao = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Paciente paciente)) return false;
        return Objects.equals(id, paciente.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
