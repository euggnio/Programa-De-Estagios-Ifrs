package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.repository;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Curso;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<Token, Long> {

    boolean existsByToken(String token);

}
