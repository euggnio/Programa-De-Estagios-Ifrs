package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.service;


import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.ImplClasses.FileImp;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.ImplClasses.HistoricoSolicitacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.controller.BaseController;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.repository.UsuarioRepository;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosAtualizacaoSolicitacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosCadastroSolicitacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.file.GoogleEmail;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.*;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.strategy.DocumentosAssinadosEmailStrategy;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.strategy.EmailStrategy;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.strategy.EstagioCanceladoEmailStrategy;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.strategy.NotificacaoEtapaEmailStrategy;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

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
                case "em análise":
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
        solicitarEstagio.setStatus("Em análise");
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
            return solicitacaoRepository.findAllByEtapaIsAndStatusEqualsIgnoreCase("4", "Em análise");
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

        @Transactional
        public ResponseEntity<String> deferirSolicitacao(SolicitarEstagio solicitacao, Servidor servidor, List<MultipartFile> documentos){
            if(validarDeferimento(solicitacao,servidor.getRole()).equalsIgnoreCase("")){
                historicoSolicitacao.mudarSolicitacao(solicitacao, "Deferido");
                solicitacaoRepository.atualizarStataus(solicitacao.getId(), "Deferindo");
                System.out.println("Role do servidor deferindo: " + servidor.getRole().getId().toString());
                switch (servidor.getRole().getId().toString()) {
                    case "3" -> deferirSetorEstagio(solicitacao);
                    case "2" -> deferirCoordenador(solicitacao);
                    case "4" -> deferirDiretor(solicitacao);
                }
                if(documentos != null && !documentos.isEmpty()){
                    salvarArquivos(documentos,solicitacao);
                }
                return ResponseEntity.ok().build();
            }
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(validarDeferimento(solicitacao,servidor.getRole()));
        }




    private void deferirSetorEstagio(SolicitarEstagio solicitacao){
        if(solicitacao.getStatus().equalsIgnoreCase("cancelamento")){
            solicitacao.setEtapa("5");
            solicitacao.setStatus("Cancelado");
            estagiariosRepository.updateAtivo(solicitacao.getIdReferente(), false);
            GoogleEnviarEmailCancelamento(solicitacao);
        }
        else {
            solicitacaoRepository.atualizarStataus(solicitacao.getId(), "Deferindo");
            solicitacao.setStatusSetorEstagio("Deferido");
            solicitacao.setStatus("Deferido");
            solicitacao.setObservacao("");
            solicitacao.setEtapa("5");
            solicitacao.setEditavel(false);
            GoogleEmaileDrive(solicitacao);
            if(estagiariosRepository.existsBySolicitacaoId(solicitacao.getId())){
                System.out.println("Estagiario já existe.");
            }
            else {
                estagiariosRepository.save(new Estagiarios(solicitacao,salvarDocumentoService.getPastaAluno()));
            }
        }
        solicitacaoRepository.save(solicitacao);
    }

        public void GoogleEmaileDrive(SolicitarEstagio solicitacao)  {
            List<Documento> docsParaDrive = documentoRepository.findBySolicitarEstagioId(solicitacao.getId());
            if (solicitacao.getStatus().equalsIgnoreCase("relatório")) {
                docsParaDrive.removeIf(documento -> !documento.getNome().contains("RELATORIO"));
            }

            try {
                String nomePasta = solicitacao.getAluno().getNomeCompleto() + " - " + solicitacao.getAluno().getMatricula();
                salvarDocumentoService.salvarDocumentoDeSolicitacao(nomePasta, 10 ,docsParaDrive);
                EmailStrategy emailStrategy = new DocumentosAssinadosEmailStrategy();
                GoogleEmail.sendMail(solicitacao.getAluno().getUsuarioSistema().getEmail()
                        ,emailStrategy.getTitle()
                        ,emailStrategy.getTitle()
                        ,emailStrategy.getBody(solicitacao,salvarDocumentoService.getPastaAluno()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }



    public void GoogleEnviarEmailCancelamento(SolicitarEstagio solicitacao){
        EmailStrategy emailStrategy = new EstagioCanceladoEmailStrategy();
        try {
            GoogleEmail.sendMail(solicitacao.getAluno().getUsuarioSistema().getEmail()
                        ,emailStrategy.getTitle()
                        ,emailStrategy.getTitle()
                        ,emailStrategy.getBody(solicitacao,""));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }

    private void deferirCoordenador(SolicitarEstagio solicitacao) {
        if(solicitacao.getStatus().equalsIgnoreCase("relatório")){
            GoogleEmaileDrive(solicitacao);
            solicitacao.setEtapa("5");
            solicitacao.setStatus("Finalizado");
        }
        else {
            solicitacao.setStatusEtapaCoordenador("Deferido");
            solicitacao.setEtapa("4");
            solicitacao.setStatusEtapaDiretor("Em análise");
        }
        solicitacao.setObservacao("");
        solicitacao.setEditavel(false);
        solicitacaoRepository.save(solicitacao);
    }

    public void deferirDiretor(SolicitarEstagio solicitacao){
        solicitacao.setEtapa("5");
        solicitacao.setStatusEtapaDiretor("Deferido");
        solicitacao.setStatus("Deferido");
        solicitacao.setObservacao("");
        solicitacao.setEditavel(false);
        solicitacaoRepository.save(solicitacao);
        GoogleEmaileDrive(solicitacao);
        estagiariosRepository.save(new Estagiarios(solicitacao,salvarDocumentoService.getPastaAluno()));
    }

    public void salvarArquivos(List<MultipartFile> docs, SolicitarEstagio solicitarEstagio){
        if(docs != null && !docs.isEmpty() ){
            fileImp.SaveDocBlob(docs,solicitarEstagio,true);
        }
    }

    public String validarDeferimento(SolicitarEstagio solicitacao, Role role){
            if(solicitacao.getStatus().equalsIgnoreCase("Indeferido")){
                return "Não é possivel deferir uma solicitação que já foi concluída ou deferida";
            }
            else if(solicitacao.getStatus().equalsIgnoreCase("Deferido") && solicitacao.getEtapa().equals("5")){
                return "Está solicitação já foi concluida e ela não pode mais ser deferida.";
            }
            else if (solicitacao.getStatus().equalsIgnoreCase("finalizada")){
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
        EmailStrategy emailStrategy = new NotificacaoEtapaEmailStrategy();

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

        if(!etapa.equalsIgnoreCase(solicitacao.getEtapa())){
            try {
                System.out.println("Email enviado para: " + emailNovoResponsavel);
                System.out.println("Email enviado para: " + solicitacao.getAluno().getUsuarioSistema().getEmail());
                GoogleEmail.sendMail(emailNovoResponsavel
                        ,emailStrategy.getTitle()
                        ,emailStrategy.getTitle()
                        ,emailStrategy.getBody(solicitacao, String.valueOf(solicitacao.getId())));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (etapa.equalsIgnoreCase("5")) {
            solicitacaoRepository.atualizarEtapa(id, etapa, "Deferido");
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
            fileImp.criarRelatorioFinal(arquivos.get(0),solicitacao);
            solicitacao.setStatus("Relatório");
            solicitacao.setEtapa("2");
            solicitacao.setRelatorioEntregue(true);
            solicitacaoRepository.save(solicitacao);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    public ResponseEntity cancelarEstagio(SolicitarEstagio solicitacao, List<MultipartFile> arquivos) {
        if(solicitacao.getEtapa().equals("5")){
            historicoSolicitacao.salvarHistoricoSolicitacaoId(solicitacao.getId(), 1, "Solicitação de cancelamento foi adicionada pelo aluno");
            fileImp.SaveDocBlob(arquivos, solicitacao, false);
            solicitacao.setStatus("Cancelamento");
            solicitacao.setTipo("Cancelamento");
            solicitacao.setEtapa("2");
            solicitacaoRepository.save(solicitacao);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }
}
