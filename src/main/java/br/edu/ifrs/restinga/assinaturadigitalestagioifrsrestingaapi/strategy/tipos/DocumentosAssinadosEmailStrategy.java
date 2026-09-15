package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.strategy.tipos;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.SolicitarEstagio;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.strategy.EmailStrategy;

public class DocumentosAssinadosEmailStrategy implements EmailStrategy {

    private SolicitarEstagio solicitacao;
    private String link;
    private final String googleDriveBaseUrl;
    private final String emailSubject;

    public DocumentosAssinadosEmailStrategy(String linkDocs, String googleDriveBaseUrl, String emailSubject) {
        this.link = linkDocs;
        this.googleDriveBaseUrl = googleDriveBaseUrl;
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
        return """
            <html>
                <body style='font-family: Arial, sans-serif;'>
                <h2 style='color: #3498db;'> Documentos Assinados! </h2>
                    <p>Olá""" + " " + this.solicitacao.getAluno().getNomeCompleto() + ",</p>" + """
             <p>Os documentos da sua solicitação foram assinados!!!</p>
            <p style='background-color: #ecf0f1; padding: 10px; font-size: 18px; font-weight: bold;'>"""
                + googleDriveBaseUrl + this.link + "</p>" + """
                        <p>Desejamos um  bom estágio!!!.</p>
                    </body>
                </html>
            """;
    }

}
