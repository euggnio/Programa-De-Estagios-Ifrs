package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;

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
        @Nullable String contatoEmpresa,
        @Nullable String agente,
        @Nullable String nomeEmpresa,
        @Nullable String statusEtapaDiretor,
        @Nullable Boolean ePrivada,
        @Nullable String observacao) {


        public DadosAtualizacaoSolicitacao(@Nullable Long id, @Nullable String status, @Nullable
        String etapa, @Nullable
                                           String statusEtapaSetorEstagio, @Nullable
                                           String statusEtapaCoordenador, @Nullable
                                           boolean editavel, @Nullable String contatoEmpresa, @Nullable String agente, @Nullable String nomeEmpresa, @Nullable String statusEtapaDiretor, @Nullable Boolean ePrivada, @Nullable String observacao) {
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
                this.ePrivada = ePrivada;
                this.observacao = observacao;
        }

        public DadosAtualizacaoSolicitacao(@Nullable Long id, @Nullable String status, @Nullable String etapa, @Nullable String statusEtapaSetorEstagio, @Nullable String statusEtapaCoordenador, boolean editavel, @Nullable String statusEtapaDiretor, @Nullable String observacao) {
                this(id, status, etapa, statusEtapaSetorEstagio, statusEtapaCoordenador, editavel, null, null, null, statusEtapaDiretor, null, observacao);
        }
}