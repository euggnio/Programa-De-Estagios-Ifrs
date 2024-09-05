package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.controller;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.repository.TokenRepository;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.Autenticacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosAutenticacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosAutenticacaoGoogle;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosDetalhamentoAluno;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.file.GoogleEmail;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.file.GoogleUtil;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.infra.ConfigProperties;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.infra.error.TratadorDeErros;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.infra.security.TokenService;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.*;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.http.WebSocket;
import java.util.Objects;
import java.util.Optional;


@RestController
@RequestMapping("/login")
@CrossOrigin(origins = "*")
public class AutenticacaoController extends BaseController {
    @Autowired
    private ConfigProperties configProperties;
    @Autowired
    private AuthenticationManager manager;

    @Autowired
    private TokenRepository tokenRepository;

    //Service para cadastro?
    @PostMapping
    public ResponseEntity efetuarLogin(@RequestBody @Valid DadosAutenticacao dados){
        Usuario role = (Usuario) usuarioRepository.findByEmail(dados.email());
        String nomeUsuario = "";
        if(role == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if(role.getRoles().getId() != 1){
           Servidor servidor = servidorRepository.findByUsuarioSistemaEmail(role.getEmail());
            if(servidor == null){
                nomeUsuario = "Servidor";
            }
        }
        else{
            nomeUsuario = alunoRepository.findByUsuarioSistemaEmail(role.getEmail()).getNomeCompleto();
        }
        var authenticationToken = new UsernamePasswordAuthenticationToken(dados.email(),dados.senha());
        var authenticacao = manager.authenticate(authenticationToken);
        var tokenJWT = tokenService.gerarToken((Usuario) authenticacao.getPrincipal());
        Autenticacao response = new Autenticacao(tokenJWT,role.getRoles().getName(), nomeUsuario);
        System.out.println(role.getEmail());
        return ResponseEntity.ok(response);
    }



    @PostMapping("/recuperarSenha")
    public ResponseEntity recuperarSenha(@RequestBody String email){
        if(usuarioRepository.existsByEmail(email)){
            var tokenJWT = new Token(tokenService.gerarTokenTempo(email,10));
            tokenRepository.save(tokenJWT);
            try {
                GoogleEmail.sendMail(email, "Recuperação de senha","SISTEMA" ,"""
                        <html>
                            <body style='font-family: Arial, sans-serif;'>
                            <h2 style='color: #3498db;'>Recuperação de Senha</h2>
                                <p>Olá,</p>
                                <p>Seu token para a recuperação de senha se encontra abaixo:</p>
                                 <p style='background-color: #ecf0f1; padding: 10px; font-size: 18px; font-weight: bold;'>""" + tokenJWT.getToken() +  "</p>" + """ 
                                <p>Utilize o token no sistema para efetuar o cadastro de uma nova senha em até 10 minutos.</p>
                            </body>
                        </html>""");
                return ResponseEntity.ok().build();
            } catch (Exception e) {
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/validarToken")
    public ResponseEntity validarToken(@RequestBody String token){
        String email = this.tokenService.getSubject(token);
        if(email != null){
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/trocarSenha/{token}")
    public ResponseEntity trocarSenha(@RequestBody String senha, @PathVariable String token){
        String email = this.tokenService.getSubject(token);
        Optional<Usuario> user = usuarioRepository.findByEmailUser(email);
        if(user.isPresent()){
            user.get().setSenha(new BCryptPasswordEncoder().encode(senha));
            usuarioRepository.save(user.get());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/google")
    public ResponseEntity efetuarLoginGoogle(@RequestBody String token){
        DadosAutenticacaoGoogle dados = GoogleUtil.verificarToken(token);
        if(Objects.nonNull(dados)) {
            if (usuarioRepository.existsByEmail(dados.email())) {
                System.out.println(dados.email());
                Usuario role = (Usuario) usuarioRepository.findByEmail(dados.email());
                var tokenJWT = tokenService.gerarToken(role);
                System.out.println(role.getRoles().getName());
                Autenticacao response = new Autenticacao(tokenJWT, role.getRoles().getName(), role.getEmail());
                return ResponseEntity.ok(response);
            }
            else
            {
                Optional<Role> roles = roleRepository.findById(1L);
                var aluno = new Aluno(dados, roles.get());
                if (usuarioRepository.findByEmail(dados.email()) != null) {
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
                var authenticationToken = new UsernamePasswordAuthenticationToken(dados.email(), dados.sub());
                var authenticacao = manager.authenticate(authenticationToken);
                var tokenJWT = tokenService.gerarToken((Usuario) authenticacao.getPrincipal());
                Autenticacao response = new Autenticacao(tokenJWT, aluno.getRole().getName(), aluno.getNomeCompleto());
                return ResponseEntity.ok(response);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

}
