package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.strategy.tipos;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.SolicitarEstagio;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.strategy.EmailStrategy;

public class ObservacaoEmailStrategy implements EmailStrategy {

    private SolicitarEstagio solicitacao;
    private final String frontendUrl;
    private final String institutionSignature;
    private final String emailSubject;

    public ObservacaoEmailStrategy(String frontendUrl, String institutionSignature, String emailSubject) {
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
                    <p>Para solucionar, acesse o sistema: <a href="%s/login/">%s/login/</a></p>
                    <p>Bons estudos!</p>
                    <p>Atenciosamente, <br>
                        %s.</p>
                </body>
            </html>
        """
                .formatted(
                        this.solicitacao.getAluno().getNomeCompleto(),
                        this.solicitacao.getTipo(),
                        this.solicitacao.getObservacao(),
                        frontendUrl,
                        frontendUrl,
                        institutionSignature
                );



    }
}
