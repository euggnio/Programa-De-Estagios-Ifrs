package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.ImplClasses.HistoricoSolicitacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.SolicitarEstagio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CoordenadorHandler implements ServidorHandler{

    private SolicitarEstagio solicitacao;

    @Autowired
    private HistoricoSolicitacao historicoSolicitacao;


    public CoordenadorHandler() {
    }

    public void setSolicitacao(SolicitarEstagio solicitacao) {
        this.solicitacao = solicitacao;
    }

    public void deferir() {
        if(solicitacao.isRelatorioEntregue()){
            historicoSolicitacao.mudarSolicitacao(solicitacao, "Relatório final foi aprovado pelo coordenador");
        }
        if(solicitacao.isCancelamento()){
            historicoSolicitacao.mudarSolicitacao(solicitacao, "Pedido de cancelamento foi aprovado pelo coordenador");
            historicoSolicitacao.mudarSolicitacao(solicitacao, "Etapa foi modificada de " + solicitacao.getEtapaAtualComoString() + " para " + solicitacao.verificarEtapaComoString("2"));
        }
        iniciarDeferimento();
    }

    private void iniciarDeferimento() {
        solicitacao.setAprovado();
        solicitacao.setStatusEtapaCoordenador("Aprovado");
        if(solicitacao.isCancelamento()){
            solicitacao.setEtapa("2");
            solicitacao.setStatus("Em análise");
        }else{
            solicitacao.setEtapa("5");
        }
        if(solicitacao.isRelatorioEntregue()){
            solicitacao.setStatus("Finalizado");
        }
    }
}
