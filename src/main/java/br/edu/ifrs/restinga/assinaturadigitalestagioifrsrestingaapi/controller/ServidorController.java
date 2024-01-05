package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.controller;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.ImplClasses.ServidorImplementacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.repository.CursoRepository;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadoUpdateServidor;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosCadastroServidor;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.infra.error.TratadorDeErros;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.infra.security.TokenService;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Curso;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Servidor;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Usuario;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;


@RestController
@ResponseBody
public class ServidorController extends BaseController {
    @Autowired
    private ServidorImplementacao servidorImplementacao;
    @Autowired
    TokenService tokenService;
    @Autowired
    CursoRepository cursoRepository;

    @PostMapping("/cadastrarServidor")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity salvar(@RequestBody @Valid DadosCadastroServidor dadosCadastroServidor, UriComponentsBuilder uriBuilder) {
        Optional<Curso> curso = cursoRepository.findById(dadosCadastroServidor.curso().getId());
        System.out.println("PASSOY");

        if (curso.isEmpty()) {
            return servidorImplementacao.salvar(dadosCadastroServidor, null, uriBuilder);
        }
        if (servidorRepository.existsServidorByCurso_IdEquals(curso.get().getId()) && dadosCadastroServidor.curso().getId() != 15) {
            return TratadorDeErros.tratarErro409("curso");
        }
        return servidorImplementacao.salvar(dadosCadastroServidor, curso.get(), uriBuilder);

    }

    @GetMapping("/buscarServidor/{id}")
    public ResponseEntity buscarServidrorId(@PathVariable Long id) {
        var servidor = servidorImplementacao.findId(id);
        return ResponseEntity.ok(servidor);
    }

    @PostMapping("/buscarOrientadorCurso")
        public ResponseEntity buscarOrientadorCurso(@RequestBody Long cursoId){
        var servidor = servidorRepository.findServidorByCurso_Id(cursoId);
        return ResponseEntity.ok(servidor);
    }

    @GetMapping("/excluirServidor/{id}")
    @Transactional
    public ResponseEntity excluirServidor(@PathVariable Long id) {
            Optional<Usuario> user = usuarioRepository.findById(id);
            if(user.isPresent()){
                servidorRepository.deleteServidorByUsuarioSistema(user.get());
                usuarioRepository.deleteById(id);
                return ResponseEntity.ok().build();
            }
            else {
                return ResponseEntity.badRequest().build();
            }
    }

    @GetMapping("/listarServidores")
    public ResponseEntity listarServidores() {
        var servidor = servidorImplementacao.listar();
        return ResponseEntity.ok(servidor);
    }

    @PutMapping("/atualizar")
    public ResponseEntity atualizar(@RequestBody DadoUpdateServidor dadosCadastroServidor, @RequestHeader("Authorization") String token) {
        Optional<Curso> curso = cursoRepository.findById(dadosCadastroServidor.curso());
        var email = tokenService.getSubject(token.replace("Bearer ", ""));
        return servidorImplementacao.atualizaServidor(dadosCadastroServidor, curso.get(), email);
    }

    @GetMapping("/buscarServidoresPorEmail/{email}")
    public ResponseEntity buscarServidorPorEmail(@PathVariable String email) {
        var servidores = servidorImplementacao.listar();
        // Percorrer a lista de servidores e encontrar o servidor com o email correspondente
        Servidor servidorEncontrado = null;
        for (Servidor servidor : servidores) {
            if (servidor.getUsuarioSistema().getEmail().equals(email)) {
                servidorEncontrado = servidor;
                break;
            }
        }
        if (servidorEncontrado != null) {
            return ResponseEntity.ok(servidorEncontrado);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
