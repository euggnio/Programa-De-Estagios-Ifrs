package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.repository.CursoRepository;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.*;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.infra.error.TratadorDeErros;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Curso;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Role;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Aluno;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@RestController
public class AlunoController extends BaseController {
    @Autowired
    CursoRepository cursoRepository;

    @PostMapping("/cadastrarAluno")
    @Transactional
    public ResponseEntity cadastrarAluno(@RequestBody @Valid DadosCadastroAluno dados, UriComponentsBuilder uriBuilder) {
        Optional<Curso> curso = cursoRepository.findById(dados.curso());
        Optional<Role> role = roleRepository.findById(1L);
        var aluno = new Aluno(dados, curso.get(), role.get());

        if (usuarioRepository.findByEmail(dados.usuarioSistema().getEmail()) != null) {
            return TratadorDeErros.tratarErro409("email");
        }
         if (!emailValidator.validaEmail(aluno.getUsuarioSistema().getEmail())) {
             return TratadorDeErros.tratarErro400(HttpStatus.BAD_REQUEST);
        }
        var usuarioSistema = new Usuario(
                dados.usuarioSistema().getEmail(),
                passwordEncoder.encode(dados.usuarioSistema().getSenha()),
                aluno.getRole()
        );
        aluno.setUsuarioSistema(usuarioSistema);
        usuarioRepository.save(aluno.getUsuarioSistema());
        alunoRepository.save(aluno);

        //spring cria a URI no metodo passa o complemento da url  passa o id do novo aluno .toURI cria objeto URI
        var uri = uriBuilder.path("/alunos/{id}").buildAndExpand(aluno.getId()).toUri();
        return ResponseEntity.created(uri).body(new DadosDetalhamentoAluno(aluno));
}

    @PostMapping("/cadastrarAlunos")
    @Transactional
    public ResponseEntity cadastrarAlunos(@RequestBody @Valid List<DadosCadastroAluno> dados, UriComponentsBuilder uriBuilder) {
        List<Aluno> alunos = new ArrayList<>();
        List<Usuario> usuariosSistema = new ArrayList<>();
        Optional<Role> role = roleRepository.findById(1L);
        for (DadosCadastroAluno dado : dados) {
            Optional<Curso> curso = cursoRepository.findById(dado.curso());


            var aluno = new Aluno(dado, curso.get(), role.get());
            alunos.add(aluno);

            var usuarioSistema = new Usuario(
                    dado.usuarioSistema().getEmail(),
                    passwordEncoder.encode(dado.usuarioSistema().getSenha()),
                    aluno.getRole()
            );
            usuariosSistema.add(usuarioSistema);
        }

        usuarioRepository.saveAll(usuariosSistema);
        alunoRepository.saveAll(alunos);

        List<DadosDetalhamentoAluno> dadosDetalhados = new ArrayList<>();
        for (Aluno aluno : alunos) {
            dadosDetalhados.add(new DadosDetalhamentoAluno(aluno));
        }

        return ResponseEntity.created(uriBuilder.path("/alunos").build().toUri()).body(dadosDetalhados);
    }


    @PostMapping("/atualizarAluno")
    @Transactional
    public ResponseEntity atualizarAluno(@RequestBody @Valid DadosAtualizacaoAluno dados, @RequestHeader("Authorization") String token){
        String email = tokenService.getSubject(token.replace("Bearer ", ""));
        Aluno aluno = alunoRepository.findByUsuarioSistemaEmail(email);
        if(aluno != null){
            if(alunoRepository.existsByMatricula(dados.matricula())){
                if(!alunoRepository.validarMatriculaPorEmail(dados.matricula(), email)){
                    return ResponseEntity.badRequest().body("Essa matrícula já se encontra cadastrada!");
                }
            }
            Optional<Curso> curso = cursoRepository.findById(dados.curso());
            if (curso.isPresent()) {
                if(dados.usuarioSistema().getSenha().equals("") || dados.usuarioSistema().getSenha().isBlank()){
                    aluno.atualizarInformacoes(dados, curso.get());
                    alunoRepository.save(aluno);
                    return ResponseEntity.ok(new DadosDetalhamentoAluno(aluno));
                }
                else {
                    String novaSenha = passwordEncoder.encode(dados.usuarioSistema().getSenha());
                    aluno.getUsuarioSistema().setSenha(novaSenha);
                    aluno.atualizarInformacoes(dados, curso.get());
                    usuarioRepository.save(aluno.getUsuarioSistema());
                    alunoRepository.save(aluno);
                    return ResponseEntity.ok(new DadosDetalhamentoAluno(aluno));
                }
            }
        }
        return ResponseEntity.badRequest().body("Erro ao atualizar aluno, confira os campos!");
    }

    @GetMapping("alunos/buscarAlunos")
    public ResponseEntity<List<DadosListagemAluno>> buscarAlunos() {
        var lista = alunoRepository.findAll().stream().map(DadosListagemAluno::new).toList();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/getAluno")
    public ResponseEntity pegarAluno(@RequestHeader("Authorization") String token) {
        String email = tokenService.getSubject(token.replace("Bearer ", ""));
        Aluno aluno = alunoRepository.findByUsuarioSistemaEmail(email);
        if (alunoRepository.findById(aluno.getId()).isPresent()) {
            return ResponseEntity.ok(alunoRepository.findById(aluno.getId()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/buscarAlunoPorIdSolicitacao/{idSolicitacao}")
    @ResponseBody
    public ResponseEntity listarIdSolicitacao(@PathVariable long idSolicitacao) {
        var aluno = alunoRepository.findById(idSolicitacao);
        return ResponseEntity.ok(aluno);
    }

}
