package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
    public class DocumentoDto {

    private Boolean assinado;
    @Getter
    private Long id;
    @Getter
    private String nome;
    private boolean paraDiretor;

    public DocumentoDto() {
    }

}



