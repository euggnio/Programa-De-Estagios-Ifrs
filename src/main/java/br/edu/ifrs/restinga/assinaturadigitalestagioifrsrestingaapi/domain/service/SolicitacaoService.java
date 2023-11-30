package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.service;


import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.ImplClasses.FileImp;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.ImplClasses.HistoricoSolicitacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.controller.BaseController;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosCadastroSolicitacao;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Aluno;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Curso;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Servidor;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.SolicitarEstagio;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class SolicitacaoService extends BaseController {

    @Autowired
    FileImp fileImp;

    @Autowired
    private HistoricoSolicitacao historicoSolicitacao;

    @Transactional
    public ResponseEntity cadastrarSolicitacao(DadosCadastroSolicitacao dados, List<MultipartFile> arquivos) {
        Optional<Aluno> aluno = alunoRepository.findById(dados.alunoId());
        Optional<Curso> curso = cursoRepository.findById(dados.cursoId());

        if (aluno.isEmpty() || curso.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        SolicitarEstagio solicitacao = criarSolicitacao(dados, curso.get(), aluno.get());
        fileImp.SaveDocBlob(arquivos, solicitacao, false);
        saveSolicitacao(solicitacao);

        return ResponseEntity.ok().build();
    }

    private SolicitarEstagio criarSolicitacao(DadosCadastroSolicitacao dados, Curso curso, Aluno aluno) {
        return new SolicitarEstagio(aluno, curso, dados.tipo(), dados.titulo(), dados.conteudo(), dados.observacao(), "Nova", "1", true);
    }

    private void saveSolicitacao(SolicitarEstagio solicitacao) {
        solicitacaoRepository.save(solicitacao);
        historicoSolicitacao.mudarSolicitacao(solicitacao, "Cadastrado");
    }

    public boolean verificarSolicitacaoExistente(long alunoId, String tipoSolicitacao) {
        int quantidade = solicitacaoRepository.countByAluno_IdAndTipoAndStatusNotContainingIgnoreCase(alunoId, tipoSolicitacao, "Indeferido");
        return (quantidade >= 5) ? true : false;
    }


    @Transactional
    public ResponseEntity atualizarStatus(long idSolicitacao, String status, Servidor servidor) {
        Optional<SolicitarEstagio> solicitarEstagio = solicitacaoRepository.findById(idSolicitacao);
        if (solicitarEstagio.isPresent() && servidor != null) {
            switch (status.toLowerCase()) {
                case "indeferido":
                    setStatusIndeferido(solicitarEstagio.get(), servidor);
                    break;
                case "deferido":
                    setDeferidoStatus(solicitarEstagio.get(), servidor);
                    break;
                case "em andamento":
                    setEmAndamentoStatus(solicitarEstagio.get(), servidor);
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


    public void setStatusIndeferido(SolicitarEstagio solicitarEstagio, Servidor servidor) {
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
            solicitarEstagio.setStatusEtapaDiretor("");
        }
    }
}
