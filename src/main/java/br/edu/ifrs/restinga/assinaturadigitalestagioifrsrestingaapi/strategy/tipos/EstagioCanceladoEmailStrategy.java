package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.strategy.tipos;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.SolicitarEstagio;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.strategy.EmailStrategy;

public class EstagioCanceladoEmailStrategy implements EmailStrategy {
    private SolicitarEstagio solicitacao;

    @Override
    public void setSolicitacao(SolicitarEstagio solicitacao) {
        this.solicitacao = solicitacao;
    }
    @Override
    public String getTitle() {
        return "Estágio Cancelado!!";
    }

    @Override
    public String getBody() {
        return """
            <html>
                <body style='font-family: Arial, sans-serif;'>
                <h2 style='color: #e74c3c;'> Estágio Cancelado! </h2>
                    <p>Olá""" + " " + this.solicitacao.getAluno().getNomeCompleto() + ",</p>" + """
           <p style='background-color: #ecf0f1; padding: 10px; font-size: 18px; font-weight: bold;'>
           Sua solicitação para cancelamento de estágio foi deferida!</p>
            <p>Bons estudos!.</p>
            <p>Atenciosamente, <br>
                Equipe de estágios do IFRS RESTINGA.</p>
                    </body>
                </html>
            """;
    }
}