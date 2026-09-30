package com.app.fisiotech.profissional.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@Table(
        name = "profissionais",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_profissional_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_profissional_registro", columnNames = "registro_profissional")
        }
)
public class Profissional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Column(name = "email", nullable = false, length = 120)
    private String email;

    @Column(name = "senha", nullable = false, length = 255)
    private String senha;

    @Column(name = "registro_profissional", nullable = false, length = 20)
    private String registroProfissional;

    @Column(name = "especialidade", nullable = false, length = 120)
    private String especialidade;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "valor_consulta_particular", precision = 10, scale = 2)
    private BigDecimal valorConsultaParticular;

    @ElementCollection
    @CollectionTable(name = "profissional_convenios", joinColumns = @JoinColumn(name = "profissional_id"))
    @Column(name = "convenio")
    private List<String> conveniosAceitos = new ArrayList<>();

    @Column(name = "foto")
    private String foto;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(name = "sexo", length = 30)
    private String sexo;

    @Column(name = "telefone", length = 30)
    private String telefone;

    protected Profissional() {
    }

    public Profissional(String nome, String email, String senha, String registroProfissional, String especialidade) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.registroProfissional = registroProfissional;
        this.especialidade = especialidade;
    }

    @PrePersist
    public void prePersist() {
        this.dataCriacao = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Profissional profissional)) return false;
        return Objects.equals(id, profissional.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
