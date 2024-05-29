package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;



@Entity
@AllArgsConstructor
@Data
@NoArgsConstructor
public class HistoricoCurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate data;
    private String acao;
    @ManyToOne
    private Curso curso;
    private String nomeServidor;

    public HistoricoCurso(Curso curso, String nomeServidor, String acao){
        this.data = LocalDate.now();
        this.nomeServidor = nomeServidor;
        this.curso = curso;
        this.acao = acao;
    }

}


