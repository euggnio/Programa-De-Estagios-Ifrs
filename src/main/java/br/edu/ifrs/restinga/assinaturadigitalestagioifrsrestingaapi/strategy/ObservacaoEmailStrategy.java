package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.strategy;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.SolicitarEstagio;

public class ObservacaoEmailStrategy implements EmailStrategy {

    @Override
    public String getTitle() {
        return "Observação adicionada";
    }

    @Override
    public String getBody(SolicitarEstagio solicitacao, String observacao) {
                return """
            <html>
                <body style="font-family: Arial, sans-serif;">
                    <h2 style="color: #e74c3c;">Observação adicionada!</h2>
                    <p>Olá %s,</p>
                    <p style="background-color: #ecf0f1; padding: 10px; font-size: 14px;">
                        Sua solicitação do tipo  '%s' recebeu uma observação:
                        <br>
                        <span style="font-weight: bold;">%s</span>
                    </p>
                    <p>Para solucionar, acesse o sistema: <a href="http://localhost:4200/login/">http://localhost:4200/login/</a></p>
                    <p>Bons estudos!!!</p>
                </body>
            </html>
        """
                .formatted(
                        solicitacao.getAluno().getNomeCompleto(),
                        solicitacao.getTipo(),
                        observacao
                );



    }
}
