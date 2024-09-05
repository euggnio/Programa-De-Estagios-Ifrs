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
                "Nova",
                "1",
                true,
                dados.cargaHoraria(),
                dados.salario(),
                dados.turnoEstagio());
    }

    public boolean verificarSolicitacaoExistente(long alunoId, String tipoSolicitacao) {
        List<SolicitarEstagio> quantidade = solicitacaoRepository.findByAluno_Id(alunoId);
        quantidade.removeIf(solicitacao ->
                solicitacao.getStatus().equalsIgnoreCase("indeferido")
                        || solicitacao.getStatus().equalsIgnoreCase("aprovado")
                        || solicitacao.getStatus().equalsIgnoreCase("cancelado")
                        || solicitacao.getStatus().equalsIgnoreCase("finalizado"));
        quantidade.removeIf(solicitacao -> !solicitacao.getTipo().equalsIgnoreCase(tipoSolicitacao));
        return quantidade.size() >= 1;
    }

    public ResponseEntity setProcessando(long id){
        Optional<SolicitarEstagio> solicitacao = solicitacaoRepository.findById(id);
        if(solicitacao.isPresent() && !solicitacao.get().isCancelamento()){
            solicitacao.get().setStatus("Processando");
            solicitacaoRepository.save(solicitacao.get());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    public ResponseEntity modificarObservacao(SolicitarEstagio solicitacao, String observacao, Long  role) {
        solicitacaoRepository.atualizarObservacao(solicitacao.getId(), observacao);
        solicitacao.setObservacao(observacao);
        historicoSolicitacao.salvarHistoricoSolicitacaoId(solicitacao.getId(), role, "Observação para edição: " + observacao);
        EmailProcessar emailProcessar = new EmailProcessar(solicitacao);
        emailProcessar.enviarEmailObservacao();
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
        if (servidor.getCargo().equals("Coordenador")) {
            return obterSolicitacoesParaCoordenador(servidor);
        } else if (servidor.getCargo().equals("Diretor")) {
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
            if(servidor.getRole().getId() == 3){
                solicitacao.setStatusSetorEstagio("Indeferido");
            } else if (servidor.getRole().getId() == 2) {
                solicitacao.setStatusEtapaCoordenador("Indeferido");
            }else{
                solicitacao.setStatusEtapaDiretor("Indeferido");
            }
            if (dados.observacao() != null) {
                solicitacao.setObservacao(dados.observacao());
            }
            solicitacao.setStatus("Indeferido");
            solicitacao.setEditavel(false);
            solicitacaoRepository.save(solicitacao);
            historicoSolicitacao.mudarSolicitacao(solicitacao, "Indeferido, motivo: '" + solicitacao.getObservacao() + "'");

            EmailProcessar emailProcessar = new EmailProcessar(solicitacao);
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
        if(solicitarEstagio.getStatus().equalsIgnoreCase("Processando")){
            solicitarEstagio.setStatus("Em análise");
            solicitacaoRepository.save(solicitarEstagio);
        }
    }


    //TODO: Refatorar o email e drive para caso conexão esteja offline
    private void deferirSetorEstagio(SolicitarEstagio solicitacao){
            try{
            EmailProcessar emailProcessar = new EmailProcessar(solicitacao);
            if(solicitacao.isCancelamento()){
                estagiarioService.desativarEstagiario(solicitacao.getId());
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
        EmailProcessar emailProcessar = new EmailProcessar(solicitacao);
        try {
            String nomePasta = solicitacao.getAluno().getNomeCompleto() + " - " + solicitacao.getAluno().getMatricula();
            salvarDocumentoService.salvarDocumentoDeSolicitacao(nomePasta, solicitacao.getCurso().getId(),docsParaDrive, solicitacao.getAluno().getUsuarioSistema().getEmail());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (solicitacao.getTipo().equalsIgnoreCase("relatório")) {
            docsParaDrive.removeIf(documento -> !documento.getNome().contains("RELATORIO"));
            emailProcessar.enviarRelatorioEntregue();
        }
        else{
            emailProcessar.enviarEmailDocsAssinadosComLink(salvarDocumentoService.getPastaAluno());
        }

    }

    private void lidarErroDeferimento(SolicitarEstagio solicitacao){
        solicitacao.setStatus("Em análise");
        solicitacao.setEtapa("2");
        solicitacaoRepository.save(solicitacao);
    }

    private void deferirCoordenador(SolicitarEstagio solicitacao) {
            EmailProcessar emailProcessar = new EmailProcessar(solicitacao);
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
        EmailProcessar emailProcessar = new EmailProcessar(solicitacao);
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
        if(solicitacao.getStatus().equalsIgnoreCase("Indeferido")){
            return "Não é possivel deferir uma solicitação que já foi concluída ou deferida";
        }
        else if(solicitacao.getStatus().equalsIgnoreCase("aprovado") && solicitacao.getEtapa().equals("5")){
            return "Está solicitação já foi concluida e ela não pode mais ser deferida.";
        }
        else if(solicitacao.getEtapa().equals("2") && !(role.getId() == 3)) {
            return "Apenas o setor de estágios pode deferir uma solicitação na etapa 2.";
        }
        else if (solicitacao.getEtapa().equals("3") && (role.getId() == 4)) {
            return "Apenas o coordenador pode deferir uma solicitação na etapa 3.";
        }
        else if (solicitacao.getEtapa().equals("4") && (role.getId() == 2 )) {
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
            Optional<Servidor> diretor = servidorRepository.findServidorByCurso_Id(16);
            if (diretor.isPresent()){
                emailNovoResponsavel = diretor.get().getUsuarioSistema().getEmail();
            }
            else{
                return ResponseEntity.notFound().build();
            }
        }
        if(!etapa.equalsIgnoreCase(solicitacao.getEtapa())) {
            EmailProcessar emailProcessar = new EmailProcessar(emailNovoResponsavel, solicitacao);
            emailProcessar.enviarEmailNotificacaoEtapa();
        }

        if (etapa.equalsIgnoreCase("5")) {
            solicitacaoRepository.atualizarEtapa(id, etapa, "Aprovado");
        } else if (solicitacao.getStatus().equalsIgnoreCase("relatório")) {
            solicitacaoRepository.atualizarEtapa(id, etapa, "Relatório");
        } else {
            solicitacaoRepository.atualizarEtapa(id, etapa, "Em análise");
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
            solicitacao.setStatus("Em análise");
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
            solicitacao.setStatus("Em análise");
            solicitacao.setCancelamento(true);
            solicitacao.setEtapa("2");
            solicitacaoRepository.save(solicitacao);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }
}
