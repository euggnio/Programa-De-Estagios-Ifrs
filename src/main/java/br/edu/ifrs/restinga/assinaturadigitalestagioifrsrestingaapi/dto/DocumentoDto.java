package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
    public class DocumentoDto {

        private Boolean assinado;
        private Long id;
        private String nome;
        private boolean paraDiretor;
        // Construtores, getters e setters
        public DocumentoDto() {
        }
        public DocumentoDto(Boolean assinado, Long id, String nome, boolean paraDiretor) {
            this.assinado = assinado;
            this.id = id;
            this.nome = nome;
            this.paraDiretor=  paraDiretor;
        }



        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }
    }

