package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.repository;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Aluno;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Curso;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.SolicitarEstagio;

import java.util.List;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

@Repository
public interface SolicitacaoRepository extends JpaRepository<SolicitarEstagio, Long> {
    List<SolicitarEstagio> findByAluno_Id(Long aluno);

    List<SolicitarEstagio> findAllByEtapaIsGreaterThanEqual(String etapa);

    List<SolicitarEstagio> findByCursoAndEtapaIsGreaterThanEqualAndStatusNotContainingIgnoreCase(Curso curso, String etapa, String Aprovado);

    void deleteAllByAluno_UsuarioSistema(Usuario usuarioSistema);

    List<SolicitarEstagio> findAllByAluno_UsuarioSistema(Usuario usuarioSistema);

    @Transactional
    @Modifying
    @Query(value = "UPDATE solicitar_estagio s SET s.observacao =:texto WHERE id = :solicitacaoId", nativeQuery = true)
    void atualizarObservacao(Long solicitacaoId, String texto);

    @Transactional
    @Modifying
    @Query(value = "UPDATE solicitar_estagio s SET s.etapa = :novaEtapa, s.observacao = '', s.editavel = false, s.status =:novoStatus WHERE id = :solicitacaoId", nativeQuery = true)
    void atualizarEtapa(Long solicitacaoId, String novaEtapa, String novoStatus);



}
