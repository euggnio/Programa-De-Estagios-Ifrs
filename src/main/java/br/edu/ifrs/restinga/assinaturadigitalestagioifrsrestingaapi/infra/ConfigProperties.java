package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.infra;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sistema")
@Data
public class ConfigProperties {
    //todo isso é uma classe feita para testes. Não é para ser usada em produção e não tem funcionalidade
    private final String emailEstagio;

}
