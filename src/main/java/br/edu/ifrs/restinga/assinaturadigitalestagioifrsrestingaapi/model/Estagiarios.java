package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
    @ManyToOne(cascade = CascadeType.ALL)
    private SolicitarEstagio solicitacao;
    private String urlPastaDocumentos;
    private boolean ativo;

    private static String googleDriveBaseUrl;

    @Value("${google.drive.base-url}")
    public void setGoogleDriveBaseUrlStatic(String url) {
        Estagiarios.googleDriveBaseUrl = url;
    }

    public Estagiarios(SolicitarEstagio solicitacao, String urlPastaDocumentos){
        this.solicitacao = solicitacao;
        this.urlPastaDocumentos = googleDriveBaseUrl + urlPastaDocumentos;
        this.ativo = true;
    }


}
