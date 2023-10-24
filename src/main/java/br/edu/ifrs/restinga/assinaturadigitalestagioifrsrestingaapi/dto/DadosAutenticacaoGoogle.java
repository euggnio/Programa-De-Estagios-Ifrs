package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto;

import jakarta.validation.constraints.NotBlank;

public record DadosAutenticacaoGoogle (
        @NotBlank
        String email,
        @NotBlank
        String email_verified,
        @NotBlank
        String family_name,
        @NotBlank
        String given_name,
        @NotBlank
        String name,
        @NotBlank
        String sub
){

}
