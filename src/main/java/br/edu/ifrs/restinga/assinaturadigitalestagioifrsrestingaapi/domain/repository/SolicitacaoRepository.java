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
public interface SolicitacaoRepository extends JpaRepository<SolicitarEstagio, Long> {
    List<SolicitarEstagio> findByAluno(Aluno aluno);
    //List<SolicitarEstagio> findByServidor(Servidor servidor);

    int countByAluno_IdAndTipoAndStatusNotContainingIgnoreCaseAndStatusNotContainingIgnoreCase(Long aluno, String tipo, String status, String statusDeferido);

    List<SolicitarEstagio> findAllByEtapaIsAndStatusEqualsIgnoreCase(String etapa, String status);

    List<SolicitarEstagio> findByCursoAndEtapaEqualsAndStatusNotContainingIgnoreCase(Curso curso, String etapa, String deferido);

    @Transactional
    @Modifying
    @Query(value = "UPDATE solicitar_estagio s \n" +
            "SET s.editavel = CASE \n" +
            "                    WHEN s.editavel = false THEN true \n" +
            "                    ELSE false \n" +
            "                 END,\n" +
            "    s.status = CASE \n" +
            "                    WHEN s.editavel = true THEN 'Pendente'\n" +
            "                    ELSE 'Em análise'\n" +
            "               END\n" +
            "WHERE s.id = :solicitacaoId ;", nativeQuery = true)
    void atualizarEditavelParaTrue(Long solicitacaoId);

    @Query(value = "SELECT CASE editavel WHEN 1 THEN 'FECHADA' ELSE  'ABERTA' END AS editavel from solicitar_estagio where id = :solicitacaoId", nativeQuery = true)
    String verificarEditavel(Long solicitacaoId);

    @Transactional
    @Modifying
    @Query(value = "UPDATE solicitar_estagio s SET s.observacao =:texto WHERE id = :solicitacaoId", nativeQuery = true)
    void atualizarObservacao(Long solicitacaoId, String texto);

    //retonar true ou false verificando se a solicitação por id tem editavel = true

    @Query(value = "SELECT CASE editavel WHEN 1 THEN 'true' ELSE  'false' END AS editavel from solicitar_estagio where id = :solicitacaoId", nativeQuery = true)
    String verificarEditavelTrue(Long solicitacaoId);




    @Transactional
    @Modifying
    @Query(value = "UPDATE solicitar_estagio s SET s.contato_empresa =:novoContato,  WHERE id = :solicitacaoId", nativeQuery = true)
    void atualizarEmpresa(Long solicitacaoId, String novoContato);


    @Modifying
    @Query(value = "UPDATE solicitar_estagio s SET s.status =:texto WHERE id = :solicitacaoId", nativeQuery = true)
    void atualizarStataus(Long solicitacaoId, String texto);



    @Transactional
    @Modifying
    @Query(value = "UPDATE solicitar_estagio s SET s.etapa = :novaEtapa, s.observacao = '', s.editavel = false, s.status =:novoStatus WHERE id = :solicitacaoId", nativeQuery = true)
    void atualizarEtapa(Long solicitacaoId, String novaEtapa, String novoStatus);

    @Transactional
    @Modifying
    @Query(value = "UPDATE solicitar_estagio s SET s.resposta=:texto WHERE id = :solicitacaoId", nativeQuery = true)
    void atualizarResposta(Long solicitacaoId, String texto);


}
