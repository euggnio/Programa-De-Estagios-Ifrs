package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.strategy;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.file.GoogleEmail;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.SolicitarEstagio;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.strategy.tipos.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailProcessar {

    private SolicitarEstagio solicitacao;
    private EmailStrategy emailStrategy;
    private String emailPara;
    private boolean setedStrategy = false;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Value("${institution.signature-default}")
    private String institutionSignature;

    @Value("${google.drive.base-url}")
    private String googleDriveBaseUrl;

    @Value("${email.subject.observacao}")
    private String emailSubjectObservacao;

    @Value("${email.subject.notificacao-etapa}")
    private String emailSubjectNotificacaoEtapa;

    @Value("${email.subject.documentos-assinados}")
    private String emailSubjectDocumentosAssinados;

    @Value("${email.subject.solicitacao-indeferida}")
    private String emailSubjectSolicitacaoIndeferida;

    @Value("${email.subject.relatorio-entregue}")
    private String emailSubjectRelatorioEntregue;

    @Value("${email.subject.estagio-cancelado}")
    private String emailSubjectEstagioCancelado;

    public void configurar(String emailPara, SolicitarEstagio solicitacao) {
        this.emailPara = emailPara;
        this.solicitacao = solicitacao;
        this.setedStrategy = false;
    }

    public void configurar(SolicitarEstagio solicitacao) {
        this.solicitacao = solicitacao;
        this.emailPara = solicitacao.getAluno().getUsuarioSistema().getEmail();
        this.setedStrategy = false;
    }

    public void enviarEmailIndeferimento(){
        this.setedStrategy = true;
        this.emailStrategy = new SolicitacaoIndeferidaEmailStrategy(institutionSignature, emailSubjectSolicitacaoIndeferida);
        this.emailPara = solicitacao.getAluno().getUsuarioSistema().getEmail();
        enviar();
        System.out.println("Email de indeferimento enviado");
    }

    public void enviarEmailObservacao(){
        this.setedStrategy = true;
        this.emailStrategy = new ObservacaoEmailStrategy(frontendUrl, institutionSignature, emailSubjectObservacao);
        this.emailPara = solicitacao.getAluno().getUsuarioSistema().getEmail();
        enviar();
        System.out.println("Email de observação enviado");
    }

    public void enviarEmailNotificacaoEtapa(){
        this.setedStrategy = true;
        this.emailStrategy = new NotificacaoEtapaEmailStrategy(frontendUrl, institutionSignature, emailSubjectNotificacaoEtapa);
        enviar();
        System.out.println("Email de notificação de etapa enviado");
    }

    public void enviarEmailDocsAssinadosComLink(String link){
        this.setedStrategy = true;
        this.emailStrategy = new DocumentosAssinadosEmailStrategy(link, googleDriveBaseUrl, emailSubjectDocumentosAssinados);
        enviar();
        System.out.println("Email de notificação de assinatura enviado");
    }

    public void enviarRelatorioEntregue(){
        this.setedStrategy = true;
        this.emailStrategy = new RelatorioEntregueEmailStrategy(institutionSignature, emailSubjectRelatorioEntregue);
        enviar();
        System.out.println("Email de notificação de relatório entregue enviado");
    }

    public void enviarEmailCancelamento(){
        this.setedStrategy = true;
        this.emailStrategy = new EstagioCanceladoEmailStrategy(institutionSignature, emailSubjectEstagioCancelado);
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
