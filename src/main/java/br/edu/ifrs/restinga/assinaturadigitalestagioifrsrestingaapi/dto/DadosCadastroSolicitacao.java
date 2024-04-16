package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

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
        String nomeEmpresa,
        @NotBlank
        Boolean ePrivada,
        @NotBlank
        String contatoEmpresa,
        @NotBlank
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
