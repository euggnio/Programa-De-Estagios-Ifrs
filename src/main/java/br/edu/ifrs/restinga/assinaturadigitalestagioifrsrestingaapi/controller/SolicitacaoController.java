package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.controller;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.ImplClasses.HistoricoSolicitacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.ImplClasses.FileImp;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.service.SalvarDocumentoService;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.service.SolicitacaoService;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosAtualizacaoSolicitacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosCadastroSolicitacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosListagemSolicitacaoAluno;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosListagemSolicitacaoServidor;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.file.GoogleEmail;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.*;
import jakarta.transaction.Transactional;
import org.apache.http.protocol.HTTP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Role;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin("*")
public class SolicitacaoController extends BaseController {
    @Autowired
    FileImp fileImp;
    @Autowired
    private HistoricoSolicitacao historicoSolicitacao;
    @Autowired
    private SolicitacaoService solicitacaoService;
    @Autowired
    private SalvarDocumentoService salvarDocumentoService;

    //Em teste para evitar deadlock diretor.
    private final Object lock = new Object();

    @PostMapping(value = "/cadastrarSolicitacao")
    public ResponseEntity cadastrarSolicitacao(@RequestPart("dados") DadosCadastroSolicitacao dados,
                                               @RequestParam("file") List<MultipartFile> arquivos) {
            return solicitacaoService.cadastrarSolicitacao(dados, arquivos);
    }

    @GetMapping("/listarDocumentos")
    @ResponseBody
    public String listar(@RequestParam long id) {
        Optional<SolicitarEstagio> solicitacao = solicitacaoRepository.findById(id);
        return solicitacao.get().getDocumento().get(1).getNome();
    }


