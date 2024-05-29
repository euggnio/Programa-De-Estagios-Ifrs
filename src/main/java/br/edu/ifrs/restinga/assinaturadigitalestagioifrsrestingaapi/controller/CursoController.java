package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.controller;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.repository.CursoRepository;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.repository.ServidorRepository;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.infra.security.TokenService;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Curso;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class CursoController {

    @Autowired
    CursoRepository cursoRepository;
    @Autowired
    TokenService tokenService;
    @Autowired
    ServidorRepository servidorRepository;

    @GetMapping("/cursos")
    public ResponseEntity cursos(){
            List<Curso> cursos = cursoRepository.findAll();
            cursos.removeIf(curso -> curso.getNomeCurso().equalsIgnoreCase("diretor") || curso.getNomeCurso().contains("Estágio"));
            return ResponseEntity.ok(cursos);

    }

    @PutMapping("/cursos/trocarStatus/{id}")
    public ResponseEntity trocarStatus(@PathVariable Long id, @RequestHeader("Authorization") String token){
        String email = tokenService.getSubject(token.replace("Bearer ", ""));
        if(servidorRepository.existsServidorByUsuarioSistemaEmail(email)) {
            Optional<Curso> curso = cursoRepository.findById(id);
            if(curso.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            curso.get().setAtivo(!curso.get().isAtivo());
            cursoRepository.save(curso.get());
            return ResponseEntity.ok().build();
        }
        else{
            return ResponseEntity.status(403).build();
        }
    }

    @PostMapping("/cadastrarCurso")
    public ResponseEntity cadastrarCurso(@RequestBody Curso curso, @RequestHeader("Authorization") String token){
        String email = tokenService.getSubject(token.replace("Bearer ", ""));
        if(servidorRepository.existsServidorByUsuarioSistemaEmail(email)) {
            if(cursoRepository.findByNomeCurso(curso.getNomeCurso()).isPresent()) {
                return ResponseEntity.badRequest().build();
            }
            cursoRepository.save(curso);
            return ResponseEntity.ok().build();
        }
        else{
            return ResponseEntity.status(403).build();
        }
    }

    @DeleteMapping("/deletarCurso")
    public ResponseEntity deletarCurso(@RequestParam long curso, @RequestHeader("Authorization") String token){
        String email = tokenService.getSubject(token.replace("Bearer ", ""));
        if(servidorRepository.existsServidorByUsuarioSistemaEmail(email)) {
            if(cursoRepository.findById(curso).isPresent()) {
                return ResponseEntity.badRequest().build();
            }
            cursoRepository.deleteById(curso);
            return ResponseEntity.ok().build();
        }
        else{
            return ResponseEntity.status(403).build();
        }
    }

}
