package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
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
        String nomeEmpresa,
        Boolean ePrivada,

        String contatoEmpresa,
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
        String respostas

) {
}
