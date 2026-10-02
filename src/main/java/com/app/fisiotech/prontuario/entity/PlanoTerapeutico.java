package com.app.fisiotech.prontuario.entity;

import com.app.fisiotech.paciente.entity.Paciente;
import com.app.fisiotech.profissional.entity.Profissional;
import jakarta.persistence.*;
import lombok.Getter;
import java.time.*;

@Entity @Table(name="planos_terapeuticos") @Getter
public class PlanoTerapeutico {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="paciente_id") private Paciente paciente;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="profissional_id") private Profissional profissional;
    @Column(nullable=false) private Integer revisao;
    @Column(nullable=false,length=4000) private String objetivos;
    @Column(nullable=false,length=4000) private String condutas;
    @Column(name="data_inicio",nullable=false) private LocalDate dataInicio;
    @Column(name="data_fim_prevista") private LocalDate dataFimPrevista;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private StatusPlano status;
    @Column(name="visivel_paciente",nullable=false) private boolean visivelPaciente;
    @Column(name="data_criacao",nullable=false,updatable=false) private LocalDateTime dataCriacao;
    protected PlanoTerapeutico() {}
    public PlanoTerapeutico(Paciente p, Profissional prof, int revisao, String objetivos, String condutas, LocalDate inicio, LocalDate fim, boolean visivel) {
        paciente=p; profissional=prof; this.revisao=revisao; this.objetivos=objetivos; this.condutas=condutas;
        dataInicio=inicio; dataFimPrevista=fim; visivelPaciente=visivel; status=StatusPlano.ATIVO; dataCriacao=LocalDateTime.now();
    }
    public void substituir() { status=StatusPlano.SUBSTITUIDO; }
    public void encerrar() { status=StatusPlano.ENCERRADO; }
}
