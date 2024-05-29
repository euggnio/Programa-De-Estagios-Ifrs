package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
public record DadosCadastroSolicitacao(
        @NotBlank
        String tipo,
        @NotBlank
        long alunoId,
        @NotBlank
        long cursoId,
        @NotBlank
        String titulo,
        @NotBlank
        @Length(min = 3, max = 48)
        String nomeEmpresa,
        @NotNull
        Boolean ePrivada,
        @NotBlank
        @Length(min = 11, max = 11)
        String contatoEmpresa,
        @NotBlank
        @Length(min = 1, max = 25)
        String agente,

        @Nullable
        String conteudo,
        @Nullable
        String observacao,
        @NotBlank
        LocalDate finalDataEstagio,
        @NotBlank
        LocalDate inicioDataEstagio,
        @Nullable
        String status,
        @Nullable
        String etapa,
        @Nullable
        String respostas,

        @NotBlank
        String cargaHoraria,
        @NotBlank
        String salario,
        @NotBlank
        String turnoEstagio


) {
}
