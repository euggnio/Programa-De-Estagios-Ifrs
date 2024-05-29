package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto;

import java.time.LocalDateTime;
import java.util.List;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Aluno;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Curso;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Historico;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.SolicitarEstagio;

public record DadosListagemSolicitacaoServidor(Long id,Boolean cancelamento, Boolean relatorioEntregue, Curso curso, String status, String tipo, Boolean editavel, String etapa, String observacao, LocalDateTime dataSolicitacao, Aluno aluno, List<Historico> historico) {
    public DadosListagemSolicitacaoServidor(SolicitarEstagio solicitarEstagio){
        this(solicitarEstagio.getId(),
            solicitarEstagio.isCancelamento(),
            solicitarEstagio.isRelatorioEntregue(),
            solicitarEstagio.getCurso(),
            solicitarEstagio.getStatus(),
            solicitarEstagio.getTipo(),
            solicitarEstagio.isEditavel(),
            solicitarEstagio.getEtapa(),
            solicitarEstagio.getObservacao(),
            solicitarEstagio.getDataSolicitacao(),
            solicitarEstagio.getAluno(),
            solicitarEstagio.getHistorico());
    } 
}
