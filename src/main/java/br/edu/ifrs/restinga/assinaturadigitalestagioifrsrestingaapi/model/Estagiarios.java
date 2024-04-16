package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity(name = "Estagiarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Estagiarios {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private SolicitarEstagio solicitacao;
    private String urlPastaDocumentos;
    private boolean ativo;

    public Estagiarios(SolicitarEstagio solicitacao, String urlPastaDocumentos){
        this.solicitacao = solicitacao;
        this.urlPastaDocumentos = "https://drive.google.com/drive/u/0/folders/" + urlPastaDocumentos;
        this.ativo = true;
    }


}
