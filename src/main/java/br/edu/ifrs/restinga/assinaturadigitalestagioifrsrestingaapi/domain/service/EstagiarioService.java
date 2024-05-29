package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.service;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.repository.EstagiariosRepository;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Estagiarios;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.SolicitarEstagio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EstagiarioService {

    @Autowired
    private EstagiariosRepository estagiarioRepository;


    public void desativarEstagiario(Long id) {
        Estagiarios estagiario = estagiarioRepository.findBySolicitacaoId(id);
        if(estagiario == null) {
            return;
        }
        estagiario.setAtivo(false);
        estagiarioRepository.save(estagiario);
    }

    public void salvarEstagiario(SolicitarEstagio solicitacao, String url) {
        if(existeEstagiario(solicitacao.getId())) {
            return;
        }
        Estagiarios estagiario = new Estagiarios(solicitacao,url);
        estagiarioRepository.save(estagiario);
    }

    private boolean existeEstagiario(Long id) {
        return estagiarioRepository.existsBySolicitacaoId(id);
    }

}
