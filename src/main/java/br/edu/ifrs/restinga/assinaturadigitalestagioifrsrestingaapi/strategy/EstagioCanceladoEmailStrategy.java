package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.strategy;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.SolicitarEstagio;

public class EstagioCanceladoEmailStrategy implements EmailStrategy {

    @Override
    public String getTitle() {
        return "Estágio Cancelado!!";
    }

    @Override
    public String getBody(SolicitarEstagio solicitacao, String link) {
        return """
            <html>
                <body style='font-family: Arial, sans-serif;'>
                <h2 style='color: #e74c3c;'> Estágio Cancelado! </h2>
                    <p>Olá""" + " " + solicitacao.getAluno().getNomeCompleto() + ",</p>" + """
           <p style='background-color: #ecf0f1; padding: 10px; font-size: 18px; font-weight: bold;'>
           Sua solicitação para cancelamento de estágio foi deferida!</p>
            <p>Bons estudos!!!.</p>
                    </body>
                </html>
            """;
    }
}