package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.controller;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.ImplClasses.HistoricoSolicitacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.ImplClasses.FileImp;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.service.SolicitacaoService;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosAtualizacaoSolicitacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosCadastroSolicitacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosListagemSolicitacaoAluno;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosListagemSolicitacaoServidor;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin("*")
public class SolicitacaoController extends BaseController{
    @Autowired
    FileImp fileImp;

    @Autowired
    private HistoricoSolicitacao historicoSolicitacao;

    @Autowired
    private SolicitacaoService solicitacaoService;

    @PostMapping(value = "/cadastrarSolicitacao")
    @Transactional
    public ResponseEntity cadastrarSolicitacao(@RequestPart("dados") DadosCadastroSolicitacao dados,
                                               @RequestParam("file") List<MultipartFile> file){
        if(solicitacaoService.verificarSolicitacaoExistente(dados.alunoId(),dados.tipo())){
            return ResponseEntity.badRequest().body("Você já possui uma solicitação deste tipo!");
        }
        else {
            Optional<Curso> curso = cursoRepository.findById(dados.cursoId());
            System.out.println("Dados slt: " + dados.alunoId());
            System.out.println("Dados slt: " + dados.cursoId());
            System.out.println("Dados slt: " + dados.tipo());
            System.out.println("Dados file: " + file.size());
            System.out.println("Dados slt: " + dados.status());
            Optional<Aluno> aluno = alunoRepository.findById(dados.alunoId());
            if (aluno.isPresent() && curso.isPresent()) {
                SolicitarEstagio solicitarEstagio = new SolicitarEstagio(aluno.get()
                        , curso.get(), dados.tipo()
                        , dados.titulo()
                        , dados.conteudo()
                        , dados.observacao()
                        , "Nova"
                        , "1"
                        , true
                        , ""
                        , ""
                        , ""
                        , "");

                fileImp.SaveDocBlob(file, solicitarEstagio, false);
                solicitacaoRepository.save(solicitarEstagio);
                historicoSolicitacao.mudarSolicitacao(solicitarEstagio, "Cadastrado");
                return ResponseEntity.ok().build();
            }
            else{
                return ResponseEntity.badRequest().build();
            }
        }
    }

    @GetMapping("/listarDocumentos")
    @ResponseBody
    public String listar(@RequestParam long id){
        Optional<SolicitarEstagio> solicitacao = solicitacaoRepository.findById(id);
        return solicitacao.get().getDocumento().get(1).getNome();
    }

