package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.ImplClasses.HistoricoSolicitacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.SolicitarEstagio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
@Component
public class DiretorHandler {

    @Autowired
    private HistoricoSolicitacao historicoSolicitacao;

    private SolicitarEstagio solicitacao;

    public void setSolicitacao(SolicitarEstagio solicitacao) {
        this.solicitacao = solicitacao;
    }

    public void deferir() {
        if(solicitacao.isCancelamento()){
            historicoSolicitacao.mudarSolicitacao(solicitacao, "Pedido de cancelamento foi aprovado pelo diretor");
            historicoSolicitacao.mudarSolicitacao(solicitacao, "Etapa foi modificada de " + solicitacao.getEtapaAtualComoString() + " para " + solicitacao.verificarEtapaComoString("2"));
        }
        iniciarDeferimento();
    }

    private void iniciarDeferimento() {
        System.out.println("Diretor deferindo");
        solicitacao.setStatusEtapaDiretor("Aprovado");
        solicitacao.setAprovado();
        if(solicitacao.isCancelamento()){
            solicitacao.setEtapa("2");
            solicitacao.setStatus("Em análise");
        }else{
            solicitacao.setEtapa("5");
        }

    }


}
