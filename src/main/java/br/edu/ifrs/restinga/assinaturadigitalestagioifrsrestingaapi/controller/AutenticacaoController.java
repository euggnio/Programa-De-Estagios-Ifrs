package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.controller;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.Autenticacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosAutenticacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosAutenticacaoGoogle;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosDetalhamentoAluno;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.infra.error.TratadorDeErros;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.infra.security.TokenService;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Aluno;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Role;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Usuario;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;


@RestController
@RequestMapping("/login")
public class AutenticacaoController extends BaseController {

    @Autowired
    private AuthenticationManager manager;

    @Autowired
    private TokenService tokenService;

    @PostMapping
    public ResponseEntity efetuarLogin(@RequestBody @Valid DadosAutenticacao dados){
        Usuario role =(Usuario) usuarioRepository.findByEmail(dados.email());
        var authenticationToken = new UsernamePasswordAuthenticationToken(dados.email(),dados.senha());
        var authenticacao = manager.authenticate(authenticationToken);
        var tokenJWT = tokenService.gerarToken((Usuario) authenticacao.getPrincipal());

        Autenticacao response = new Autenticacao(tokenJWT,role.getRoles().getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/google")
    public ResponseEntity efetuarLoginGoogle(@RequestBody DadosAutenticacaoGoogle dados, UriComponentsBuilder uriBuilder){
        System.out.println("DATA  " + dados.email());
        System.out.println("Data:" +dados.toString() + " ");

        if(usuarioRepository.existsByEmail(dados.email())){
            Usuario role =(Usuario) usuarioRepository.findByEmail(dados.email());
            var authenticationToken = new UsernamePasswordAuthenticationToken(dados.email(),dados.sub());
            var authenticacao = manager.authenticate(authenticationToken);
            var tokenJWT = tokenService.gerarToken((Usuario) authenticacao.getPrincipal());

            Autenticacao response = new Autenticacao(tokenJWT,role.getRoles().getName());
            return ResponseEntity.ok(response);
        }
        else{
            System.out.println("AQUI");
            //buscando curso por ID para salvar no aluno pelo construtor.
            Optional<Role> roles = roleRepository.findById(1L);
            var aluno = new Aluno(dados,roles.get());
            if(usuarioRepository.findByEmail(dados.email())!= null){
                return TratadorDeErros.tratarErro409("email");
            }
            var usuarioSistema = new Usuario(
                    dados.email(),
                    roles.get(),
                    passwordEncoder.encode(dados.sub())

            );
            aluno.setUsuarioSistema(usuarioSistema);

            usuarioRepository.save(aluno.getUsuarioSistema());
            alunoRepository.save(aluno);

            System.out.println("ALUNO CADASTRADO PELO GOOGLE");

            var uri = uriBuilder.path("/alunos/{id}").buildAndExpand(aluno.getId()).toUri();
            var authenticationToken = new UsernamePasswordAuthenticationToken(dados.email(), dados.sub());
            var authenticacao = manager.authenticate(authenticationToken);
            var tokenJWT = tokenService.gerarToken((Usuario) authenticacao.getPrincipal());
            Autenticacao response = new Autenticacao(tokenJWT,aluno.getRole().getName());

            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/googleLogin")
    public ResponseEntity LoginGoogle(@RequestBody DadosAutenticacaoGoogle dados){
        System.out.println("DATA aawdawda " + dados.email());


        return ResponseEntity.ok("ok :D");
    }

}
