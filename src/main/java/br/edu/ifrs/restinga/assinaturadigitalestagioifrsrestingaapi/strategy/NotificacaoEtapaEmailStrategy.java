package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.strategy;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.SolicitarEstagio;

public class NotificacaoEtapaEmailStrategy implements EmailStrategy{


    @Override
    public String getTitle() {
        return "Documentação de estágio";
    }

    @Override
    public String getBody(SolicitarEstagio solicitacao, String id) {
        return "Olá!\n" +
                "<span>Uma nova documentação de estágio está pronta para ser analisada em sua etapa.</span><br>\n" +
                "<span>Acesse o link a seguir para visualizar: <b>http://localhost:4200/login/" + id + "?servidor=true</b></span>\n" +
                "<p>Atenciosamente, <br>\n" +
                "Equipe de estágios do IFRS</p>";
    }
}
