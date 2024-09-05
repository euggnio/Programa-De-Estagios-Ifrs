package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.infra.security;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Usuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Date;

@Service
public class TokenService {


    @Value("${api.security.token.secret}")
    private String secret;

    public String gerarToken(Usuario usuario){
        try {
            Algorithm algoritimo = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("API ASSINATURA.EST.IFRS")
                    .withSubject(usuario.getEmail())
                    .withExpiresAt(LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00")))
                    .withClaim("role",usuario.getRoles().getName())
                    .sign(algoritimo);

        } catch (JWTCreationException exception){
            throw  new RuntimeException("erro ao gerar token jwt",exception);
        }
    }

    public String gerarTokenTempo(String email, long minutos){
        try {
            Algorithm algoritimo = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("API ASSINATURA.EST.IFRS")
                    .withSubject(email)
                    .withExpiresAt(LocalDateTime.now().plusMinutes(minutos).toInstant(ZoneOffset.of("-03:00")))
                    .sign(algoritimo);
        } catch (JWTCreationException exception){
            throw  new RuntimeException("erro ao gerar token jwt",exception);
        }
    }


    public String getSubject(String tokenJWT) throws JWTVerificationException{
        var algoritimo = Algorithm.HMAC256(secret);
        return JWT.require(algoritimo)
                .withIssuer("API ASSINATURA.EST.IFRS")
                .build()
                .verify(tokenJWT)
                .getSubject();
    }

    public String getRole(String tokenJWT) throws JWTVerificationException{
        var algoritimo = Algorithm.HMAC256(secret);
        return JWT.require(algoritimo)
                .withIssuer("API ASSINATURA.EST.IFRS")
                .build()
                .verify(tokenJWT)
                .getClaim("role").asString();
    }

    public  boolean isServidor(String token){
        if(token == null || token.isEmpty()){
            return false;
        }
        String role = this.getRole(token);
        return role.toLowerCase().contains("servidor")
                || role.toLowerCase().contains("sestagio")
                || role.toLowerCase().contains("diretor");
    }


    public  boolean isAluno(String token){
        if(token == null || token.isEmpty()){
            return false;
        }
        String role = this.getRole(token);
        return role.toLowerCase().contains("aluno");
    }


}
