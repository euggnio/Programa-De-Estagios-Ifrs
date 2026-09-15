package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.strategy.tipos;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.SolicitarEstagio;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.strategy.EmailStrategy;

public class NotificacaoEtapaEmailStrategy implements EmailStrategy {

    private SolicitarEstagio solicitacao;
    private final String frontendUrl;
    private final String institutionSignature;
    private final String emailSubject;

    public NotificacaoEtapaEmailStrategy(String frontendUrl, String institutionSignature, String emailSubject) {
        this.frontendUrl = frontendUrl;
        this.institutionSignature = institutionSignature;
        this.emailSubject = emailSubject;
    }

    @Override
    public void setSolicitacao(SolicitarEstagio solicitacao) {
        this.solicitacao = solicitacao;
    }
    @Override
    public String getTitle() {
        return emailSubject;
    }

    @Override
    public String getBody() {
        return "Olá!\n" +
                "<span>Uma nova documentação de estágio está pronta para ser analisada em sua etapa.</span><br>\n" +
                "<span>Acesse o link a seguir para visualizar: <b>" + frontendUrl + "/login/" + this.solicitacao.getId() + "?servidor=true</b></span>\n" +
                "<p>Atenciosamente, <br>\n" +
                institutionSignature + "</p>";
    }
}
