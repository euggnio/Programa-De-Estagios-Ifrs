package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.strategy;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.file.GoogleEmail;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.SolicitarEstagio;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.strategy.tipos.*;

public class EmailProcessar {

    private SolicitarEstagio solicitacao;
    private EmailStrategy emailStrategy;
    private String emailPara;
    private boolean setedStrategy = false;


    public EmailProcessar(String emailPara,SolicitarEstagio solicitacao) {
        this.emailPara = emailPara;
        this.solicitacao = solicitacao;
    }

    public EmailProcessar(SolicitarEstagio solicitacao) {
        this.solicitacao = solicitacao;
        this.emailPara = solicitacao.getAluno().getUsuarioSistema().getEmail();
    }

    public void enviarEmailIndeferimento(){
        this.setedStrategy = true;
        this.emailStrategy = new SolicitacaoIndeferidaEmailStrategy();
        this.emailPara = solicitacao.getAluno().getUsuarioSistema().getEmail();
        enviar();
        System.out.println("Email de indeferimento enviado");
    }

    public void enviarEmailObservacao(){
        this.setedStrategy = true;
        this.emailStrategy = new ObservacaoEmailStrategy();
        this.emailPara = solicitacao.getAluno().getUsuarioSistema().getEmail();
        enviar();
        System.out.println("Email de observação enviado");
    }

    public void enviarEmailNotificacaoEtapa(){
        this.setedStrategy = true;
        this.emailStrategy = new NotificacaoEtapaEmailStrategy();
        enviar();
        System.out.println("Email de notificação de etapa enviado");
    }

    public void enviarEmailDocsAssinadosComLink(String link){
        this.setedStrategy = true;
        this.emailStrategy = new DocumentosAssinadosEmailStrategy(link);
        enviar();
        System.out.println("Email de notificação de assinatura enviado");
    }

    public void enviarRelatorioEntregue(){
        this.setedStrategy = true;
        this.emailStrategy = new RelatorioEntregueEmailStrategy();
        enviar();
        System.out.println("Email de notificação de relatório entregue enviado");
    }

    public void enviarEmailCancelamento(){
        this.setedStrategy = true;
        this.emailStrategy = new EstagioCanceladoEmailStrategy();
        enviar();
        System.out.println("Email de cancelamento enviado");
    }

    private void enviar() {
        if (!setedStrategy) {
            throw new RuntimeException("Estratégia de email não definida");
        }
        this.emailStrategy.setSolicitacao(solicitacao);
        try {
            GoogleEmail.sendMail(emailPara
                    ,emailStrategy.getTitle()
                    ,emailStrategy.getTitle()
                    ,emailStrategy.getBody());
        } catch (Exception e) {
            //TODO: Tratar exceção! email não enviado ocorreu
            throw new RuntimeException(e);
        }
    }

}
