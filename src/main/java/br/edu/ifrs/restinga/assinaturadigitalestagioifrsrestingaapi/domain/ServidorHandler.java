package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.SolicitarEstagio;

public interface ServidorHandler {


    void deferir();

    private void iniciarDeferimento() {

    }

    void setSolicitacao(SolicitarEstagio solicitacao);
}