    @GetMapping("/editarSolicitacao")
    public ResponseEntity setEditavel(@RequestParam long id, @RequestHeader("Authorization") String token){
        String email = tokenService.getSubject(token.replace("Bearer ", ""));
        System.out.println("EMAIL: " + email);
        solicitacaoRepository.atualizarEditavelParaTrue(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/editarobservacaoSolicitacao")
    public ResponseEntity setObservacao(@RequestParam long id, @RequestParam String texto, @RequestHeader("Authorization") String token){
        String email = tokenService.getSubject(token.replace("Bearer ", ""));
        System.out.println("EMAIL: " + email + " " + texto);
        solicitacaoRepository.atualizarObservacao(id, texto);
        return ResponseEntity.ok().build();
    }

    //arrumar isso aqui com switch case :D
    @GetMapping("/editarstatus")
    public ResponseEntity setSolicitacao(@RequestParam long id, @RequestParam String status, @RequestHeader("Authorization") String token){
        String email = tokenService.getSubject(token.replace("Bearer ", ""));
        Servidor servidor = servidorRepository.findByUsuarioSistemaEmail(email);
        solicitacaoService.atualizarStatus(id,status,servidor);
        return  ResponseEntity.ok().build();
    }


    @GetMapping("/editarEtapa")
    public ResponseEntity setEtapa(@RequestParam long id, @RequestParam String etapa, @RequestHeader("Authorization") String token){
        String email = tokenService.getSubject(token.replace("Bearer ", ""));
        System.out.println("EMAIL: " + email + " " + etapa);
        solicitacaoRepository.atualizarEtapa(id, etapa);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/dadosSolicitacaoAluno")
    public ResponseEntity<List<DadosListagemSolicitacaoAluno>> obterSolicitacoes(@RequestHeader("Authorization") String token) {
        String email = tokenService.getSubject(token.replace("Bearer ", ""));
        Aluno aluno = alunoRepository.findByUsuarioSistemaEmail(email);

        List<SolicitarEstagio> solicitacoes = solicitacaoRepository.findByAluno(aluno);
        List<DadosListagemSolicitacaoAluno> dadosSolicitacoes = solicitacoes.stream()
                .map(DadosListagemSolicitacaoAluno::new)
                .toList();

        return ResponseEntity.ok(dadosSolicitacoes);
    }

    @GetMapping("/listarSolicitacoes")
    public ResponseEntity<List<SolicitarEstagio>> listarSolicitacoes() {
        var solicitacoes = solicitacaoRepository.findAll();
        if (solicitacoes.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(solicitacoes);
        }
    }

/*    @GetMapping("/listarSolicitacoesPorServidor/{servidorId}")
    public ResponseEntity<List<SolicitarEstagio>> listarSolicitacoesPorServidor(@PathVariable("servidorId") Long servidorId) {
        var servidor = servidorRepository.findById(servidorId);
        if (servidor.isPresent()) {
            var solicitacoesPorServidor = solicitacaoRepository.findByServidor(servidor.get());
            if (solicitacoesPorServidor.isEmpty()) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.ok(solicitacoesPorServidor);
            }
        }
            return ResponseEntity.notFound().build();

    }*/

  @GetMapping("/dadosSolicitacaoTeste")
public ResponseEntity<List<SolicitarEstagio>> dadoSolicitacaoTeste() {
    List<SolicitarEstagio> solicitacoes = solicitacaoRepository.findAll();
    if (solicitacoes.isEmpty()) {
        return ResponseEntity.notFound().build(); // Se não encontrar, retorna 404 Not Found
    } else {
        return ResponseEntity.ok(solicitacoes); // Se encontrar, retorna 200 OK com a lista de entidades
    }
}


    @GetMapping("/listarSolicitacoesPorEmailServidor")
    public ResponseEntity<List<DadosListagemSolicitacaoServidor>> listarSolicitacoesPorEmailServidor(@RequestHeader("Authorization") String token) {
        String email = tokenService.getSubject(token.replace("Bearer ", ""));
        var servidor = servidorRepository.findByUsuarioSistemaEmail(email);

        List<SolicitarEstagio> solicitacoes = null;
        if(servidor.getCargo().equals("Coordenador")){
            Optional<Curso> curso = cursoRepository.findById(servidor.getCurso().getId());
            solicitacoes = solicitacaoRepository.findByCursoAndEtapaIsGreaterThanEqual(curso.get(), "2");
        }
        else if(servidor.getCargo().equals("Diretor")){
            System.out.println("GOGOGO");
            solicitacoes = solicitacaoRepository.findAllByEtapaIsGreaterThanEqual("4");
        }
        else {
            System.out.println(servidor.getRole() + " " + servidor.getCargo());
            solicitacoes = solicitacaoRepository.findAll();
        }

        List<DadosListagemSolicitacaoServidor> dadosSolicitacoes = solicitacoes.stream().map(DadosListagemSolicitacaoServidor::new).toList();
        
        return ResponseEntity.ok(dadosSolicitacoes);
    }

    @GetMapping("/alunoSolicitacao/{id}")
    public ResponseEntity<DadosListagemSolicitacaoAluno> getAlunoSolicitacao(@PathVariable("id") Long id) {
        Optional<SolicitarEstagio> solicitacao = solicitacaoRepository.findById(id);
        if (solicitacao.isPresent()) {
            DadosListagemSolicitacaoAluno dadosSolicitacao = new DadosListagemSolicitacaoAluno(solicitacao.get());
            if(solicitacao.get().getStatus().equals("Nova")){
                SolicitarEstagio sl;
                sl = solicitacao.get();
                sl.setStatus("Em andamento");
                sl.setEtapa("2");
                sl.setEditavel(false);
                solicitacaoRepository.save(sl);
            }
            return ResponseEntity.ok(dadosSolicitacao);
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
                    ,solicitacao.getStatus()
                    ,solicitacao.getEtapa()
                    ,solicitacao.getStatusSetorEstagio()
                    ,solicitacao.getStatusEtapaCoordenador()
                    ,solicitacao.isEditavel()
                    ,solicitacao.getStatusEtapaDiretor()
                    ,solicitacao.getObservacao());

            solicitacaoDTO.statusEtapaDiretor();
            solicitacaoDTO.statusEtapaCoordenador();
            solicitacaoDTO.etapa();
            solicitacaoDTO.editavel();
            solicitacaoDTO.statusEtapaSetorEstagio();
            solicitacaoDTO.observacao();
            solicitacaoDTO.status();

            return ResponseEntity.ok(solicitacaoDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/documentosSolicitacao/{id}")
    public ResponseEntity<List<Documento>> getDocumentosSolicitacao(@PathVariable("id") Long id) {
        Optional<SolicitarEstagio> solicitacao = solicitacaoRepository.findById(id);
        if (solicitacao.isPresent()) {
            return ResponseEntity.ok(solicitacao.get().getDocumento());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/deferirSolicitacao/{id}")
    @Transactional
    public ResponseEntity deferirSolicitacao(@PathVariable("id") Long id,

                                             @RequestPart("dados") DadosAtualizacaoSolicitacao dados,
                                             @RequestParam(value = "file", required = false) List<MultipartFile> files,
                                             @RequestHeader("Authorization") String token) {

                                                System.out.println("---------------------");
                                                System.out.println("aqui o erro");

        String email = tokenService.getSubject(token.replace("Bearer ", ""));
        Servidor servidor = servidorRepository.findByUsuarioSistemaEmail(email);
        Optional<SolicitarEstagio> solicitacaoOptional = solicitacaoRepository.findById(id);
        String deferido = "Deferido";

        if (!solicitacaoOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        SolicitarEstagio solicitacao = solicitacaoOptional.get();

        if (solicitacao.getStatus().equals("Indeferido")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Não é possível deferir uma solicitação indeferida.");
        }

        if(solicitacao.getEtapa().equals("5")){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Está solicitação já foi concluida como deferida ela não pode mais ser deferida.");
        }

        // se estiver na etapa 1 e não for role 3  role 3 = setor
        if (solicitacao.getEtapa().equals("2") && !( servidor.getRole().getId() == 3))  {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas o setor de estágios pode deferir uma solicitação na etapa 1.");
        }

        // se estiver na etapa 2 e não for role 2 role 2  = coordenador
        if (solicitacao.getEtapa().equals("3") && !(servidor.getRole().getId() == 2)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas o coordenador pode deferir uma solicitação na etapa 2.");
        }

       // se estiver na etapa 3 e não for role 4 role 4  = diretor
        if (solicitacao.getEtapa().equals("4") && !( servidor.getRole().getId() == 4 )) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas o diretor pode deferir uma solicitação na etapa 3.");
        }

        if (servidor.getRole().getId() == 3) {
            solicitacao.setStatusSetorEstagio(dados.statusEtapaSetorEstagio()); // status setor estagio
            historicoSolicitacao.mudarSolicitacao(solicitacao, deferido);
            solicitacao.setEtapa("3");
            solicitacao.setStatusSetorEstagio("Deferido");
            solicitacao.setStatusEtapaCoordenador("Em Andamento");
            historicoSolicitacao.mudarSolicitacao(solicitacao, deferido);
        }

        if (servidor.getRole().getId() == 2) {
            solicitacao.setStatusEtapaCoordenador(dados.statusEtapaCoordenador()); // status coordenador
            solicitacao.setEtapa("4");
            solicitacao.setStatusEtapaDiretor("Em Andamento");
            historicoSolicitacao.mudarSolicitacao(solicitacao, deferido);
        }

        if (servidor.getRole().getId() == 4) {
            solicitacao.setEtapa("5"); // Diretor
            solicitacao.setStatusEtapaDiretor(dados.statusEtapaDiretor()); // definir status
            solicitacao.setStatus(dados.status()); // Definir o status como "DEFERIDO"
            historicoSolicitacao.mudarSolicitacao(solicitacao, deferido);
        }


        if (files != null && !files.isEmpty()) {
            fileImp.SaveDocBlob(files, solicitacao, true);
        }
        solicitacao.setEditavel(false);
        solicitacao.setObservacao("");
        solicitacaoRepository.save(solicitacao);
        //historicoSolicitacao.mudarSolicitacao(solicitacao);
        return ResponseEntity.ok().build();
    }


    @PutMapping("/indeferirSolicitacao/{id}")
    @Transactional
    public ResponseEntity indeferirSolicitacao(@PathVariable("id") Long id, @RequestBody DadosAtualizacaoSolicitacao dados, @RequestHeader("Authorization") String token) {

        System.out.println("---------------------");
        System.out.println("aqui o erro");



        String email = tokenService.getSubject(token.replace("Bearer ", ""));
        Servidor servidor = servidorRepository.findByUsuarioSistemaEmail(email);
        String indeferido = "Indeferido";

        // Verificar se o servidor tem permissão para indeferir solicitações
        if (!(servidor.getRole().getId() == 2 || servidor.getRole().getId() == 3 || servidor.getRole().getId() == 4)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas pessoas do setor de estágios, diretores ou coordenadores podem indeferir uma solicitação.");
        }


        Optional<SolicitarEstagio> solicitacaoOptional = solicitacaoRepository.findById(id);

        if (solicitacaoOptional.isPresent()) {
            SolicitarEstagio solicitacao = solicitacaoOptional.get();

            if(solicitacao.getEtapa().equals("5")){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Está solicitação já foi concluida como deferida ela não pode mais ser indeferida.");
            }

            if (servidor.getRole().getId() == 3) {
                // status setor estagio
                solicitacao.setStatus("Indeferido");
                solicitacao.setStatusSetorEstagio("Indeferido");
                historicoSolicitacao.mudarSolicitacao(solicitacao, indeferido);
            }

            if (servidor.getRole().getId() == 2) {
                // status coordenador
                solicitacao.setStatus("Indeferido");
                solicitacao.setStatusEtapaCoordenador("Indeferido");
                historicoSolicitacao.mudarSolicitacao(solicitacao, indeferido);
            }

            if (servidor.getRole().getId() == 4) {
                // Diretor
                solicitacao.setStatus("Indeferido");
                solicitacao.setStatusEtapaDiretor("Indeferido");
                historicoSolicitacao.mudarSolicitacao(solicitacao, indeferido);
            }

            if (dados.observacao() != null) {
                solicitacao.setObservacao(dados.observacao());
            }
            solicitacao.setEditavel(false);
            solicitacaoRepository.save(solicitacao);
            //historicoSolicitacao.mudarSolicitacao(solicitacao);
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.notFound().build();
    }
    
}