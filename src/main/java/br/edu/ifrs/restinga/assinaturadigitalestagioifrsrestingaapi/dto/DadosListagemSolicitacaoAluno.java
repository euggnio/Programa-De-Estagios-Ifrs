package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Aluno;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Historico;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.SolicitarEstagio;

public record DadosListagemSolicitacaoAluno(Long id, String titulo, String conteudo, String status, String tipo,
                                            String etapa, boolean editavel, String observacao,
                                            LocalDateTime dataSolicitacao, Aluno aluno, List<Historico> historico,
                                            LocalDate finalDataEstagio, LocalDate inicioDataEstagio, String agente,
                                            String nomeEmpresa, String contatoEmpresa, boolean ePrivada) {

    public DadosListagemSolicitacaoAluno(SolicitarEstagio solicitarEstagio) {
        this(solicitarEstagio.getId()
                , solicitarEstagio.getTitulo()
                , solicitarEstagio.getConteudo()
                , solicitarEstagio.getStatus()
                , solicitarEstagio.getTipo()
                , solicitarEstagio.getEtapa()
                , solicitarEstagio.isEditavel()
                , solicitarEstagio.getObservacao()
                , solicitarEstagio.getDataSolicitacao()
                , solicitarEstagio.getAluno()
                , solicitarEstagio.getHistorico()
                , solicitarEstagio.getFinalDataEstagio()
                , solicitarEstagio.getInicioDataEstagio()
                , solicitarEstagio.getAgente()
                , solicitarEstagio.getNomeEmpresa()
                , solicitarEstagio.getContatoEmpresa()
                , solicitarEstagio.getEPrivada()
        );
    }

}