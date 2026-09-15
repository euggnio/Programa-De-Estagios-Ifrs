package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.service;


import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.ImplClasses.FileImp;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.ImplClasses.HistoricoSolicitacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.controller.BaseController;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.CoordenadorHandler;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.DiretorHandler;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.SetorEstagiosHandler;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosAtualizacaoSolicitacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosCadastroSolicitacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.*;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.strategy.EmailProcessar;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class SolicitacaoService extends BaseController {

    @Autowired
    FileImp fileImp;

    @Autowired
    private SalvarDocumentoService salvarDocumentoService;

    @Autowired
    private CoordenadorHandler coordenadorHandler;

    @Autowired
    private DiretorHandler diretorHandler;

    @Autowired
    private SetorEstagiosHandler setorEstagiosHandler;

    @Autowired
    private EstagiarioService estagiarioService;

    @Autowired
    private EmailProcessar emailProcessar;

    @Value("${status.nova}")
    private String statusNova;

    @Value("${status.aprovado}")
    private String statusAprovado;

    @Value("${status.indeferido}")
    private String statusIndeferido;

    @Value("${status.cancelado}")
    private String statusCancelado;

    @Value("${status.em-analise}")
    private String statusEmAnalise;

    @Value("${status.processando}")
    private String statusProcessando;

    @Value("${status.finalizado}")
    private String statusFinalizado;

    @Value("${status.respondido}")
    private String statusRespondido;

    @Value("${status.relatorio}")
    private String statusRelatorio;

    @Value("${cargo.coordenador}")
    private String cargoCoordenador;

    @Value("${cargo.diretor}")
    private String cargoDiretor;

    @Value("${role.id.aluno}")
    private long roleIdAluno;

    @Value("${role.id.coordenador}")
    private long roleIdCoordenador;

    @Value("${role.id.setor-estagio}")
    private long roleIdSetorEstagio;

    @Value("${role.id.diretor}")
    private long roleIdDiretor;

    @Value("${curso.id.diretor}")
    private long cursoIdDiretor;


    @Transactional
    public ResponseEntity cadastrarSolicitacao(DadosCadastroSolicitacao dados, List<MultipartFile> arquivos) {
        Optional<Aluno> aluno = alunoRepository.findById(dados.alunoId());
        Optional<Curso> curso = cursoRepository.findById(dados.cursoId());
        if (aluno.isEmpty() || curso.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (verificarSolicitacaoExistente(dados.alunoId(), dados.tipo())) {
            return ResponseEntity.badRequest().body("Você já possui uma solicitação deste tipo em andamento!");
        }
        SolicitarEstagio solicitacao = criarSolicitacao(dados, curso.get(), aluno.get());
        fileImp.SaveDocBlob(arquivos, solicitacao, false);
        solicitacaoRepository.save(solicitacao);
        historicoSolicitacao.mudarSolicitacao(solicitacao, "Cadastrado");
        return ResponseEntity.ok().build();
    }

    private SolicitarEstagio criarSolicitacao(DadosCadastroSolicitacao dados, Curso curso, Aluno aluno) {
        return new SolicitarEstagio(dados.finalDataEstagio(),
                dados.inicioDataEstagio(),
                aluno,
                curso,
                dados.tipo(),
                dados.nomeEmpresa(),
                dados.ePrivada(),
                dados.contatoEmpresa(),
                dados.agente(),
                dados.observacao(),
                statusNova,
                "1",
                true,
                dados.cargaHoraria(),
                dados.salario(),
                dados.turnoEstagio());
    }

    public boolean verificarSolicitacaoExistente(long alunoId, String tipoSolicitacao) {
        List<SolicitarEstagio> quantidade = solicitacaoRepository.findByAluno_Id(alunoId);
        quantidade.removeIf(solicitacao ->
                solicitacao.getStatus().equalsIgnoreCase(statusIndeferido)
                        || solicitacao.getStatus().equalsIgnoreCase(statusAprovado)
                        || solicitacao.getStatus().equalsIgnoreCase(statusCancelado)
                        || solicitacao.getStatus().equalsIgnoreCase(statusFinalizado));
        quantidade.removeIf(solicitacao -> !solicitacao.getTipo().equalsIgnoreCase(tipoSolicitacao));
        return quantidade.size() >= 1;
    }

    public ResponseEntity setProcessando(long id){
        Optional<SolicitarEstagio> solicitacao = solicitacaoRepository.findById(id);
        if(solicitacao.isPresent() && !solicitacao.get().isCancelamento()){
            solicitacao.get().setStatus(statusProcessando);
            solicitacaoRepository.save(solicitacao.get());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    public ResponseEntity modificarObservacao(SolicitarEstagio solicitacao, String observacao, Long  role) {
        solicitacaoRepository.atualizarObservacao(solicitacao.getId(), observacao);
        solicitacao.setObservacao(observacao);
        historicoSolicitacao.salvarHistoricoSolicitacaoId(solicitacao.getId(), role, "Observação para edição: " + observacao);
        try {
            emailProcessar.configurar(solicitacao);
            emailProcessar.enviarEmailObservacao();
        }finally {
            solicitacaoRepository.atualizarObservacao(solicitacao.getId(), observacao);
        }
        return ResponseEntity.ok().build();
    }

    @Transactional
    public ResponseEntity setEditavel(SolicitarEstagio solicitacao, Long role) {
        boolean statusEditavel = solicitacao.isEditavel();
        solicitacao.setEditavel(!statusEditavel);
        if(statusEditavel){
            solicitacao.setObservacao("");
        }
        solicitacaoRepository.save(solicitacao);
        historicoSolicitacao.salvarHistoricoSolicitacaoId(solicitacao.getId(), role, "Edição de documentos foi:  " + (solicitacao.isEditavel() ? "Aberta" : "Fechada"));
        return ResponseEntity.ok().build();
    }

    public List<SolicitarEstagio> obterSolicitacoesDoServidor(Servidor servidor) {
        if (servidor.getCargo().equals(cargoCoordenador)) {
            return obterSolicitacoesParaCoordenador(servidor);
        } else if (servidor.getCargo().equals(cargoDiretor)) {
            return obterSolicitacoesParaDiretor();
        } else {
            return solicitacaoRepository.findAll();
        }
    }
    private List<SolicitarEstagio> obterSolicitacoesParaCoordenador(Servidor coordenador) {
        Optional<Curso> curso = cursoRepository.findById(coordenador.getCurso().getId());
        return solicitacaoRepository.findByCursoAndEtapaIsGreaterThanEqualAndStatusNotContainingIgnoreCase(curso.get(), "3", "Respondido");
    }
    private List<SolicitarEstagio> obterSolicitacoesParaDiretor() {
        return solicitacaoRepository.findAllByEtapaIsGreaterThanEqual("4");
    }


    public ResponseEntity<String> indeferirSolicitacao(long id, Servidor servidor, DadosAtualizacaoSolicitacao dados) {
        Optional<SolicitarEstagio> solicitacaoOptional = solicitacaoRepository.findById(id);
        if(solicitacaoOptional.isPresent()){
            SolicitarEstagio solicitacao = solicitacaoOptional.get();
            if (solicitacao.getEtapa().equals("5")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Está solicitação já foi concluida como deferida ela não pode mais ser indeferida.");
            }
            if(servidor.getRole().getId() == roleIdSetorEstagio){
                solicitacao.setStatusSetorEstagio(statusIndeferido);
            } else if (servidor.getRole().getId() == roleIdCoordenador) {
                solicitacao.setStatusEtapaCoordenador(statusIndeferido);
            }else{
                solicitacao.setStatusEtapaDiretor(statusIndeferido);
            }
            if (dados.observacao() != null) {
                solicitacao.setObservacao(dados.observacao());
            }
            solicitacao.setStatus(statusIndeferido);
            solicitacao.setEditavel(false);
            solicitacaoRepository.save(solicitacao);
            historicoSolicitacao.mudarSolicitacao(solicitacao, statusIndeferido + ", motivo: '" + solicitacao.getObservacao() + "'");

            emailProcessar.configurar(solicitacao);
            emailProcessar.enviarEmailIndeferimento();

            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @Transactional
    public ResponseEntity<String> deferirSolicitacao(SolicitarEstagio solicitacao, Servidor servidor, List<MultipartFile> documentos){
        if(validarDeferimento(solicitacao,servidor.getRole()).equalsIgnoreCase("")){
            switch (servidor.getRole().getId().toString()) {
                case "3" -> deferirSetorEstagio(solicitacao);
                case "2" -> deferirCoordenador(solicitacao);
                case "4" -> deferirDiretor(solicitacao);
            }
            if(documentos != null && !documentos.isEmpty()){
                salvarArquivos(documentos,solicitacao);
            }
            if(!solicitacao.isCancelamento()) {
                historicoSolicitacao.salvarHistoricoSolicitacaoId(solicitacao.getId(), servidor.getRole().getId(), "Solicitação foi deferida");
            }
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(validarDeferimento(solicitacao,servidor.getRole()));
    }

    public void trocarProcessamento(SolicitarEstagio solicitarEstagio){
        if(solicitarEstagio.getStatus().equalsIgnoreCase(statusProcessando)){
            solicitarEstagio.setStatus(statusEmAnalise);
            solicitacaoRepository.save(solicitarEstagio);
        }
    }


    //TODO: Refatorar o email e drive para caso conexão esteja offline
    private void deferirSetorEstagio(SolicitarEstagio solicitacao){
            try{
            emailProcessar.configurar(solicitacao);
            if(solicitacao.isCancelamento()){
                estagiarioService.desativarEstagiario(solicitacao.getId());
                emailProcessar.configurar(solicitacao);
                emailProcessar.enviarEmailCancelamento();
            }
            else {
                GoogleEmaileDrive(solicitacao);
                estagiarioService.salvarEstagiario(solicitacao, salvarDocumentoService.getPastaAluno());
            }
            setorEstagiosHandler.setSolicitacao(solicitacao);
            setorEstagiosHandler.deferir();
            solicitacaoRepository.save(solicitacao);
        }
            catch (Exception e) {
                lidarErroDeferimento(solicitacao);
                throw new RuntimeException(e);
            }
    }

    public void GoogleSalvarDrive(SolicitarEstagio solicitacao) {
        List<Documento> docsParaDrive = documentoRepository.findBySolicitarEstagioId(solicitacao.getId());
        if (solicitacao.isRelatorioEntregue()){
            docsParaDrive.removeIf(documento -> !documento.getNome().contains("RELATORIO"));
        }
        try {
            String nomePasta = solicitacao.getAluno().getNomeCompleto() + " - " + solicitacao.getAluno().getMatricula();
            salvarDocumentoService.salvarDocumentoDeSolicitacao(nomePasta, solicitacao.getCurso().getId() ,docsParaDrive, solicitacao.getAluno().getUsuarioSistema().getEmail());
        } catch (Exception e) {
            this.trocarProcessamento(solicitacao);
            throw new RuntimeException(e);
        }
    }

    public void GoogleEmaileDrive(SolicitarEstagio solicitacao)  {
        List<Documento> docsParaDrive = documentoRepository.findBySolicitarEstagioId(solicitacao.getId());
        emailProcessar.configurar(solicitacao);
        try {
            String nomePasta = solicitacao.getAluno().getNomeCompleto() + " - " + solicitacao.getAluno().getMatricula();
            salvarDocumentoService.salvarDocumentoDeSolicitacao(nomePasta, solicitacao.getCurso().getId(),docsParaDrive, solicitacao.getAluno().getUsuarioSistema().getEmail());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (solicitacao.getTipo().equalsIgnoreCase(statusRelatorio)) {
            docsParaDrive.removeIf(documento -> !documento.getNome().contains("RELATORIO"));
            emailProcessar.enviarRelatorioEntregue();
        }
        else{
            emailProcessar.enviarEmailDocsAssinadosComLink(salvarDocumentoService.getPastaAluno());
        }

    }

    private void lidarErroDeferimento(SolicitarEstagio solicitacao){
        solicitacao.setStatus(statusEmAnalise);
        solicitacao.setEtapa("2");
        solicitacaoRepository.save(solicitacao);
    }

    private void deferirCoordenador(SolicitarEstagio solicitacao) {
            emailProcessar.configurar(solicitacao);
            if (solicitacao.isRelatorioEntregue() || solicitacao.isCancelamento()) {
                if (solicitacao.isRelatorioEntregue()) {
                    emailProcessar.enviarRelatorioEntregue();
                    estagiarioService.desativarEstagiario(solicitacao.getId());
                }
            } else {
                GoogleSalvarDrive(solicitacao);
                emailProcessar.enviarEmailDocsAssinadosComLink(salvarDocumentoService.getPastaAluno());
                estagiariosRepository.save(new Estagiarios(solicitacao, salvarDocumentoService.getPastaAluno()));
            }
            coordenadorHandler.setSolicitacao(solicitacao);
            coordenadorHandler.deferir();
            solicitacaoRepository.save(solicitacao);
    }

    public void deferirDiretor(SolicitarEstagio solicitacao){
        emailProcessar.configurar(solicitacao);
        System.out.println(solicitacao.isCancelamento()  + " SSS3");
        if(!solicitacao.isCancelamento()){
            GoogleEmaileDrive(solicitacao);
            estagiariosRepository.save(new Estagiarios(solicitacao,salvarDocumentoService.getPastaAluno()));
        }
        diretorHandler.setSolicitacao(solicitacao);
        diretorHandler.deferir();
        solicitacaoRepository.save(solicitacao);
    }

    private void salvarArquivos(List<MultipartFile> docs, SolicitarEstagio solicitarEstagio){
        if(docs != null && !docs.isEmpty() ){
            fileImp.SaveDocBlob(docs,solicitarEstagio,true);
        }
    }
    public String validarDeferimento(SolicitarEstagio solicitacao, Role role){
        if(solicitacao.getStatus().equalsIgnoreCase(statusIndeferido)){
            return "Não é possivel deferir uma solicitação que já foi concluída ou deferida";
        }
        else if(solicitacao.getStatus().equalsIgnoreCase(statusAprovado) && solicitacao.getEtapa().equals("5")){
            return "Está solicitação já foi concluida e ela não pode mais ser deferida.";
        }
        else if(solicitacao.getEtapa().equals("2") && !(role.getId() == roleIdSetorEstagio)) {
            return "Apenas o setor de estágios pode deferir uma solicitação na etapa 2.";
        }
        else if (solicitacao.getEtapa().equals("3") && (role.getId() == roleIdDiretor)) {
            return "Apenas o coordenador pode deferir uma solicitação na etapa 3.";
        }
        else if (solicitacao.getEtapa().equals("4") && (role.getId() == roleIdCoordenador )) {
            return "Apenas o diretor pode deferir uma solicitação na etapa 4.";
        }
        else{
            return "";
        }
    }

    public ResponseEntity<String> editarEtapa(long id, String etapa, long role){
        SolicitarEstagio solicitacao = solicitacaoRepository.findById(id).get();
        String emailNovoResponsavel = "";
        if(etapa.equalsIgnoreCase("3")){
            Optional<Servidor> coordenador = servidorRepository.findServidorByCurso_Id(solicitacao.getCurso().getId());
            if(coordenador.isPresent()){
                emailNovoResponsavel = coordenador.get().getUsuarioSistema().getEmail();
            }
            else{
                return ResponseEntity.notFound().build();

            }
        }
        else if(etapa.equalsIgnoreCase("4")){
            Optional<Servidor> diretor = servidorRepository.findServidorByCurso_Id(cursoIdDiretor);
            if (diretor.isPresent()){
                emailNovoResponsavel = diretor.get().getUsuarioSistema().getEmail();
            }
            else{
                return ResponseEntity.notFound().build();
            }
        }
        if(!etapa.equalsIgnoreCase(solicitacao.getEtapa())) {
            emailProcessar.configurar(emailNovoResponsavel, solicitacao);
            emailProcessar.enviarEmailNotificacaoEtapa();
        }

        if (etapa.equalsIgnoreCase("5")) {
            solicitacaoRepository.atualizarEtapa(id, etapa, statusAprovado);
        } else if (solicitacao.getStatus().equalsIgnoreCase(statusRelatorio)) {
            solicitacaoRepository.atualizarEtapa(id, etapa, statusRelatorio);
        } else {
            solicitacaoRepository.atualizarEtapa(id, etapa, statusEmAnalise);
        }
        historicoSolicitacao.salvarHistoricoSolicitacaoId(id, role, "Etapa foi modificada de " + solicitacao.getEtapaAtualComoString() + " para " + solicitacao.verificarEtapaComoString(etapa));
        return ResponseEntity.ok().build();
    }


    public ResponseEntity salvarRelatorioFinal(SolicitarEstagio solicitacao, List<MultipartFile> arquivos) {
        if(solicitacao.getEtapa().equals("5")){
            historicoSolicitacao.salvarHistoricoSolicitacaoId(solicitacao.getId(), 1, "Relatório final foi adicionado pelo aluno");
            System.out.println(arquivos.size() + " Tamanho");
            System.out.println(arquivos.get(0).getOriginalFilename() + " Nome");
            fileImp.CriarRelatorioFinal(arquivos.get(0),solicitacao);
            solicitacao.setStatus(statusEmAnalise);
            solicitacao.setEtapa("2");
            solicitacao.setCancelamento(false);
            solicitacao.setRelatorioEntregue(true);
            solicitacaoRepository.save(solicitacao);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    public ResponseEntity cancelarEstagio(SolicitarEstagio solicitacao, List<MultipartFile> arquivos) {
        if(solicitacao.getEtapa().equals("5")){
            historicoSolicitacao.salvarHistoricoSolicitacaoId(solicitacao.getId(), 1, "Pedido de cancelamento foi adicionado pelo aluno");
            fileImp.SaveDocBlob(arquivos, solicitacao, false);
            solicitacao.setStatus(statusEmAnalise);
            solicitacao.setCancelamento(true);
            solicitacao.setEtapa("2");
            solicitacaoRepository.save(solicitacao);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }
}
