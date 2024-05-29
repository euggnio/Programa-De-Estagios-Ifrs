package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.controller;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosAtualizacaoSolicitacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.file.PdfGenerator;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Estagiarios;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Servidor;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.SolicitarEstagio;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Usuario;
import com.lowagie.text.Document;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RestController
public class EstagiariosController extends BaseController {

    @GetMapping("/retornarListaEstagiarios")
    public ResponseEntity listaEstagiarios(@RequestHeader("Authorization") String token) {
        if (tokenService.isServidor(token.replace("Bearer ", ""))) {
            try {
                return ResponseEntity.ok(estagiariosRepository.findAll());
            } catch (Exception e) {
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping()
    public ResponseEntity buscarPorNome(@RequestHeader("Authorization") String token) {
        if (tokenService.isServidor(token.replace("Bearer ", ""))) {
            try {
                return ResponseEntity.ok(estagiariosRepository.findAll());
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/retornarEstagiarioDrivePorSolicitacaoId")
    public ResponseEntity<String> retornarEstagiarioPorSolicitacaoId(@RequestParam Long id, @RequestHeader("Authorization") String token) {
        System.out.println(token);
        System.out.println("id: " + id);
        if (tokenService.isAluno(token.replace("Bearer ", ""))) {
            try {
                Estagiarios estagiario = estagiariosRepository.findBySolicitacaoId(id);
                if (estagiario != null) {
                        return ResponseEntity.ok("{\"urlDrive\" : \"" + estagiario.getUrlPastaDocumentos() +"\"}");
                }
                else {
                    return ResponseEntity.notFound().build();
                }
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/retornarListaEstagiariosPagina")
    public ResponseEntity listaEstagiariosPagina(@RequestParam int pagina, @RequestHeader("Authorization") String token) {
        if (tokenService.isServidor(token.replace("Bearer ", ""))) {
            try {
                List<Estagiarios> estagiarios = estagiariosRepository.pegarPagina(pagina * 20);
                estagiarios.sort((est1, est2) -> {
                    String nome1 = est1.getSolicitacao().getAluno().getNomeCompleto();
                    String nome2 = est2.getSolicitacao().getAluno().getNomeCompleto();
                    return nome1.compareTo(nome2);
                });
                return ResponseEntity.ok(estagiarios);
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/retornarEstagioEstagiario")
    public ResponseEntity retornarEstagioEstagiario(@RequestParam Long id, @RequestHeader("Authorization") String token) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Usuario userDetails = (Usuario) authentication.getPrincipal();
        if (userDetails.getRoles().getName().equals("ROLE_ALUNO")) {
            try {
                return ResponseEntity.ok(estagiariosRepository.pegarEstagioPorAluno(id));
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/retornarEstagiarioMatricula")
    public ResponseEntity retornarEstagiarioMatricula(@RequestParam String matricula, @RequestHeader("Authorization") String token) {
        if (tokenService.isServidor(token.replace("Bearer ", ""))) {
            try {
                List<Estagiarios> estagiarios = estagiariosRepository.findByAlunoMatricula(matricula);
                return ResponseEntity.ok(estagiarios);
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PutMapping("/cancelarEstagio")
    public ResponseEntity<String> cancelarEstagio(@RequestBody String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Usuario userDetails = (Usuario) authentication.getPrincipal();
        if (!userDetails.getRoles().getName().equals("ROLE_ALUNO")) {
            try {
                var estagio = estagiariosRepository.findById(Long.valueOf(id)).get();
                estagio.setAtivo(false);
                estagiariosRepository.save(estagio);
                return ResponseEntity.ok().build();
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PutMapping("/atualizarEstagio")
    public ResponseEntity atualizarEstagio(@RequestBody DadosAtualizacaoSolicitacao estagiario, @RequestHeader("Authorization") String token) {
        if (tokenService.isServidor(token.replace("Bearer ", ""))) {
            assert estagiario.id() != null;
            Optional<SolicitarEstagio> solicitacaoOp = solicitacaoRepository.findById(estagiario.id());
            SolicitarEstagio solicitacao = solicitacaoOp.get();
            solicitacao.setTurnoEstagio(estagiario.turnoEstagio());
            solicitacao.setSalario(estagiario.salario());
            solicitacao.setCargaHoraria(estagiario.cargaHoraria());
            solicitacao.setContatoEmpresa(estagiario.contatoEmpresa());
            solicitacao.setNomeEmpresa(estagiario.nomeEmpresa());
            if(estagiario.eprivada() == null) {
                solicitacao.setEPrivada(false);
            }else {
                solicitacao.setEPrivada(estagiario.eprivada());
            }
            try {
                solicitacaoRepository.save(solicitacao);
                return ResponseEntity.ok().build();
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/pegarPdfEstagiarios")
    public ResponseEntity<Document> pegarPdfEstagiarios(@RequestHeader("Authorization") String token , HttpServletResponse response) {
        if (tokenService.isServidor(token.replace("Bearer ", ""))) {
            try {
                List<Servidor> servidores = servidorRepository.findServidoresNotInEstagioOrDiretor();
                List<Estagiarios> estagiarios = estagiariosRepository.findAll();
                estagiarios.sort((est1, est2) -> {
                    String nome1 = est1.getSolicitacao().getAluno().getNomeCompleto();
                    String nome2 = est2.getSolicitacao().getAluno().getNomeCompleto();
                    return nome1.compareTo(nome2);
                });

                PdfGenerator pdfGenerator = new PdfGenerator(estagiarios,servidores);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDispositionFormData("filename", "estagiarios.pdf");

                Document document = pdfGenerator.pegarPdfEstagiarios(response);
                return ResponseEntity.ok().body(document);
            } catch (RuntimeException e) {
                System.out.println(e);
                return ResponseEntity.badRequest().build();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
