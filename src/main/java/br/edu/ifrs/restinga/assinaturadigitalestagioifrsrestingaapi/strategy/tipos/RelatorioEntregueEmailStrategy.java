package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.strategy.tipos;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.SolicitarEstagio;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.strategy.EmailStrategy;

public class RelatorioEntregueEmailStrategy implements EmailStrategy {

    private SolicitarEstagio solicitacao;

    @Override
    public void setSolicitacao(SolicitarEstagio solicitacao) {
        this.solicitacao = solicitacao;
    }
    @Override
    public String getTitle() {
        return "Relatório final visto pelo coordenador!!";
    }

    @Override
    public String getBody() {
        return """
            <html>
                <body style='font-family: Arial, sans-serif;'>
                <h2 style='color: #e74c3c;'> Relatório aprovado! </h2>
                    <p>Olá""" + " " + this.solicitacao.getAluno().getNomeCompleto() + ",</p>" + """
           <p style='background-color: #ecf0f1; padding: 10px; font-size: 18px; font-weight: bold;'>
           Seu relatório final do estagio na empresa:"""+ " '" +this.solicitacao.getNomeEmpresa()+ "' foi visto e aprovado pelo coordenador!</p>" + """
            <br>
            <p>Bons estudos!</p>
            <p>Atenciosamente, <br>
                Equipe de estágios do IFRS RESTINGA.</p>
                    </body>
                </html>
            """;
    }
}