package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.ImplClasses.HistoricoSolicitacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.SolicitarEstagio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SetorEstagiosHandler implements ServidorHandler {

    @Autowired
    private HistoricoSolicitacao historicoSolicitacao;

    @Value("${status.aprovado}")
    private String statusAprovado;

    @Value("${status.cancelado}")
    private String statusCancelado;

    private SolicitarEstagio solicitacao;

    public void setSolicitacao(SolicitarEstagio solicitacao) {
        this.solicitacao = solicitacao;
    }

    public void deferir() {
        if(solicitacao.isCancelamento()){
            historicoSolicitacao.mudarSolicitacao(solicitacao, "Solicitação de cancelamento foi aprovada");
        }
        iniciarDeferimento();
    }

    private void iniciarDeferimento() {
        solicitacao.setStatusSetorEstagio(statusAprovado);
        solicitacao.setAprovado();
        solicitacao.setEtapa("5");
        if(solicitacao.isCancelamento()){
            solicitacao.setStatus(statusCancelado);
        }
    }
}
