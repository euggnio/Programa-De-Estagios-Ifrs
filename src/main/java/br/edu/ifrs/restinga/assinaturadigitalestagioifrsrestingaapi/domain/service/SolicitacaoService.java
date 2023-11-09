package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.service;


import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.controller.BaseController;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.SolicitarEstagio;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SolicitacaoService extends BaseController {

    public boolean verificarSolicitacaoExistente(long alunoId, String tipoSolicitacao){
        int quantidade = solicitacaoRepository.countByAluno_IdAndTipo(alunoId,tipoSolicitacao);
        System.out.println("Quantidade: " + quantidade+ " tipo " + tipoSolicitacao);
        return (quantidade >= 1) ? true: false;
    }


}
