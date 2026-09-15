package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.ImplClasses;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.controller.BaseController;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadoDetalhamentoServidor;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadoUpdateServidor;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosCadastroServidor;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosDetalhamentoAluno;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.infra.error.TratadorDeErros;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Curso;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Role;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Usuario;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Servidor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;


@Service
public class ServidorImplementacao extends BaseController {
	
	@PersistenceContext	
	EntityManager manager;

	@Value("${cargo.coordenador}")
	private String cargoCoordenador;

	@Value("${cargo.setor-estagio}")
	private String cargoSetorEstagio;

	@Value("${cargo.diretor}")
	private String cargoDiretor;

	@Value("${role.id.coordenador}")
	private long roleIdCoordenador;

	@Value("${role.id.setor-estagio}")
	private long roleIdSetorEstagio;

	@Value("${role.id.diretor}")
	private long roleIdDiretor;

	String[] cargo;

	public Servidor findId(long id) {
		// TODO Auto-generated method stub
		return manager.find(Servidor.class, id);
	}

	@Transactional
	public ResponseEntity salvar(DadosCadastroServidor dadosCadastroServidor, Curso curso, UriComponentsBuilder uriBuilder) {
		Optional<Role> role = Optional.empty();
		if(dadosCadastroServidor.cargo().equals(cargoCoordenador)){
			role = roleRepository.findById(roleIdCoordenador);
		}else if(dadosCadastroServidor.cargo().equals(cargoSetorEstagio)) {
			role = roleRepository.findById(roleIdSetorEstagio);
		} else if (dadosCadastroServidor.cargo().equalsIgnoreCase(cargoDiretor)) {
			role = roleRepository.findById(roleIdDiretor);
		}
		var servidor = new Servidor(dadosCadastroServidor, curso,role.get());

		if(usuarioRepository.findByEmail(dadosCadastroServidor.usuarioSistema().getEmail())!= null){
			return TratadorDeErros.tratarErro409("email");
		}


		if (!emailValidator.validaEmail(servidor.getUsuarioSistema().getEmail())) {
			return TratadorDeErros.tratarErro400(HttpStatus.BAD_REQUEST);
		}


		var usuarioSistemaServidor = new Usuario(
				dadosCadastroServidor.usuarioSistema().getEmail(),
				passwordEncoder.encode(dadosCadastroServidor.usuarioSistema().getSenha()),
				servidor.getRole()
		);

		servidor.setUsuarioSistema(usuarioSistemaServidor);
		historicoCursoService.salvarCadastroServidor(curso, servidor.getNome());
		usuarioRepository.save(servidor.getUsuarioSistema());
		servidorRepository.save(servidor);
		var uri = uriBuilder.path("/servidor/{id}").buildAndExpand(servidor.getId()).toUri();
	    return ResponseEntity.created(uri).body(servidor);
	}


	public List<Servidor> listar() {
		return manager.createQuery("from Servidor", Servidor.class).getResultList();
	}
	
	@Transactional
	public void remover(Servidor servidor) {
		// TODO Auto-generated method stub
		manager.remove(servidor);
	}


	@Transactional
	@SuppressWarnings("unchecked")
	public ResponseEntity atualizaServidor(DadoUpdateServidor dados, Curso curso, String email) {

		if(!dados.usuarioSistema().getEmail().equals(email)){
			return TratadorDeErros.tratarErro403();
		}
		String sql = "SELECT s.id FROM servidores s INNER JOIN usuarios u ON s.usuario_sistema_id = u.id WHERE u.email = :email";
		Query query = manager.createNativeQuery(sql, Integer.class);
		query.setParameter("email", email);
		int ID = (int) query.getSingleResult();

		var servidor = servidorRepository.getReferenceById((long) ID);


		servidor.atualizarInformacoes(dados, curso);

		return ResponseEntity.ok(new DadoDetalhamentoServidor(servidor));
	}

}
