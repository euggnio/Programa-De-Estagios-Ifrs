package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.controller;


import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.repository.DocumentoRepository;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.repository.SolicitacaoRepository;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DocumentoDto;
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
    @PostMapping("/salvarDocumento")
    @ResponseBody
    @Transactional
    public ResponseEntity salvarDocumentos(@RequestPart("solicitacaoId") Long id,
                                           @RequestParam("file") List<MultipartFile> documentos) {
        Optional<SolicitarEstagio> solicitacao = solicitacaoRepository.findById(id);
        if (solicitacao.get().isEditavel()) {
            try {
                if(documentoRepository.countBySolicitarEstagioId(id) > 8){
                    return ResponseEntity.status(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED).body("Limite de dados atingido!!");
                }
            for (MultipartFile documento : documentos) {
                Documento doc = new Documento();
                byte[] bytesDocumento = documento.getBytes();
                Blob blobDoc = new SerialBlob(bytesDocumento);
                doc.setNome(documento.getOriginalFilename());
                doc.setDocumento(blobDoc);
                doc.setSolicitarEstagio(solicitacao.get());
                documentoRepository.save(doc);
                return ResponseEntity.ok().build();
            }
                } catch (IOException | SQLException e) {
                    return ResponseEntity.badRequest().body("Algum erro ocorreu!");
                }
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
            } catch (IOException e) {
                // Lidar com a exceção, se necessário
            }
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(baos.toByteArray());
    }

    @DeleteMapping("/deletarDocumento")
    public ResponseEntity<String> deletarDocumento(@RequestParam Long chamadoId){
        Optional<Documento> doc = documentoRepository.findById(chamadoId);
        if(doc.isPresent()){
            documentoRepository.deleteById(chamadoId);
            return ResponseEntity.ok("Documento deletado");
        }
        else{
            return ResponseEntity.notFound().build();
        }
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

    @GetMapping("/listarDocumento/{solicitarEstagioId}")
    @ResponseBody
    public ResponseEntity<List<DocumentoDto>> listarDocumentosPorSolicitarEstagioId(@PathVariable("solicitarEstagioId") Long solicitarEstagioId) {
        List<Documento> documentos = documentoRepository.findBySolicitarEstagioId(solicitarEstagioId);
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
}
