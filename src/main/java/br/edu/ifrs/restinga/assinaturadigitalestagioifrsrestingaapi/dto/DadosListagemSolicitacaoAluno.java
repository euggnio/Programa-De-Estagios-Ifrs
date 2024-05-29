package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Aluno;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Historico;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.SolicitarEstagio;
import jakarta.annotation.Nullable;

import javax.validation.constraints.Null;

public record DadosListagemSolicitacaoAluno(Long id, String status, String tipo,
                                            String etapa, boolean editavel, boolean cancelamento, String observacao,
                                            LocalDateTime dataSolicitacao, Aluno aluno, List<Historico> historico,
                                            LocalDate finalDataEstagio, LocalDate inicioDataEstagio, String agente,
                                            String nomeEmpresa, String contatoEmpresa, boolean ePrivada
                                            , String cargaHoraria, String salario, String turnoEstagio, boolean relatorioEntregue) {

    public DadosListagemSolicitacaoAluno(SolicitarEstagio solicitarEstagio) {
        this(solicitarEstagio.getId()
                , solicitarEstagio.getStatus()
                , solicitarEstagio.getTipo()
                , solicitarEstagio.getEtapa()
                , solicitarEstagio.isEditavel()
                , solicitarEstagio.isCancelamento()
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
                , solicitarEstagio.getCargaHoraria()
                , solicitarEstagio.getSalario()
                , solicitarEstagio.getTurnoEstagio()
                , solicitarEstagio.isRelatorioEntregue()
        );
    }

}