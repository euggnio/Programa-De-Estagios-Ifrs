package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.infra;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sistema")
@Data
public class ConfigProperties {

    private final String emailEstagio;

}
