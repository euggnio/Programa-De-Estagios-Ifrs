package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.repository;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Servidor;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServidorRepository extends JpaRepository<Servidor,Long> {

    boolean existsServidorByCurso_IdEquals(long id);
    Optional<Servidor>  findServidorByCurso_Id(long id);
    Servidor findByUsuarioSistemaEmail(String email);

    Optional<Servidor> findServidorByUsuarioSistema(Usuario usuario);

    void deleteServidorByUsuarioSistema(Usuario usuario);

}
