package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.ImplClasses;

import java.time.LocalDateTime;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.repository.SolicitacaoRepository;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.repository.HistoricoSolicitacaoRepository;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Historico;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.SolicitarEstagio;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PreRemove;
import jakarta.transaction.Transactional;

@Service
public class HistoricoSolicitacao {

    @Autowired
    HistoricoSolicitacaoRepository historicoSolicitacaoRepository;
    @Autowired
    SolicitacaoRepository solicitacaoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public void mudarSolicitacao(SolicitarEstagio solicitarEstagio, String situacao){
        Historico log = new Historico(LocalDateTime.now(),solicitarEstagio.getEtapa(),situacao,solicitarEstagio);
        historicoSolicitacaoRepository.save(log);
    }

    @Transactional
    public void salvarHistoricoSolicitacaoId(Long solicitarEstagio,long servidorRole, String situacao){
        solicitacaoRepository.findById(solicitarEstagio).ifPresent(solicitarEstagio1 -> {
            Historico log = new Historico(LocalDateTime.now(),getEtapa(servidorRole),situacao,solicitarEstagio1);
            historicoSolicitacaoRepository.save(log);
        });
    }

    public String getEtapa(Long id){
        if (id == 2) {
            return "3";
        } else if (id == 3) {
            return "2";
        } else if (id == 4) {
            return "4";
        }
        return "1";
    }

}
