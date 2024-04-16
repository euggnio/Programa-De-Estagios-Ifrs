package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.strategy;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.SolicitarEstagio;

public class DocumentosAssinadosEmailStrategy implements EmailStrategy{
    @Override
    public String getTitle() {
        return "Documentos Assinados!!";
    }

    @Override
    public String getBody(SolicitarEstagio solicitacao, String link) {
        return """
            <html>
                <body style='font-family: Arial, sans-serif;'>
                <h2 style='color: #3498db;'> Documentos Assinados! </h2>
                    <p>Olá""" + " " + solicitacao.getAluno().getNomeCompleto() + ",</p>" + """
            <p>Os documentos da sua solicitação foram assinados!!!</p>
            <p style='background-color: #ecf0f1; padding: 10px; font-size: 18px; font-weight: bold;'>"""
                + " https://drive.google.com/drive/u/0/folders/" + link + "</p>" + """
                        <p>Desejamos um  bom estágio!!!.</p>
                    </body>
                </html>
            """;
    }

}
