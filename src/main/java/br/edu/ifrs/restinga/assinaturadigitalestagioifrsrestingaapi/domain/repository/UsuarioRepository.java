package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.repository;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Usuario;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;


public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    UserDetails findByEmail(String email);

    Usuario findUsuarioByEmail(String email);

    @Query(value = "SELECT * FROM usuarios  WHERE usuarios.email = :email  AND usuarios.roles_id <> 1", nativeQuery = true)
    Optional<Usuario> findByEmailAndNotRoleId1(String email);

    boolean existsByEmail(String email);

    @Query(value = "SELECT * FROM usuarios  WHERE usuarios.email = :email", nativeQuery = true)
    Optional<Usuario> findByEmailUser(String email);
}
