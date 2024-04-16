package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.strategy;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.SolicitarEstagio;

public interface EmailStrategy {
    String getTitle();
    String  getBody(SolicitarEstagio solicitacao, String link);
}