package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.controller;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.ImplClasses.HistoricoSolicitacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.ImplClasses.FileImp;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.service.SolicitacaoService;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosAtualizacaoSolicitacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosCadastroSolicitacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosListagemSolicitacaoAluno;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosListagemSolicitacaoServidor;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.file.GoogleEmail;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.*;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.strategy.EmailStrategy;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.strategy.ObservacaoEmailStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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

    @Transactional
    @GetMapping("/setEdicaoDocumentosSolicitacao")
    public ResponseEntity setEditavel(@RequestParam long id, @RequestHeader("Authorization") String token) {
        //Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //Usuario userDetails = (Usuario) authentication.getPrincipal();
        //System.out.println("TESTE 4: " + (userDetails.getRoles().getName()).equals("ROLE_SESTAGIO"));

        String email = tokenService.getSubject(token.replace("Bearer ", ""));
        if (usuarioRepository.findByEmailAndNotRoleId1(email).isPresent()) {
            String statusAtual = solicitacaoRepository.verificarEditavel(id);
            historicoSolicitacao.salvarHistoricoSolicitacaoId(id, usuarioRepository.findRoleIdByEmail(email), "Edição de documentos foi " + statusAtual);
            solicitacaoRepository.atualizarEditavelParaTrue(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/editarobservacaoSolicitacao")
    public ResponseEntity setObservacao(@RequestParam long id, @RequestParam String texto, @RequestHeader("Authorization") String token) {
        String email = tokenService.getSubject(token.replace("Bearer ", ""));
        if (usuarioRepository.findByEmailAndNotRoleId1(email).isPresent()) {
            SolicitarEstagio so = solicitacaoRepository.findById(id).get();
            historicoSolicitacao.salvarHistoricoSolicitacaoId(id, usuarioRepository.findRoleIdByEmail(email), "Observação para edição: " + texto);
            EmailStrategy emailStrategy = new ObservacaoEmailStrategy();
            try {
                GoogleEmail.sendMail(so.getAluno().getUsuarioSistema().getEmail()
                        ,emailStrategy.getTitle()
                        ,emailStrategy.getTitle()
                        ,emailStrategy.getBody(so, texto));
            } catch (Exception e) {
                throw new RuntimeException("Erro ao enviar email");
            }
            solicitacaoRepository.atualizarObservacao(id, texto);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/editarEmpresaSolicitacao")
    public ResponseEntity editarEmpresa(@RequestParam long id, @RequestBody DadosAtualizacaoSolicitacao empresa, @RequestHeader("Authorization") String token) {
        if (usuarioRepository.findByEmailAndNotRoleId1(tokenService.getSubject(token.replace("Bearer ", ""))).isPresent()) {
            System.out.println(empresa.toString());
            Optional<SolicitarEstagio> solicitarEstagio = solicitacaoRepository.findById(id);
            if (solicitarEstagio.isPresent()) {
                solicitarEstagio.get().setNomeEmpresa(empresa.nomeEmpresa());
                solicitarEstagio.get().setAgente(empresa.agente());
                solicitarEstagio.get().setContatoEmpresa(empresa.contatoEmpresa());
                solicitarEstagio.get().setEPrivada(empresa.eprivada());
                solicitarEstagio.get().setCargaHoraria(empresa.cargaHoraria());
                solicitarEstagio.get().setSalario(empresa.salario());
                solicitarEstagio.get().setTurnoEstagio(empresa.turnoEstagio());

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

    @GetMapping("/editarEtapa")
    public ResponseEntity setEtapa(@RequestParam long id, @RequestParam String etapa, @RequestHeader("Authorization") String token) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Usuario userDetails = (Usuario) authentication.getPrincipal();
        String email = userDetails.getEmail();
        UserDetails usuario = usuarioRepository.findByEmail(email);
        if (usuarioRepository.findByEmailAndNotRoleId1(email).isPresent()) {
            return solicitacaoService.editarEtapa(id, etapa, userDetails.getRoles().getId());
        }else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
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

    @GetMapping("/adicionarRespostaSolicitacao")
    public ResponseEntity adicionarResposta(@RequestParam long id, @RequestParam String resposta, @RequestHeader("Authorization") String token) {
        String email = tokenService.getSubject(token.replace("Bearer ", ""));
        if (usuarioRepository.findByEmailAndNotRoleId1(email).isPresent()) {
            historicoSolicitacao.salvarHistoricoSolicitacaoId(id, usuarioRepository.findRoleIdByEmail(email), "Aluno resolveu a pendência.");
            solicitacaoRepository.atualizarResposta(id, resposta);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }


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
            if (solicitacao.get().getStatus().equals("Nova")) {
                solicitacao.get().setStatus("Em análise");
                solicitacao.get().setEtapa("2");
                solicitacao.get().setEditavel(false);
                solicitacaoRepository.save(solicitacao.get());
            } if(solicitacao.get().getStatus().equalsIgnoreCase("respondido")){
                solicitacao.get().setStatus("Em análise");
                solicitacao.get().setEditavel(false);
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
        String email = tokenService.getSubject(token.replace("Bearer ", ""));
        Servidor servidor = servidorRepository.findByUsuarioSistemaEmail(email);
        Optional<SolicitarEstagio> solicitacaoOptional = solicitacaoRepository.findById(id);
        if (solicitacaoOptional.isEmpty() || servidor == null) {
            return ResponseEntity.notFound().build();
        }

        return solicitacaoService.deferirSolicitacao(solicitacaoOptional.get(), servidor, files);
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
        if (solicitacao.getAluno().getId() != aluno.getId()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if(solicitacao.isRelatorioEntregue()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Relatório final já foi entregue.");
        }
        return solicitacaoService.salvarRelatorioFinal(solicitacao, arquivos);
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

        return solicitacaoService.cancelarEstagio(solicitacao, arquivos);
    }
}
