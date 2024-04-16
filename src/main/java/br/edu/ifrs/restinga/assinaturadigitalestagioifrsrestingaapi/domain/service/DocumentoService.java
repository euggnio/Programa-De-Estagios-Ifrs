package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.service;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.ImplClasses.FileImp;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.controller.BaseController;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Documento;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.SolicitarEstagio;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;

@Service
public class DocumentoService extends BaseController {
    @Autowired
    public FileImp fileImp;
    @Transactional
    public ResponseEntity salvarDocumentosSolicitacao(SolicitarEstagio solicitacao, List<MultipartFile> documentos, String solicitante){
        if(servidorRepository.existsServidorByUsuarioSistemaEmail(solicitante)){
            historicoSolicitacao.mudarSolicitacao(solicitacao,"Adicionou um total de " + documentos.size() + " documentos");
            fileImp.SaveDocBlob(documentos,solicitacao,true);
            return ResponseEntity.ok().build();
        }
        else if(validarAlunoEEditavel(solicitante,solicitacao)){
                solucionarPendente(solicitacao);
                salvarDocs(documentos,solicitacao);
                if(documentos.size() >= 2){
                    String nomesDocumentos = documentos.stream()
                            .map(MultipartFile::getOriginalFilename)
                            .map(nome -> "<br>  ->> " + nome + "")
                            .reduce((a, b) -> a + ", " + b)
                            .orElse("");

                    historicoSolicitacao.mudarSolicitacao(solicitacao, "Aluno adicionou um total de " + documentos.size() + " documentos:" + nomesDocumentos);
                }else{
                    historicoSolicitacao.mudarSolicitacao(solicitacao,"Um documento adicionado pelo aluno: " + documentos.get(0).getOriginalFilename());
                }
                return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuário não autorizado!");
    }

    private void solucionarPendente(SolicitarEstagio solicitacao){
        if(solicitacao.getStatus().equalsIgnoreCase("pendente")){
            solicitacao.setStatus("Respondido");
            solicitacaoRepository.save(solicitacao);
        }
    }

    private boolean validarAlunoEEditavel(String solicitante, SolicitarEstagio solicitacao){
        return alunoRepository.existsAlunoByUsuarioSistemaEmail(solicitante) && solicitacao.isEditavel();
    }

    private void salvarDocs(List<MultipartFile> documentos , SolicitarEstagio solicitacao){

        try{
        for (MultipartFile documento : documentos) {
            Documento doc = new Documento();
            byte[] bytesDocumento = documento.getBytes();
            Blob blobDoc = new SerialBlob(bytesDocumento);
            doc.setNome(documento.getOriginalFilename());
            doc.setDocumento(blobDoc);
            doc.setSolicitarEstagio(solicitacao);
            documentoRepository.save(doc);
        }
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Erro ao salvar documento");
        }
    }

    public ResponseEntity deletarDocumento(Long idDocumento, String solicitante){
            if(servidorRepository.existsServidorByUsuarioSistemaEmail(solicitante)){
                documentoRepository.deleteById(idDocumento);
                return ResponseEntity.ok().build();
            }
            if(alunoRepository.existsAlunoByUsuarioSistemaEmail(solicitante)){
            Documento doc = documentoRepository.findById(idDocumento).orElseThrow();
            if(doc.getSolicitarEstagio().isEditavel() && doc.getSolicitarEstagio().getAluno().getUsuarioSistema().getEmail().equals(solicitante)){
                documentoRepository.deleteById(idDocumento);
                historicoSolicitacao.mudarSolicitacao(doc.getSolicitarEstagio(),"Um documento deletado pelo aluno: " + doc.getNome());
                return ResponseEntity.ok().build();
            }
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuário não autorizado!");
    }

}
