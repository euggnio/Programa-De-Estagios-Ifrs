package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.infra.ConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableConfigurationProperties({ConfigProperties.class})
@Configuration
public class AssinaturaDigitalEstagioIfrsRestingaApiApplication implements WebMvcConfigurer {

	public static void main(String[] args) {
		SpringApplication.run(AssinaturaDigitalEstagioIfrsRestingaApiApplication.class, args);
	}
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**").allowedMethods("*");
	}
}
