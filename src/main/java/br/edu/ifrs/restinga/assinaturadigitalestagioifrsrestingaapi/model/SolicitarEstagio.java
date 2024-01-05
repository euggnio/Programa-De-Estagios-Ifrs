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

    private String status;

    private String statusEtapaCoordenador;

    private String statusSetorEstagio;

    private String statusEtapaDiretor;

    @OneToOne Curso curso;

    @OneToMany(mappedBy = "solicitarEstagio", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonManagedReference
    private List<Historico> historico = new ArrayList<>();

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime dataSolicitacao;

    @Column(columnDefinition = "DATETIME")
    private LocalDate finalDataEstagio;

    @Column(columnDefinition = "DATETIME")
    private LocalDate inicioDataEstagio;

    private String titulo;

    private String nomeEmpresa;

    private Boolean ePrivada;

    private String contatoEmpresa;

    private String agente;

    private String conteudo;

    private String etapa;

    private boolean editavel;

    private String observacao;

    private String resposta;

    public SolicitarEstagio(LocalDate finalDataEstagio,LocalDate inicioDataEstagio, Aluno aluno, Curso curso, String tipo, String titulo, String nomeEmpresa,Boolean ePrivada,String contatoEmpresa, String agente, String conteudo, String observacao, String status, String etapa, boolean editavel) {
        this.finalDataEstagio = finalDataEstagio;
        this.inicioDataEstagio = inicioDataEstagio;
        this.aluno = aluno;
        this.curso = curso;
        this.tipo = tipo;
        this.dataSolicitacao = LocalDateTime.now();
        this.status = status;
        this.conteudo = conteudo;
        this.etapa = etapa;
        this.titulo = aluno.getNomeCompleto();
        this.observacao = observacao;
        this.nomeEmpresa = nomeEmpresa;
        this.ePrivada = ePrivada;
        this.contatoEmpresa = contatoEmpresa;
        this.agente = agente;
        this.resposta = resposta;
        this.editavel = editavel;
        this.resposta = "";
        this.statusEtapaCoordenador = "";
        this.statusSetorEstagio = "";
        this.statusEtapaDiretor = "";
    }

}
