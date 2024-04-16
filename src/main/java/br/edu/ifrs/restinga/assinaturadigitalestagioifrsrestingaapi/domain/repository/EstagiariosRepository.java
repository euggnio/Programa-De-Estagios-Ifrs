package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.repository;


import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Estagiarios;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EstagiariosRepository extends JpaRepository<Estagiarios, Long> {

    @Query(value = "SELECT * FROM estagiarios LIMIT 20 OFFSET :pagina", nativeQuery = true)
    List<Estagiarios> pegarPagina(int pagina);

    //SELECT * FROM `estagiarios` e INNER JOIN `solicitar_estagio`  ON e.solicitacao_id = solicitar_estagio.id WHERE solicitar_estagio.final_data_estagio > NOW() AND solicitar_estagio.aluno_id = 2;
    @Query(value = "SELECT e.* FROM `estagiarios` e INNER JOIN `solicitar_estagio` se ON e.solicitacao_id = se.id WHERE e.ativo = true and se.final_data_estagio > NOW() AND se.aluno_id = :id", nativeQuery = true)
    List<Estagiarios> pegarEstagioPorAluno(Long id);

    @Query(value = "SELECT * FROM estagiarios WHERE solicitacao_id IN (SELECT id FROM solicitar_estagio WHERE aluno_id IN (SELECT id FROM aluno WHERE matricula = :matricula))", nativeQuery = true)
    List<Estagiarios> findByAlunoMatricula(String matricula);

    Estagiarios findBySolicitacaoId(Long id);
    @Transactional
    @Modifying
    @Query(value = "UPDATE estagiarios SET ativo = :ativo WHERE solicitacao_id = :id", nativeQuery = true)
    void updateAtivo(Long id, Boolean ativo);

    Boolean existsBySolicitacaoId(Long id);

}
