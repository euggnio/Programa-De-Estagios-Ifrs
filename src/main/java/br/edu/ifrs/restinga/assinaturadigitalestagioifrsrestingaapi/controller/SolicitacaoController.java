package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.controller;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.ImplClasses.HistoricoSolicitacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.ImplClasses.FileImp;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.service.SolicitacaoService;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosAtualizacaoSolicitacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosCadastroSolicitacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosListagemSolicitacaoAluno;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosListagemSolicitacaoServidor;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
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

    @PostMapping(value = "/cadastrarSolicitacao")
    public ResponseEntity cadastrarSolicitacao(@RequestPart("dados") DadosCadastroSolicitacao dados,
                                               @RequestParam("file") List<MultipartFile> arquivos) {
        return solicitacaoService.cadastrarSolicitacao(dados, arquivos);
    }


    //@GetMapping("/listarDocumentos")
    //@ResponseBody
    //public String listar(@RequestParam long id) {
    //    Optional<SolicitarEstagio> solicitacao = solicitacaoRepository.findById(id);
    //   if(solicitacao.isPresent()){
    //        return solicitacao.get().getDocumento().get(1).getNome();
    //   }
    //    return solicitacao.get().getDocumento().get(1).getNome();
    // }

    @Transactional
    @GetMapping("/setEdicaoDocumentosSolicitacao")
    public ResponseEntity setEditavel(@RequestParam long id, @RequestHeader("Authorization") String token) {
        String email = tokenService.getSubject(token.replace("Bearer ", ""));
        Optional<Usuario> user = usuarioRepository.findByEmailAndNotRoleId1(email);
        if (user.isPresent()) {
            SolicitarEstagio solicitacao = solicitacaoRepository.findById(id).get();
            return solicitacaoService.setEditavel(solicitacao,user.get().getRoles().getId());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/editarobservacaoSolicitacao")
    public ResponseEntity setObservacao(@RequestParam long id, @RequestParam String texto, @RequestHeader("Authorization") String token) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Usuario userDetails = (Usuario) authentication.getPrincipal();
        if(tokenService.isServidor(token.replace("Bearer ", ""))){
            Optional<SolicitarEstagio> so = solicitacaoRepository.findById(id);
            return solicitacaoService.modificarObservacao(so.get(),texto, userDetails.getRoles().getId());
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/editarEmpresaSolicitacao")
    public ResponseEntity editarEmpresa(@RequestParam long id, @RequestBody DadosAtualizacaoSolicitacao empresa, @RequestHeader("Authorization") String token) {
        if (usuarioRepository.findByEmailAndNotRoleId1(tokenService.getSubject(token.replace("Bearer ", ""))).isPresent()) {
            Optional<SolicitarEstagio> solicitarEstagio = solicitacaoRepository.findById(id);
            if (solicitarEstagio.isPresent()) {
                solicitarEstagio.get().atualizarDadosEmpresa(empresa);
                solicitacaoRepository.save(solicitarEstagio.get());
                historicoSolicitacao.mudarSolicitacao(solicitarEstagio.get(), "Dados da solicitação foram alterados.");
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/editarEtapa")
    public ResponseEntity setEtapa(@RequestParam long id, @RequestParam String etapa, @RequestHeader("Authorization") String token) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Usuario userDetails = (Usuario) authentication.getPrincipal();
        String email = userDetails.getEmail();
        if (usuarioRepository.findByEmailAndNotRoleId1(email).isPresent()) {
            return solicitacaoService.editarEtapa(id, etapa, userDetails.getRoles().getId());
        }else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/dadosSolicitacaoAluno")
    public ResponseEntity<List<DadosListagemSolicitacaoAluno>> obterSolicitacoes(@RequestHeader("Authorization") String token) {
        Aluno aluno = alunoRepository.findByUsuarioSistemaEmail(tokenService.getSubject(token.replace("Bearer ", "")));
        return ResponseEntity.ok(solicitacaoRepository.findByAluno_Id(aluno.getId()).stream()
                .map(DadosListagemSolicitacaoAluno::new)
                .toList());
    }

    @GetMapping("/listarSolicitacoes")
    public ResponseEntity<List<SolicitarEstagio>> listarSolicitacoes() {
        List<SolicitarEstagio> solicitacoes = solicitacaoRepository.findAll();
        return solicitacoes.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(solicitacoes);
    }

    //@GetMapping("/adicionarRespostaSolicitacao")
    //public ResponseEntity adicionarResposta(@RequestParam long id, @RequestParam String resposta, @RequestHeader("Authorization") String token) {
    //    String email = tokenService.getSubject(token.replace("Bearer ", ""));
    //    if (usuarioRepository.findByEmailAndNotRoleId1(email).isPresent()) {
    //        historicoSolicitacao.salvarHistoricoSolicitacaoId(id, usuarioRepository.findRoleIdByEmail(email), "Aluno resolveu a pendência.");
    //        solicitacaoRepository.atualizarResposta(id, resposta);
    //        return ResponseEntity.ok().build();
    //    }
    //    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    //}


    @GetMapping("/listarSolicitacoesPorEmailServidor")
    public ResponseEntity<List<DadosListagemSolicitacaoServidor>> listarSolicitacoesPorEmailServidor(@RequestHeader("Authorization") String token) {
        String email = tokenService.getSubject(token.replace("Bearer ", ""));
        var servidor = servidorRepository.findByUsuarioSistemaEmail(email);
        if (servidor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<SolicitarEstagio> publisolicitacoes = solicitacaoService.obterSolicitacoesDoServidor(servidor);
        List<DadosListagemSolicitacaoServidor> dadosSolicitacoes = publisolicitacoes.stream()
                .map(DadosListagemSolicitacaoServidor::new)
                .toList();
        return ResponseEntity.ok(dadosSolicitacoes);
    }

    @GetMapping("/alunoSolicitacao/{id}")
    public ResponseEntity<DadosListagemSolicitacaoAluno> getAlunoSolicitacao(@PathVariable("id") Long id, @RequestHeader("Authorization") String token) {
        String email = tokenService.getSubject(token.replace("Bearer ", ""));
        var servidor = servidorRepository.findByUsuarioSistemaEmail(email);
        if (servidor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<SolicitarEstagio> solicitacao = solicitacaoRepository.findById(id);
        if (solicitacao.isPresent()) {
            DadosListagemSolicitacaoAluno dadosSolicitacao = new DadosListagemSolicitacaoAluno(solicitacao.get());
            if (solicitacao.get().isNova()) {
                solicitacao.get().setStatus("Em análise");
                solicitacao.get().setEtapa("2");
                solicitacao.get().setEditavel(false);
                solicitacaoRepository.save(solicitacao.get());
            }
            if(solicitacao.get().isRespondido()){
                solicitacao.get().setStatus("Em análise");
                solicitacao.get().setEditavel(false);
                historicoSolicitacao.mudarSolicitacao(solicitacao.get(), "Resposta visualizada.");
                solicitacaoRepository.save(solicitacao.get());
            }
            return ResponseEntity.ok(dadosSolicitacao);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/trocarValidadeContrato")
    public ResponseEntity trocarDataContrato(@RequestParam Long id, @RequestParam String dataFinalNova, @RequestParam String dataInicioNova, @RequestHeader("Authorization") String token) {
        String email = tokenService.getSubject(token.replace("Bearer ", ""));
        var servidor = servidorRepository.findByUsuarioSistemaEmail(email);
        if (servidor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<SolicitarEstagio> solicitacao = solicitacaoRepository.findById(id);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        if (solicitacao.isPresent()) {
            solicitacao.get().setFinalDataEstagio(LocalDate.parse(dataFinalNova, formatter));
            solicitacao.get().setInicioDataEstagio(LocalDate.parse(dataInicioNova, formatter));
            solicitacaoRepository.save(solicitacao.get());
            historicoSolicitacao.mudarSolicitacao(solicitacao.get(), "Data de início e término do estágio foram alteradas.");
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/solicitacao/{id}")
    public ResponseEntity getSolicitacaoById(@PathVariable("id") Long id, @RequestHeader("Authorization") String token) {
        String email = tokenService.getSubject(token.replace("Bearer ", ""));
        var servidor = servidorRepository.findByUsuarioSistemaEmail(email);
        if (servidor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<SolicitarEstagio> solicitacao = solicitacaoRepository.findById(id);
        if (solicitacao.isPresent()) {
            DadosListagemSolicitacaoAluno dadosSolicitacao = new DadosListagemSolicitacaoAluno(solicitacao.get());
            if (solicitacao.get().isNova()) {
                solicitacao.get().setStatus("Em análise");
                solicitacao.get().setEtapa("2");
                solicitacao.get().setEditavel(false);
                solicitacaoRepository.save(solicitacao.get());
            }
            if(solicitacao.get().isRespondido()){
                solicitacao.get().setStatus("Em análise");
                solicitacao.get().setEditavel(false);
                historicoSolicitacao.mudarSolicitacao(solicitacao.get(), "Resposta visualizada.");
                solicitacaoRepository.save(solicitacao.get());
            }
            return ResponseEntity.ok(dadosSolicitacao);
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
                                                          @RequestParam(value = "file", required = false) List<MultipartFile> files,
                                                          @RequestHeader("Authorization") String token) {
        Optional<SolicitarEstagio> solicitacaoOptional = solicitacaoRepository.findById(id);
        if (tokenService.isServidor(token.replace("Bearer ", ""))) {
            String email = tokenService.getSubject(token.replace("Bearer ", ""));
            Servidor servidor = servidorRepository.findByUsuarioSistemaEmail(email);
            if (solicitacaoOptional.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return solicitacaoService.deferirSolicitacao(solicitacaoOptional.get(), servidor, files);
        }
        solicitacaoService.trocarProcessamento(solicitacaoOptional.get());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Você não tem permissão");
    }

    @PutMapping("/indeferirSolicitacao/{id}")
    @Transactional
    public ResponseEntity indeferirSolicitacao(@PathVariable("id") Long id, @RequestBody DadosAtualizacaoSolicitacao dados, @RequestHeader("Authorization") String token) {
        String email = tokenService.getSubject(token.replace("Bearer ", ""));
        Servidor servidor = servidorRepository.findByUsuarioSistemaEmail(email);
        if (servidor.getRole().getId() == 1) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Você não tem permissão para indeferir essa solicitação.");
        }
        return solicitacaoService.indeferirSolicitacao(id, servidor, dados);
    }

    @PostMapping("/salvarRelatorioFinal")
    public ResponseEntity salvarRelatorioFinal(@RequestPart String id, @RequestParam("file") List<MultipartFile> arquivos, @RequestHeader("Authorization") String token) {
        String email = tokenService.getSubject(token.replace("Bearer ", ""));
        Aluno aluno = alunoRepository.findByUsuarioSistemaEmail(email);
        if (aluno == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        SolicitarEstagio solicitacao = solicitacaoRepository.findById(Long.parseLong(id)).get();
        if (!Objects.equals(solicitacao.getAluno().getId(), aluno.getId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if(solicitacao.isRelatorioEntregue()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Relatório final já foi entregue.");
        }
        return solicitacaoService.salvarRelatorioFinal(solicitacao, arquivos);
    }

    @PostMapping("/setProcessando")
    public ResponseEntity setProcessando(@RequestParam long id, @RequestHeader("Authorization") String token) {
        String email = tokenService.getSubject(token.replace("Bearer ", ""));
        Servidor servidor = servidorRepository.findByUsuarioSistemaEmail(email);
        if (servidor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return solicitacaoService.setProcessando(id);
    }

    @PostMapping("/cancelarEstagio")
    public ResponseEntity cancelarEstagio(@RequestParam long id, @RequestParam("file") List<MultipartFile> arquivos, @RequestHeader("Authorization") String token) {
        String email = tokenService.getSubject(token.replace("Bearer ", ""));
        Aluno aluno = alunoRepository.findByUsuarioSistemaEmail(email);
        if (aluno == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        SolicitarEstagio solicitacao = solicitacaoRepository.findById(id).get();
        if (solicitacao.getAluno().getId() != aluno.getId()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if(solicitacao.isCancelamento()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Estágio já foi solicitado o cancelamento.");
        }
        return solicitacaoService.cancelarEstagio(solicitacao, arquivos);
    }
}
