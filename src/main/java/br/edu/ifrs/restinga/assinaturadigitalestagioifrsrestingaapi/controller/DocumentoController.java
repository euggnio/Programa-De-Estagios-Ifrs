package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.controller;


import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.repository.DocumentoRepository;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.repository.SolicitacaoRepository;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.service.DocumentoService;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.service.SalvarDocumentoService;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DocumentoDto;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.infra.security.TokenService;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Documento;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.SolicitarEstagio;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.sql.rowset.serial.SerialBlob;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class DocumentoController {
    @Autowired
    DocumentoRepository documentoRepository;
    @Autowired
    SolicitacaoRepository solicitacaoRepository;
    @Autowired
    DocumentoService documentoService;
    @Autowired
    TokenService tokenService;

    @PostMapping("/salvarDocumento")
    @ResponseBody
    @Transactional
    public ResponseEntity salvarDocumentos(@RequestPart("solicitacaoId") Long id,
                                           @RequestParam("file") List<MultipartFile> documentos,
                                           @RequestHeader("Authorization") String token) {
        String email = tokenService.getSubject(token.replace("Bearer ", ""));
        Optional<SolicitarEstagio> solicitacao = solicitacaoRepository.findById(id);
        if(solicitacao.isPresent()){
            return documentoService.salvarDocumentosSolicitacao(solicitacao.get(), documentos,email);
        }
        return ResponseEntity.badRequest().body("Impossivel adicionar documentos!");
    }

    @GetMapping("/downloadDocumento")
    public ResponseEntity<Resource> downloadArquivo(@RequestParam long chamadoId) {
        Optional<Documento> doc;
        try {
            doc  = documentoRepository.findById(chamadoId);
            if(!doc.isPresent()){
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }
            byte[] documentoBytes = doc.get().getDocumento().getBytes(1, (int) doc.get().getDocumento().length());
            InputStream inputStream = new ByteArrayInputStream(documentoBytes);
            Resource resource = new InputStreamResource(inputStream);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
        } catch (SQLException e) {
            return ResponseEntity.badRequest().build();
        }
    }


    @GetMapping("/downloadDocumentosAssinados")
    public ResponseEntity<byte[]> downloadArquivosAssinados(@RequestParam long chamadoId) throws SQLException {
        List<Documento> docs = documentoRepository.findBySolicitarEstagioIdAndAssinadoTrue(chamadoId);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (Documento doc : docs) {
            byte[] documentoBytes = doc.getDocumento().getBytes(1, (int) doc.getDocumento().length());
            try {
                baos.write(documentoBytes);
            } catch (IOException ignored) {

            }
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(baos.toByteArray());
    }

    @DeleteMapping("/deletarDocumento")
    public ResponseEntity deletarDocumento(@RequestParam Long chamadoId,
                                           @RequestHeader("Authorization") String token){
        String email = tokenService.getSubject(token.replace("Bearer ", ""));
        return documentoService.deletarDocumento(chamadoId, email);
    }


    @GetMapping("/listarDocumento")
    @ResponseBody
    public ResponseEntity<List<DocumentoDto>> listarDocumentos() {
        List<Documento> documentos = documentoRepository.findAll();
        List<DocumentoDto> documentosDto = new ArrayList<>();

        for (Documento documento : documentos) {
            DocumentoDto documentoDto = new DocumentoDto();
            documentoDto.setId(documento.getId());
            documentoDto.setNome(documento.getNome());
            documentosDto.add(documentoDto);
        }
        if (!documentosDto.isEmpty()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(documentosDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/listarDocumentoAssinados/{solicitarEstagioId}")
    @ResponseBody
    public ResponseEntity<List<DocumentoDto>> listarDocumentosAssinados(@PathVariable("solicitarEstagioId") Long solicitarEstagioId) {
        List<Documento> documentos = documentoRepository.findBySolicitarEstagioIdAndAssinadoTrue(solicitarEstagioId);
        List<DocumentoDto> documentosDto = new ArrayList<>();
        for (Documento documento : documentos) {
            DocumentoDto documentoDto = new DocumentoDto();
            documentoDto.setId(documento.getId());
            documentoDto.setNome(documento.getNome());
            documentosDto.add(documentoDto);
        }
        if (!documentosDto.isEmpty()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(documentosDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/direcionarDocumentoDiretor/{documentoId}")
    @ResponseBody
    public ResponseEntity<String> direcionarDocDiretor(@PathVariable("documentoId") Long documentoId){
        Optional<Documento> documento = documentoRepository.findById(documentoId);
        if(documento.isPresent()){
            documento.get().setParaDiretor(!documento.get().isParaDiretor());
            documentoRepository.save(documento.get());
            return ResponseEntity.ok().build();
        }
        else{
            return ResponseEntity.notFound().build();
        }

    }

    @GetMapping("/listarDocumento/{solicitarEstagioId}")
    @ResponseBody
    public ResponseEntity<List<DocumentoDto>> listarDocumentosPorSolicitarEstagioId(@PathVariable("solicitarEstagioId") Long solicitarEstagioId) {
        List<Documento> documentos = documentoRepository.findBySolicitarEstagioId(solicitarEstagioId);
        List<DocumentoDto> documentosDto = new ArrayList<>();
        for (Documento documento : documentos) {
            DocumentoDto documentoDto = new DocumentoDto();
            documentoDto.setAssinado(documento.isAssinado());
            documentoDto.setId(documento.getId());
            documentoDto.setParaDiretor(documento.isParaDiretor());
            documentoDto.setNome(documento.getNome());
            documentosDto.add(documentoDto);
        }

        if (!documentosDto.isEmpty()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(documentosDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
