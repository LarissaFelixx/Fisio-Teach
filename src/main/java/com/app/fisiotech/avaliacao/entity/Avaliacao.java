package com.app.fisiotech.avaliacao.entity;

import com.app.fisiotech.consulta.entity.Consulta;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@Table(
        name = "avaliacoes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_avaliacao_consulta", columnNames = "consulta_id")
        }
)
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "consulta_id", nullable = false)
    private Consulta consulta;

    @Column(name = "nota", nullable = false)
    private Integer nota;

    @Column(name = "comentario", length = 1000)
    private String comentario;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    protected Avaliacao() {
    }

    public Avaliacao(Consulta consulta, Integer nota, String comentario) {
        this.consulta = consulta;
        this.nota = nota;
        this.comentario = comentario;
    }

    @PrePersist
    public void prePersist() {
        this.dataCriacao = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Avaliacao avaliacao)) return false;
        return Objects.equals(id, avaliacao.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
