package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.repository;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Aluno;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Curso;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.SolicitarEstagio;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

@Repository
public interface SolicitacaoRepository  extends JpaRepository<SolicitarEstagio,Long> {
    List<SolicitarEstagio> findByAluno(Aluno aluno);
//    List<SolicitarEstagio> findByServidor(Servidor servidor);

    int countByAluno_IdAndTipoAndStatusNotContainingIgnoreCase(Long aluno, String tipo,String status);

    boolean existsByAluno_IdAndTipoAndStatusNotContainingIgnoreCase(Long aluno, String tipo,String status);

    List<SolicitarEstagio> findAllByEtapaIsGreaterThanEqualAndStatusNotContaining(String etapa, String deferido);
    List<SolicitarEstagio> findByCursoAndEtapaIsGreaterThanEqualAndStatusNotContainingIgnoreCase(Curso curso, String etapa, String deferido);
    List<SolicitarEstagio> findByCursoAndEtapaEqualsAndStatusNotContainingIgnoreCase(Curso curso, String etapa, String deferido);
    @Transactional
    @Modifying
    @Query( value = " UPDATE solicitar_estagio s SET s.editavel = CASE WHEN s.editavel = false THEN true ELSE s.editavel = false END WHERE s.id = :solicitacaoId", nativeQuery = true)
    void atualizarEditavelParaTrue(Long solicitacaoId);

    @Transactional
    @Modifying
    @Query( value = "UPDATE solicitar_estagio s SET s.observacao =:texto WHERE id = :solicitacaoId", nativeQuery = true)
    void atualizarObservacao(Long solicitacaoId, String texto);

    @Transactional
    @Modifying
    @Query( value = "UPDATE solicitar_estagio s SET s.contato_empresa =:novoContato,  WHERE id = :solicitacaoId", nativeQuery = true)
    void atualizarEmpresa(Long solicitacaoId, String novoContato);

    @Transactional
    @Modifying
    @Query( value = "UPDATE solicitar_estagio s SET s.status =:texto WHERE id = :solicitacaoId", nativeQuery = true)
    void atualizarStataus(Long solicitacaoId, String texto);

    @Transactional
    @Modifying
    @Query(value = "UPDATE solicitar_estagio s SET s.etapa = :novaEtapa, s.status =:novoStatus WHERE id = :solicitacaoId", nativeQuery = true)
    void atualizarEtapa(Long solicitacaoId, String novaEtapa, String novoStatus);



}
