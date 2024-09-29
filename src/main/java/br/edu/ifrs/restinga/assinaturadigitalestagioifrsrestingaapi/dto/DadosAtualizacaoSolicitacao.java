package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.SolicitarEstagio;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;

public record DadosAtualizacaoSolicitacao(

        @Nullable Long id,
        @Nullable String status,
        @Nullable
        String etapa,
        @Nullable
        String statusEtapaSetorEstagio,
        @Nullable
        String statusEtapaCoordenador,
        @Nullable
        boolean editavel,
        @NotNull @Size(max = 11) String contatoEmpresa,
        @NotEmpty @Size(max = 16)  String agente,
        @NotEmpty @Size(max = 48) String nomeEmpresa,
        @Nullable String statusEtapaDiretor,
        @Nullable Boolean eprivada,
        @Nullable String observacao,
        @NotEmpty
        @Size(max = 3)
        String cargaHoraria,
        @NotEmpty String salario,
        @NotEmpty String turnoEstagio

) {


    public DadosAtualizacaoSolicitacao(@Nullable Long id
            , @Nullable String status
            , @Nullable String etapa
            , @Nullable String statusEtapaSetorEstagio
            , @Nullable String statusEtapaCoordenador
            , @Nullable boolean editavel
            , @Nullable String contatoEmpresa
            , @Nullable String agente
            , @Nullable String nomeEmpresa
            , @Nullable String statusEtapaDiretor
            , @Nullable Boolean eprivada
            , @Nullable String observacao
            , @Nullable String cargaHoraria
            , @Nullable String salario
            , @Nullable String turnoEstagio) {
        this.id = id;
        this.status = status;
        this.etapa = etapa;
        this.statusEtapaSetorEstagio = statusEtapaSetorEstagio;
        this.statusEtapaCoordenador = statusEtapaCoordenador;
        this.editavel = editavel;
        this.contatoEmpresa = contatoEmpresa;
        this.agente = agente;
        this.nomeEmpresa = nomeEmpresa;
        this.statusEtapaDiretor = statusEtapaDiretor;
        this.eprivada = eprivada;
        this.observacao = observacao;
        this.cargaHoraria = cargaHoraria;
        this.salario = salario;
        this.turnoEstagio = turnoEstagio;
    }

    public DadosAtualizacaoSolicitacao(@Nullable Long id
            , @Nullable String status
            , @Nullable String etapa
            , @Nullable String statusEtapaSetorEstagio
            , @Nullable String statusEtapaCoordenador
            , boolean editavel
            , @Nullable String statusEtapaDiretor
            , @Nullable String observacao) {
        this(id, status, etapa, statusEtapaSetorEstagio, statusEtapaCoordenador, editavel, null, null, null, statusEtapaDiretor, null, observacao, null, null, null);
    }

}