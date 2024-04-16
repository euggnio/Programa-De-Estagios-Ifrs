package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Curso;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Usuario;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record DadosAtualizacaoAluno(


    Long id,
    @NotBlank
    @Length(min = 5, max = 100)
    String nomeCompleto,
    Usuario usuarioSistema,
    String turno,
    @NotBlank
    @Length(min = 5, max = 11)
    String matricula,

    Long curso

) {
    
}
