package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.service;


import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.controller.BaseController;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Servidor;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.SolicitarEstagio;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Locale;
import java.util.Optional;

@Service
public class SolicitacaoService extends BaseController {

    public boolean verificarSolicitacaoExistente(long alunoId, String tipoSolicitacao){
        int quantidade = solicitacaoRepository.countByAluno_IdAndTipoAndStatusNotContainingIgnoreCase(alunoId,tipoSolicitacao,"Indeferido");
        System.out.println("Quantidade: " + quantidade+ " tipo " + tipoSolicitacao);
        return (quantidade >= 1) ? true: false;
    }



    @Transactional
    public ResponseEntity atualizarStatus(long idSolicitacao, String status, Servidor servidor){
        Optional<SolicitarEstagio> solicitarEstagio = solicitacaoRepository.findById(idSolicitacao);
        if(solicitarEstagio.isPresent() && servidor != null){
            switch (status.toLowerCase()) {
                case "indeferido":
                    setStatusIndeferido(solicitarEstagio.get(),servidor);
                    break;
                case "deferido":
                    setDeferidoStatus(solicitarEstagio.get(),servidor);
                    break;
                case "em andamento":
                    setEmAndamentoStatus(solicitarEstagio.get(),servidor);
                    break;
                default:
                    solicitacaoRepository.atualizarStataus(solicitarEstagio.get().getId(), status);
                    break;
            }
            solicitacaoRepository.save(solicitarEstagio.get());
            return ResponseEntity.ok("Sucesso");
          }
          return ResponseEntity.notFound().build();
        }


    public void setStatusIndeferido(SolicitarEstagio solicitarEstagio, Servidor servidor){
        solicitarEstagio.setStatus("Indeferido");
        if (servidor.getRole().getName().contains("ESTAGIO")) {
            solicitarEstagio.setStatusSetorEstagio("Indeferido");
        } else if (servidor.getRole().getName().contains("SERVIDOR")) {
            solicitarEstagio.setStatusEtapaCoordenador("Indeferido");
        } else {
            solicitarEstagio.setStatusEtapaDiretor("Indeferido");
        }
    }

    private void setDeferidoStatus(SolicitarEstagio solicitarEstagio, Servidor servidor) {
        solicitarEstagio.setStatus("Deferido");
        if (servidor.getRole().getName().contains("ESTAGIO")) {
            solicitarEstagio.setStatusSetorEstagio("Deferido");
        } else if (servidor.getRole().getName().contains("SERVIDOR")) {
            solicitarEstagio.setStatusEtapaCoordenador("Deferido");
        } else {
            solicitarEstagio.setStatusEtapaDiretor("Deferido");
        }
    }
    private void setEmAndamentoStatus(SolicitarEstagio solicitarEstagio, Servidor servidor) {
        solicitarEstagio.setStatus("Em andamento");
       if (servidor.getRole().getName().contains("ESTAGIO")) {
            solicitarEstagio.setStatusSetorEstagio("");
        } else if (servidor.getRole().getName().contains("SERVIDOR")) {
            solicitarEstagio.setStatusEtapaCoordenador("");
        } else {
            solicitarEstagio.setStatusEtapaDiretor("");}
        }
}
