package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.ImplClasses.HistoricoSolicitacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.controller.DocumentoController;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.service.SalvarDocumentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;

@SpringBootApplication
public class AssinaturaDigitalEstagioIfrsRestingaApiApplication {

	HistoricoSolicitacao historico;
	public static void main(String[] args) {
		SpringApplication.run(AssinaturaDigitalEstagioIfrsRestingaApiApplication.class, args);



	}



}
