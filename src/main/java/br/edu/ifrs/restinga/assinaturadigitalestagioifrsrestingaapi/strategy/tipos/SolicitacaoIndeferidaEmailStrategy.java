package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.strategy.tipos;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.SolicitarEstagio;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.strategy.EmailStrategy;

public class SolicitacaoIndeferidaEmailStrategy implements EmailStrategy {

    private SolicitarEstagio solicitacao;

    @Override
    public void setSolicitacao(SolicitarEstagio solicitacao) {
        this.solicitacao = solicitacao;
    }

    @Override
    public String getTitle() {
        return "Solicitação indeferida!";
    }

    @Override
    public String getBody() {
        return """
            <html>
                <body style='font-family: Arial, sans-serif;'>
                    <h2 style='color: #3498db;'> Solicitação de assinatura de documentos foi indeferida! </h2>
                    <p>Olá""" + " " + this.solicitacao.getAluno().getNomeCompleto() + ",</p>" + """
                    <p>A sua solicitação foi indeferida no sistema, abaixo segue o motivo do indeferimento:</p>
                    <p style='background-color: #ecf0f1; padding: 10px; font-size: 18px; font-weight: bold;'>"""
                    +  this.solicitacao.getObservacao() + "</p>" + """
                    <br>
                    <br>
                    <p>Atenciosamente, <br>
                    Equipe de estágios do IFRS RESTINGA.</p>
                </body>
                </html>
            """;
    }
}
