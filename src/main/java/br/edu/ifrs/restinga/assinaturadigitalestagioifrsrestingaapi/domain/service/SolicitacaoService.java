package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.service;


import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.ImplClasses.FileImp;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.ImplClasses.HistoricoSolicitacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.controller.BaseController;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.CoordenadorHandler;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.DiretorHandler;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.SetorEstagiosHandler;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosAtualizacaoSolicitacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosCadastroSolicitacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.file.GoogleAuthPendenteException;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.*;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.strategy.EmailProcessar;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class SolicitacaoService extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolicitacaoService.class);

    @Autowired
    private ApplicationEventPublisher publicadorDeEventos;

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
        String validacao = validarDeferimento(solicitacao,servidor.getRole());
        if(!validacao.equalsIgnoreCase("")){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(validacao);
        }
        long papel = servidor.getRole().getId();
        if (precisaGoogleDrive(solicitacao, papel) && !salvarDocumentoService.isPastaRaizConfigurada()) {
            // fallback: não deixa a solicitação presa em "Processando" por uma configuração ausente
            EstadoDeferimento estadoBloqueio = EstadoDeferimento.de(solicitacao);
            trocarProcessamento(solicitacao);
            historicoSolicitacao.mudarSolicitacao(solicitacao,
                    "Deferimento bloqueado: pasta raiz do Google Drive não configurada (GOOGLE_DRIVE_ROOT_FOLDER_ID)");
            publicadorDeEventos.publishEvent(new RecuperacaoDeferimento(solicitacao.getId(), estadoBloqueio,
                    "pasta raiz do Google Drive não configurada"));
            LOGGER.warn("Deferimento da solicitação {} bloqueado: pasta raiz do Google Drive não configurada",
                    solicitacao.getId());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Deferimento bloqueado: a pasta raiz do Google Drive não está configurada "
                            + "(google.drive.root-folder-id / GOOGLE_DRIVE_ROOT_FOLDER_ID).");
        }
        EstadoDeferimento anterior = EstadoDeferimento.de(solicitacao);
        try {
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
        } catch (Exception falha) {
            return recuperarAposFalha(solicitacao, anterior, falha);
        }
    }

    /**
     * Se a pasta raiz do Drive não estiver configurada, os caminhos que gravam documentos
     * (setor de estágio, coordenador e diretor) precisam do Drive. Cancelamento e deferimento
     * de relatório pelo coordenador não usam o Drive.
     */
    private boolean precisaGoogleDrive(SolicitarEstagio solicitacao, long papel) {
        if (solicitacao.isCancelamento()) {
            return false;
        }
        if (papel == roleIdCoordenador) {
            return !solicitacao.isRelatorioEntregue();
        }
        return true;
    }

    /**
     * Fallback de falha no deferimento: o status "Processando" é gravado por uma requisição
     * separada (setProcessando), então o rollback da transação do deferimento não o desfaz.
     * Aqui restauramos o estado anterior, garantimos sair de "Processando" e devolvemos uma
     * resposta de erro (em vez de estourar exceção) para que a transação commite essa recuperação.
     */
    private ResponseEntity<String> recuperarAposFalha(SolicitarEstagio solicitacao, EstadoDeferimento anterior, Exception falha) {
        String motivo = descreverFalha(falha);
        Long id = solicitacao.getId();
        try {
            anterior.restaurarEm(solicitacao);
            solicitacao.setStatus(statusEmAnalise);
            solicitacaoRepository.save(solicitacao);
        } catch (Exception persistencia) {
            LOGGER.error("Não foi possível restaurar o status da solicitação {}", id, persistencia);
        }
        try {
            historicoSolicitacao.mudarSolicitacao(solicitacao,
                    cortar("Falha ao deferir (" + motivo + "). Status restaurado para '" + statusEmAnalise + "'.", 250));
        } catch (Exception persistencia) {
            LOGGER.error("Não foi possível registrar o histórico da falha na solicitação {}", id, persistencia);
        }
        // rede de segurança: se a transação do deferimento terminar em rollback (inclusive
        // por causa desta própria recuperação), a restauração acima some com o rollback;
        // este evento roda depois do rollback e reaplica o estado fora da transação perdida.
        publicadorDeEventos.publishEvent(new RecuperacaoDeferimento(id, anterior, motivo));
        LOGGER.error("Falha ao deferir a solicitação {}: {}", id, motivo, falha);
        GoogleAuthPendenteException pendente = causaDoTipo(falha, GoogleAuthPendenteException.class);
        if (pendente != null) {
            String url = pendente.getUrlAutorizacao() == null ? "" : " URL: " + pendente.getUrlAutorizacao();
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(pendente.getMessage() + url);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Falha ao deferir a solicitação: " + motivo);
    }

    private static <T extends Throwable> T causaDoTipo(Throwable falha, Class<T> tipo) {
        Throwable atual = falha;
        while (atual != null) {
            if (tipo.isInstance(atual)) {
                return tipo.cast(atual);
            }
            atual = atual.getCause();
        }
        return null;
    }

    private static String descreverFalha(Throwable falha) {
        Throwable raiz = falha;
        while (raiz.getCause() != null) {
            raiz = raiz.getCause();
        }
        String descricao = raiz.getMessage() == null ? raiz.getClass().getSimpleName() : raiz.getMessage();
        descricao = descricao.replaceAll("\\s+", " ").trim();
        return descricao.length() > 300 ? descricao.substring(0, 300) + "..." : descricao;
    }

    private static String cortar(String texto, int limite) {
        return texto.length() <= limite ? texto : texto.substring(0, limite);
    }

    public record RecuperacaoDeferimento(Long solicitacaoId, EstadoDeferimento estadoAnterior, String motivo) {
    }

    /**
     * Rede de segurança para a solicitação não ficar presa em "Processando": se a transação do
     * deferimento terminar em rollback (inclusive por falha na própria restauração), a restauração
     * feita dentro da transação também desaparece. Este listener roda depois do rollback, fora dessa
     * transação, e reaplica o estado anterior + status "Em analise".
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void restaurarSolicitacaoAposRollback(RecuperacaoDeferimento evento) {
        solicitacaoRepository.findById(evento.solicitacaoId()).ifPresent(solicitacao -> {
            evento.estadoAnterior().restaurarEm(solicitacao);
            solicitacao.setStatus(statusEmAnalise);
            solicitacaoRepository.save(solicitacao);
            LOGGER.warn("Transação de deferimento revertida: solicitação {} restaurada para '{}'",
                    evento.solicitacaoId(), statusEmAnalise);
            try {
                historicoSolicitacao.mudarSolicitacao(solicitacao,
                        cortar("Falha ao deferir (transação revertida): " + evento.motivo(), 250));
            } catch (Exception persistencia) {
                LOGGER.warn("Não foi possível registrar o histórico da solicitação {} após rollback",
                        evento.solicitacaoId(), persistencia);
            }
        });
    }

    private record EstadoDeferimento(String status, String etapa, String statusSetorEstagio,
                                     String statusEtapaCoordenador, String statusEtapaDiretor, boolean editavel) {

        static EstadoDeferimento de(SolicitarEstagio solicitacao) {
            return new EstadoDeferimento(solicitacao.getStatus(), solicitacao.getEtapa(),
                    solicitacao.getStatusSetorEstagio(), solicitacao.getStatusEtapaCoordenador(),
                    solicitacao.getStatusEtapaDiretor(), solicitacao.isEditavel());
        }

        void restaurarEm(SolicitarEstagio solicitacao) {
            solicitacao.setStatus(status);
            solicitacao.setEtapa(etapa);
            solicitacao.setStatusSetorEstagio(statusSetorEstagio);
            solicitacao.setStatusEtapaCoordenador(statusEtapaCoordenador);
            solicitacao.setStatusEtapaDiretor(statusEtapaDiretor);
            solicitacao.setEditavel(editavel);
        }
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
