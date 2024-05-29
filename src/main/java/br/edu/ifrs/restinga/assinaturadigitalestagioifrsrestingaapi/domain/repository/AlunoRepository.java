package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Aluno;
import org.springframework.data.jpa.repository.Query;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {
    Aluno findByUsuarioSistemaEmail(String email);
    void deleteByUsuarioSistemaEmail(String email);
    boolean existsByMatricula(String matricula);
    boolean existsAlunoByUsuarioSistemaEmail(String email);

    @Query(value = "SELECT CASE WHEN EXISTS ( SELECT 1 FROM aluno WHERE aluno.matricula = :matricula " +
            "AND (SELECT COUNT(*) FROM usuarios WHERE usuarios.email = :email AND usuarios.id = aluno.usuario_sistema_id) > 0) " +
            "THEN 'True' ELSE 'False' END AS valido;", nativeQuery = true)
    Boolean validarMatriculaPorEmail(String matricula, String email);
}
