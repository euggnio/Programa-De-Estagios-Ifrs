package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.service;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.repository.ServidorRepository;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.repository.UsuarioRepository;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.infra.security.TokenService;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Servidor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AutenticacaoService implements UserDetailsService {

    @Autowired
    private UsuarioRepository repository;
    @Autowired
    private ServidorRepository servidorRepository;
    @Autowired 
    private TokenService tokenService;


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return repository.findByEmail(email);
    }
    
    public Servidor obterServidorPorToken(String token){
        String email = tokenService.getSubject(token.replace("Bearer ", ""));
        return servidorRepository.findByUsuarioSistemaEmail(email);
    }
}
