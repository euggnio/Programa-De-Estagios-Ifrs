package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.strategy.tipos;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.SolicitarEstagio;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.strategy.EmailStrategy;

public class NotificacaoEtapaEmailStrategy implements EmailStrategy {

    private SolicitarEstagio solicitacao;

    @Override
    public void setSolicitacao(SolicitarEstagio solicitacao) {
        this.solicitacao = solicitacao;
    }
    @Override
    public String getTitle() {
        return "Documentação de estágio";
    }

    @Override
    public String getBody() {
        return "Olá!\n" +
                "<span>Uma nova documentação de estágio está pronta para ser analisada em sua etapa.</span><br>\n" +
                "<span>Acesse o link a seguir para visualizar: <b>http://localhost:4200/login/" + this.solicitacao.getId() + "?servidor=true</b></span>\n" +
                "<p>Atenciosamente, <br>\n" +
                "Equipe de estágios do IFRS</p>";
    }
}
