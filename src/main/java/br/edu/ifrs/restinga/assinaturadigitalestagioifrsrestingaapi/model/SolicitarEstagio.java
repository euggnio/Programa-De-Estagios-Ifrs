package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.validator.constraints.Length;

@Entity(name = "SolicitarEstagio")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitarEstagio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String tipo;

    @OneToMany(mappedBy = "solicitarEstagio", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private List<Documento> documento = new ArrayList<>();

    @OneToOne
    private Aluno aluno;

    @Length(max = 25)
    private String status;

    private String statusEtapaCoordenador;
    private String statusSetorEstagio;
    private String statusEtapaDiretor;

    @OneToOne
    Curso curso;

    @OneToMany(mappedBy = "solicitarEstagio", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonManagedReference
    private List<Historico> historico = new ArrayList<>();

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime dataSolicitacao;

    @Column(columnDefinition = "DATETIME")
    private LocalDate finalDataEstagio;

    @Column(columnDefinition = "DATETIME")
    private LocalDate inicioDataEstagio;

    private String nomeEmpresa;

    private Boolean ePrivada;

    private String contatoEmpresa;

    private String agente;

    @Length(max = 2)
    private String etapa;

    private boolean editavel;

    private String observacao;

    private String resposta;

    private Long idReferente;

    private String cargaHoraria;

    private String salario;

    private String turnoEstagio;

    private boolean relatorioEntregue;
    public SolicitarEstagio(LocalDate finalDataEstagio,LocalDate inicioDataEstagio, Aluno aluno, Curso curso
            , String tipo,  String nomeEmpresa,Boolean ePrivada,String contatoEmpresa, String agente
            , String observacao, String status, String etapa, boolean editavel
            , String cargaHoraria, String salario, String turnoEstagio) {
        this.finalDataEstagio = finalDataEstagio;
        this.inicioDataEstagio = inicioDataEstagio;
        this.aluno = aluno;
        this.curso = curso;
        this.tipo = tipo;
        this.dataSolicitacao = LocalDateTime.now();
        this.status = status;
        this.etapa = etapa;
        this.observacao = observacao;
        this.nomeEmpresa = nomeEmpresa;
        this.ePrivada = ePrivada;
        this.contatoEmpresa = contatoEmpresa;
        this.agente = agente;
        this.editavel = editavel;
        this.cargaHoraria = cargaHoraria;
        this.salario = salario;
        this.turnoEstagio = turnoEstagio;
        this.statusEtapaCoordenador = "";
        this.statusSetorEstagio = "";
        this.statusEtapaDiretor = "";
        this.relatorioEntregue = false;
    }

    public String getEtapaAtualComoString(){
        switch (this.etapa) {
            case "1" -> {
                return "Aluno";
            }
            case "2" -> {
                return "Setor de estágio";
            }
            case "3" -> {
                return "Coordenador";
            }
            case "4" -> {
                return "Diretor";
            }
            default -> {
                return "Sistema";
            }
        }
    }

    public String verificarEtapaComoString(String etapa){
        switch (etapa) {
            case "1" -> {
                return "aluno";
            }
            case "2" -> {
                return "Setor de estágio";
            }
            case "3" -> {
                return "Coordenador";
            }
            case "4" -> {
                return "Diretor";
            }
            default -> {
                return "Erro";
            }
        }

    }

}