    @GetMapping("/setEdicaoDocumentosSolicitacao")
    public ResponseEntity setEditavel(@RequestParam long id, @RequestHeader("Authorization") String token) {
        if (usuarioRepository.findByEmailAndNotRoleId1(tokenService.getSubject(token.replace("Bearer ", ""))).isPresent()) {
            solicitacaoRepository.atualizarEditavelParaTrue(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/editarobservacaoSolicitacao")
    public ResponseEntity setObservacao(@RequestParam long id, @RequestParam String texto, @RequestHeader("Authorization") String token) {
        if (usuarioRepository.findByEmailAndNotRoleId1(tokenService.getSubject(token.replace("Bearer ", ""))).isPresent()) {
            solicitacaoRepository.atualizarObservacao(id, texto);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/editarEmpresaSolicitacao")
    public ResponseEntity editarEmpresa(@RequestParam long id, @RequestBody DadosAtualizacaoSolicitacao empresa, @RequestHeader("Authorization") String token){
        if (usuarioRepository.findByEmailAndNotRoleId1(tokenService.getSubject(token.replace("Bearer ", ""))).isPresent()) {
            Optional<SolicitarEstagio> solicitarEstagio = solicitacaoRepository.findById(id);
            if(solicitarEstagio.isPresent()){
                solicitarEstagio.get().setNomeEmpresa(empresa.nomeEmpresa());
                solicitarEstagio.get().setAgente(empresa.agente());
                solicitarEstagio.get().setContatoEmpresa(empresa.contatoEmpresa());
                solicitarEstagio.get().setEPrivada(empresa.ePrivada());
                solicitacaoRepository.save(solicitarEstagio.get());
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }


    @GetMapping("/editarstatus")
    public ResponseEntity setSolicitacao(@RequestParam long id, @RequestParam String status, @RequestHeader("Authorization") String token) {
        Servidor servidor = servidorRepository.findByUsuarioSistemaEmail(tokenService.getSubject(token.replace("Bearer ", "")));
        solicitacaoService.atualizarStatus(id, status, servidor);
        return ResponseEntity.ok().build();
    }

    //?Verificar se a edição da etapa reseta os status da etapa no coordenador e diretor.
    @GetMapping("/editarEtapa")
    public ResponseEntity setEtapa(@RequestParam long id, @RequestParam String etapa, @RequestHeader("Authorization") String token) {
        if (usuarioRepository.findByEmailAndNotRoleId1(tokenService.getSubject(token.replace("Bearer ", ""))).isPresent()) {
            System.out.println(etapa);
            if(etapa.equalsIgnoreCase("5")){
                solicitacaoRepository.atualizarEtapa(id, etapa , "Deferido");
            }
            else {
                solicitacaoRepository.atualizarEtapa(id, etapa, "Em andamento");
            }
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/dadosSolicitacaoAluno")
    public ResponseEntity<List<DadosListagemSolicitacaoAluno>> obterSolicitacoes(@RequestHeader("Authorization") String token) {
        Aluno aluno = alunoRepository.findByUsuarioSistemaEmail(tokenService.getSubject(token.replace("Bearer ", "")));
        return ResponseEntity.ok(solicitacaoRepository.findByAluno(aluno).stream()
                .map(DadosListagemSolicitacaoAluno::new)
                .toList());
    }

    @GetMapping("/listarSolicitacoses")
    public ResponseEntity<List<SolicitarEstagio>> listarSolicitacoes() {
        List<SolicitarEstagio> solicitacoes = solicitacaoRepository.findAll();
        return solicitacoes.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(solicitacoes);
    }

    @GetMapping("/listarSolicitacoesPorEmailServidor")
    public ResponseEntity<List<DadosListagemSolicitacaoServidor>> listarSolicitacoesPorEmailServidor(@RequestHeader("Authorization") String token) {
        String email = tokenService.getSubject(token.replace("Bearer ", ""));
        var servidor = servidorRepository.findByUsuarioSistemaEmail(email);
        if(servidor == null){
            return  ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<SolicitarEstagio> publisolicitacoes = solicitacaoService.obterSolicitacoesDoServidor(servidor);
        List<DadosListagemSolicitacaoServidor> dadosSolicitacoes = publisolicitacoes.stream()
                .map(DadosListagemSolicitacaoServidor::new)
                .toList();
        return ResponseEntity.ok(dadosSolicitacoes);
    }

    @GetMapping("/alunoSolicitacao/{id}")
    public ResponseEntity<DadosListagemSolicitacaoAluno> getAlunoSolicitacao(@PathVariable("id") Long id , @RequestHeader("Authorization") String token) {
        String email = tokenService.getSubject(token.replace("Bearer ", ""));
        var servidor = servidorRepository.findByUsuarioSistemaEmail(email);
        if(servidor == null){
            return  ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<SolicitarEstagio> solicitacao = solicitacaoRepository.findById(id);
        if (solicitacao.isPresent()) {
            DadosListagemSolicitacaoAluno dadosSolicitacao = new DadosListagemSolicitacaoAluno(solicitacao.get());
            if (solicitacao.get().getStatus().equals("Nova")) {
                solicitacao.get().setStatus("Em andamento");
                solicitacao.get().setEtapa("2");
                solicitacao.get().setEditavel(false);
                solicitacaoRepository.save(solicitacao.get());
            }
            return ResponseEntity.ok(dadosSolicitacao);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/trocarValidadeContrato")
    public ResponseEntity trocarDataContrato(@RequestParam Long id, @RequestParam String dataFinalNova, @RequestParam String dataInicioNova , @RequestHeader("Authorization") String token) {
        String email = tokenService.getSubject(token.replace("Bearer ", ""));
        var servidor = servidorRepository.findByUsuarioSistemaEmail(email);
        if(servidor == null){
            return  ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<SolicitarEstagio> solicitacao = solicitacaoRepository.findById(id);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        if (solicitacao.isPresent()) {
            solicitacao.get().setFinalDataEstagio(LocalDate.parse(dataFinalNova, formatter));
            solicitacao.get().setInicioDataEstagio(LocalDate.parse(dataInicioNova, formatter));
            solicitacaoRepository.save(solicitacao.get());
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/solicitacao/{id}")
    public ResponseEntity<DadosAtualizacaoSolicitacao> getSolicitacaoById(@PathVariable("id") Long id) {
        Optional<SolicitarEstagio> solicitacaoOptional = solicitacaoRepository.findById(id);
        if (solicitacaoOptional.isPresent()) {
            SolicitarEstagio solicitacao = solicitacaoOptional.get();
            DadosAtualizacaoSolicitacao solicitacaoDTO = new DadosAtualizacaoSolicitacao(solicitacao.getId()
                    , solicitacao.getStatus()
                    , solicitacao.getEtapa()
                    , solicitacao.getStatusSetorEstagio()
                    , solicitacao.getStatusEtapaCoordenador()
                    , solicitacao.isEditavel()
                    , solicitacao.getStatusEtapaDiretor()
                    , solicitacao.getObservacao());
            return ResponseEntity.ok(solicitacaoDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/documentosSolicitacao/{id}")
    public ResponseEntity<List<Documento>> getDocumentosSolicitacao(@PathVariable("id") Long id) {
        Optional<SolicitarEstagio> solicitacao = solicitacaoRepository.findById(id);
        return solicitacao.map(solicitarEstagio -> ResponseEntity.ok(solicitarEstagio.getDocumento())).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/deferirSolicitacao/{id}")
    @Transactional
    public synchronized ResponseEntity deferirSolicitacao(@PathVariable("id") Long id,
                                                          @RequestPart("dados") DadosAtualizacaoSolicitacao dados,
                                                          @RequestParam(value = "file", required = false) List<MultipartFile> files,
                                                          @RequestHeader("Authorization") String token) {
        //Verificar se é servidor  e se a solicitação existe
        String email = tokenService.getSubject(token.replace("Bearer ", ""));
        Servidor servidor = servidorRepository.findByUsuarioSistemaEmail(email);
        Optional<SolicitarEstagio> solicitacaoOptional = solicitacaoRepository.findById(id);
        if (!solicitacaoOptional.isPresent() || servidor == null) {
            return ResponseEntity.notFound().build();
        }
        return solicitacaoService.deferirSolicitacao(solicitacaoOptional.get(),servidor,files);
    }


    @PutMapping("/indeferirSolicitacao/{id}")
    @Transactional
    public ResponseEntity indeferirSolicitacao(@PathVariable("id") Long id, @RequestBody DadosAtualizacaoSolicitacao dados, @RequestHeader("Authorization") String token) {
        String email = tokenService.getSubject(token.replace("Bearer ", ""));
        Servidor servidor = servidorRepository.findByUsuarioSistemaEmail(email);
        if (servidor.getRole().getId() == 1) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Você não tem permissão para indeferir essa solicitação.");
        }
        return solicitacaoService.indeferirSolicitacao(id,servidor,dados);
    }

}