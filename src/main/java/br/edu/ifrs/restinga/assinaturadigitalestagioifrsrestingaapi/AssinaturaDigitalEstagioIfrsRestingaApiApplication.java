package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.ImplClasses.HistoricoSolicitacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.controller.DocumentoController;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.service.SalvarDocumentoService;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.infra.ConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;

@SpringBootApplication
@EnableConfigurationProperties({ConfigProperties.class})
public class AssinaturaDigitalEstagioIfrsRestingaApiApplication {

	HistoricoSolicitacao historico;
	public static void main(String[] args) {


		SpringApplication.run(AssinaturaDigitalEstagioIfrsRestingaApiApplication.class, args);



	}



}
