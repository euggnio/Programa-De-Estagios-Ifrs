package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.strategy;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.SolicitarEstagio;

public interface EmailStrategy {


    void setSolicitacao(SolicitarEstagio solicitacao);


    String getTitle();

    String  getBody();
}