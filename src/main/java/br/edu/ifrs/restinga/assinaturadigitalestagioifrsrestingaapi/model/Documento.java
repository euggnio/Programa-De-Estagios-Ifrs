package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.sql.Blob;

@Entity(name = "documento")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private SolicitarEstagio solicitarEstagio;
    @Column
    private String nome;
    @Column
    private boolean assinado;
    @Column
    private boolean paraDiretor;
    @Lob
    private Blob documento;


}
