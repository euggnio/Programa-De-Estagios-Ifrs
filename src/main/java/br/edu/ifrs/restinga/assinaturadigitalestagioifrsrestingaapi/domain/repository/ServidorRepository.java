package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.repository;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Servidor;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServidorRepository extends JpaRepository<Servidor, Long> {

    boolean existsServidorByCurso_IdEquals(long id);

    Optional<Servidor> findServidorByCurso_Id(long id);

    @Query(value = "SELECT * from servidores WHERE servidores.curso_id != 15 and servidores.curso_id != 16",
            nativeQuery = true)
    List<Servidor> findServidoresNotInEstagioOrDiretor();

    Servidor findByUsuarioSistemaEmail(String email);

    boolean existsServidorByUsuarioSistemaEmail(String usuario);

    @Query(value = "SELECT COUNT(*) FROM servidor s WHERE s.role_id = 1", nativeQuery = true)
    public boolean existeServidorEstagio();

    int  countByRole_Id(long id);



    void deleteServidorByUsuarioSistema(Usuario usuario);

}
