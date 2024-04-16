package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.controller;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosAtualizacaoSolicitacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Estagiarios;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.SolicitarEstagio;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
//Limitar para servidores!!!!
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

    @GetMapping("/retornarListaEstagiariosPagina")
    public ResponseEntity listaEstagiariosPagina(@RequestParam int pagina, @RequestHeader("Authorization") String token) {
        if (tokenService.isServidor(token.replace("Bearer ", ""))) {
            try {
                return ResponseEntity.ok(estagiariosRepository.pegarPagina(pagina * 20));
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
    public ResponseEntity cancelarEstagio(@RequestBody String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Usuario userDetails = (Usuario) authentication.getPrincipal();
        if (!userDetails.getRoles().getName().equals("ROLE_ALUNO")) {
            try {
                var estagio = estagiariosRepository.findById(Long.valueOf(id)).get();
                if(estagio.getSolicitacao().getAluno().getId() != userDetails.getId()){
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                }
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
            SolicitarEstagio solicitacao = solicitacaoRepository.findById(estagiario.id()).get();
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
}
