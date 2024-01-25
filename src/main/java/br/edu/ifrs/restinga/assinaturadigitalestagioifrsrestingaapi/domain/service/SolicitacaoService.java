package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.service;


import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.ImplClasses.FileImp;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.ImplClasses.HistoricoSolicitacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.controller.BaseController;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosAtualizacaoSolicitacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosCadastroSolicitacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.file.GoogleEmail;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Service
public class SolicitacaoService extends BaseController {

    @Autowired
    FileImp fileImp;

    @Autowired
    private HistoricoSolicitacao historicoSolicitacao;
    @Autowired
    private SalvarDocumentoService salvarDocumentoService;

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
        saveSolicitacao(solicitacao, "Cadastrado");

        return ResponseEntity.ok().build();
    }

    private SolicitarEstagio criarSolicitacao(DadosCadastroSolicitacao dados, Curso curso, Aluno aluno) {
        return new SolicitarEstagio(dados.finalDataEstagio(),
                dados.inicioDataEstagio(),
                aluno,
                curso,
                dados.tipo(),
                dados.titulo(),
                dados.nomeEmpresa(),
                dados.ePrivada(),
                dados.contatoEmpresa(),
                dados.agente(),
                dados.conteudo(),
                dados.observacao(),
                "Nova",
                "1",
                true);
    }

    private void saveSolicitacao(SolicitarEstagio solicitacao, String situacao) {
        solicitacaoRepository.save(solicitacao);
        historicoSolicitacao.mudarSolicitacao(solicitacao, situacao);
    }

    public boolean verificarSolicitacaoExistente(long alunoId, String tipoSolicitacao) {
        int quantidade = solicitacaoRepository.countByAluno_IdAndTipoAndStatusNotContainingIgnoreCaseAndStatusNotContainingIgnoreCase(alunoId, tipoSolicitacao, "Indeferido" , "Deferido");
        return (quantidade >= 2) ? true : false;
    }

    @Transactional
    public ResponseEntity atualizarStatus(long idSolicitacao, String status, Servidor servidor) {
        Optional<SolicitarEstagio> solicitarEstagio = solicitacaoRepository.findById(idSolicitacao);
        if (solicitarEstagio.isPresent() && servidor != null) {
            switch (status.toLowerCase()) {
                case "indeferido":
                    setStatusIndeferido(solicitarEstagio.get(), servidor);
                    break;
                case "deferido":
                    setDeferidoStatus(solicitarEstagio.get(), servidor);
                    break;
                case "em andamento":
                    setEmAndamentoStatus(solicitarEstagio.get(), servidor);
                    break;
                default:
                    solicitacaoRepository.atualizarStataus(solicitarEstagio.get().getId(), status);
                    break;
            }
            solicitacaoRepository.save(solicitarEstagio.get());
            return ResponseEntity.ok("Sucesso");
        }
        return ResponseEntity.notFound().build();
    }

    public void setStatusIndeferido(SolicitarEstagio solicitarEstagio, Servidor servidor) {
        solicitarEstagio.setStatus("Indeferido");
        if (servidor.getRole().getName().contains("ESTAGIO")) {
            solicitarEstagio.setStatusSetorEstagio("Indeferido");
        } else if (servidor.getRole().getName().contains("SERVIDOR")) {
            solicitarEstagio.setStatusEtapaCoordenador("Indeferido");
        } else {
            solicitarEstagio.setStatusEtapaDiretor("Indeferido");
        }
    }

    private void setDeferidoStatus(SolicitarEstagio solicitarEstagio, Servidor servidor) {
        solicitarEstagio.setStatus("Deferido");
        if (servidor.getRole().getName().contains("ESTAGIO")) {
            solicitarEstagio.setStatusSetorEstagio("Deferido");
        } else if (servidor.getRole().getName().contains("SERVIDOR")) {
            solicitarEstagio.setStatusEtapaCoordenador("Deferido");
        } else {
            solicitarEstagio.setStatusEtapaDiretor("Deferido");
        }
    }

    private void setEmAndamentoStatus(SolicitarEstagio solicitarEstagio, Servidor servidor) {
        solicitarEstagio.setStatus("Em andamento");
        solicitarEstagio.setEtapa("2");
        if (servidor.getRole().getName().contains("ESTAGIO")) {
            solicitarEstagio.setStatusSetorEstagio("");
        } else if (servidor.getRole().getName().contains("SERVIDOR")) {
            solicitarEstagio.setStatusEtapaCoordenador("");
        } else {
            solicitarEstagio.setStatusEtapaDiretor("");
        }
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
            return solicitacaoRepository.findByCursoAndEtapaEqualsAndStatusNotContainingIgnoreCase(curso.get(), "3", "Deferido");
        }
        private List<SolicitarEstagio> obterSolicitacoesParaDiretor() {
            return solicitacaoRepository.findAllByEtapaIsAndStatusEqualsIgnoreCase("4", "Em Andamento");
        }

        public ResponseEntity<String> indeferirSolicitacao(long id, Servidor servidor, DadosAtualizacaoSolicitacao dados){
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
                historicoSolicitacao.mudarSolicitacao(solicitacao, "Indeferido");
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.notFound().build();
        }

        public ResponseEntity<String> deferirSolicitacao(SolicitarEstagio solicitacao, Servidor servidor, List<MultipartFile> documentos){
            System.out.println("DEBUGGER 2");
            if(validarDeferimento(solicitacao,servidor.getRole()).equalsIgnoreCase("")){
                System.out.println("DEBUGGER 4");
                switch (servidor.getRole().getId().toString()) {
                    case "3" -> deferirSetorEstagio(solicitacao);
                    case "2" -> deferirCoordenador(solicitacao);
                    case "4" -> {
                        deferirDiretor(solicitacao);
                        try {
                            GoogleEmaileDrive(solicitacao);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                System.out.println("DEBUGGER 5");
                salvarArquivos(documentos,solicitacao);
                solicitacao.setObservacao("");
                solicitacao.setEditavel(false);
                saveSolicitacao(solicitacao, "Deferido");
                return ResponseEntity.ok().build();
            }
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(validarDeferimento(solicitacao,servidor.getRole()));
        }

        public void GoogleEmaileDrive(SolicitarEstagio solicitacao) throws Exception {
            List<Documento> docsParaDrive = documentoRepository.findBySolicitarEstagioId(solicitacao.getId());
                salvarDocumentoService.salvarDocumentoDeSolicitacao(solicitacao.getAluno().getMatricula(), docsParaDrive);
                String titulo = "Documentos Assiandos!!";
                String msg = "Fim do processo!";
                String body = """
                        <html>
                            <body style='font-family: Arial, sans-serif;'>
                            <h2 style='color: #3498db;'> Documentos Assinados! </h2>
                                <p>Olá""" + " " + solicitacao.getAluno().getNomeCompleto() + ",</p>" + """
                        <p>Os documentos da sua solicitação foram assinados!!!</p>
                        <p style='background-color: #ecf0f1; padding: 10px; font-size: 18px; font-weight: bold;'>"""
                        + " https://drive.google.com/drive/u/0/folders/" + salvarDocumentoService.getPastaAluno() + "</p>" + """
                                    <p>Desejamos um  bom estágio!!!.</p>
                                </body>
                            </html>
                        """;
                GoogleEmail.sendMail(solicitacao.getAluno().getUsuarioSistema().getEmail(),titulo,msg,body);
        }

        public void salvarArquivos(List<MultipartFile> docs, SolicitarEstagio solicitarEstagio){
            if(docs != null && !docs.isEmpty() ){
                fileImp.SaveDocBlob(docs,solicitarEstagio,true);
            }
        }
    private void deferirSetorEstagio(SolicitarEstagio solicitacao) {
        System.out.println("DEBUGGER");
        solicitacao.setStatusSetorEstagio("Deferido");
        solicitacao.setEtapa("3");
        solicitacao.setStatusEtapaCoordenador("Em Andamento");
    }

    private void deferirCoordenador(SolicitarEstagio solicitacao) {
        solicitacao.setStatusEtapaCoordenador("Deferido");
        solicitacao.setEtapa("4");
        solicitacao.setStatusEtapaDiretor("Em Andamento");
    }

    public void deferirDiretor(SolicitarEstagio solicitacao){
        solicitacao.setEtapa("5");
        solicitacao.setStatusEtapaDiretor("Deferido");
        solicitacao.setStatus("Deferido");
    }

    public String validarDeferimento(SolicitarEstagio solicitacao, Role role){
            if(solicitacao.getStatus().equalsIgnoreCase("Indeferido")){
                return "Não é possivel deferir uma solicitação que já foi concluída ou deferida";
            }
            if(solicitacao.getEtapa().equals("5")){
                return "Está solicitação já foi concluida e ela não pode mais ser deferida.";
            }
            else if (solicitacao.getEtapa().equals("2") && !(role.getId() == 3)) {
                return "Apenas o setor de estágios pode deferir uma solicitação na etapa 2.";
            }
            else if (solicitacao.getEtapa().equals("3") && !(role.getId() == 2)) {
                return "Apenas o coordenador pode deferir uma solicitação na etapa 3.";
            }
            else if (solicitacao.getEtapa().equals("4") && !(role.getId() == 4)) {
                return "Apenas o diretor pode deferir uma solicitação na etapa 4.";
            }
            else{
                return "";
            }
        }


}
