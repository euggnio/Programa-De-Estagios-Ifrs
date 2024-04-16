package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.controller;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.ImplClasses.FileImp;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.ImplClasses.HistoricoSolicitacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.repository.*;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.infra.security.TokenService;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BaseController {
    @Autowired
    public AlunoRepository alunoRepository;

    @Autowired
    public SolicitacaoRepository solicitacaoRepository;

    @Autowired
    public HistoricoSolicitacao historicoSolicitacao;

    @Autowired
    public UsuarioRepository usuarioRepository;

    @Autowired
    public EstagiariosRepository estagiariosRepository;

    @Autowired
    protected
    PasswordEncoder passwordEncoder;

    @Autowired
    TokenService tokenService;

    @Autowired
    public DocumentoRepository documentoRepository;

    @Autowired
    public CursoRepository cursoRepository;

    @Autowired
    protected
    EmailValidator emailValidator;

    @Autowired
    public ServidorRepository servidorRepository;

    @Autowired
    public RoleRepository roleRepository;

}
