package com.app.fisiotech.consulta.entity;

import com.app.fisiotech.paciente.entity.Paciente;
import com.app.fisiotech.profissional.entity.Profissional;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@Table(name = "consultas")
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoConsulta tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusConsulta status;

    @Column(name = "convenio", length = 60)
    private String convenio;

    @Column(name = "valor", precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "foi_remarcada", nullable = false)
    private boolean foiRemarcada = false;

    @Embedded
    private QuadroClinico quadroClinico = new QuadroClinico();

    @Embedded
    private HabitosVida habitosVida = new HabitosVida();

    @Embedded
    private ExameFisico exameFisico = new ExameFisico();

    @Embedded
    private Diagnostico diagnostico = new Diagnostico();

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    protected Consulta() {
    }

    public Consulta(Paciente paciente, Profissional profissional, LocalDateTime dataHora, TipoConsulta tipo, String convenio, BigDecimal valor) {
        this.paciente = paciente;
        this.profissional = profissional;
        this.dataHora = dataHora;
        this.tipo = tipo;
        this.convenio = convenio;
        this.valor = valor;
        this.status = StatusConsulta.AGENDADA;
    }

    @PrePersist
    public void prePersist() {
        this.dataCriacao = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Consulta consulta)) return false;
        return Objects.equals(id, consulta.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /*
     * Getters manuais para os embeddables: quando todas as colunas de um @Embedded estão
     * NULL no banco, o Hibernate deixa a própria referência null ao recarregar a entidade
     * (o inicializador "= new ...()" só vale para objetos novos em memória). Sem isso, um
     * GET numa consulta cujo Quadro Clínico ainda não foi preenchido lançaria NPE.
     */

    public QuadroClinico getQuadroClinico() {
        if (quadroClinico == null) {
            quadroClinico = new QuadroClinico();
        }
        return quadroClinico;
    }

    public HabitosVida getHabitosVida() {
        if (habitosVida == null) {
            habitosVida = new HabitosVida();
        }
        return habitosVida;
    }

    public ExameFisico getExameFisico() {
        if (exameFisico == null) {
            exameFisico = new ExameFisico();
        }
        return exameFisico;
    }

    public Diagnostico getDiagnostico() {
        if (diagnostico == null) {
            diagnostico = new Diagnostico();
        }
        return diagnostico;
    }
}
