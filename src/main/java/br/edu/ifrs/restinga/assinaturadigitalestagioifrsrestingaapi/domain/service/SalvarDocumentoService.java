package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.service;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.controller.BaseController;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.file.GoogleUtil;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Documento;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import lombok.Getter;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

@Service
@Getter
public class SalvarDocumentoService extends BaseController {

    private String pastaAluno;
    private final String pastaSistemaEstagios = "15KVLbIBFCyDcYvaUHeJndqX-2qh5Mwah";

    public void salvarDocumentoDeSolicitacao(String idChamado, List<Documento> documentos) throws IOException, GeneralSecurityException {
        Drive service = GoogleUtil.createDriveService();
        //Linhas comentadas servem para imprimir todos itens do drive, serve como debug.
        //FileList result = service.files().list()
        //         .setPageSize(10)
        //         .setFields("nextPageToken, files(id, name)")
        //         .execute();
        // List<File> files = result.getFiles();
        // if (files == null || files.isEmpty()) {
        //     System.out.println("No files found.");
        // } else {
        //     System.out.println("Files:");
        //     for (File file : files) {
        //         System.out.printf("%s (%s)\n", file.getName(), file.getId());
        //     }
        //}
        if (verificarExistenciaArquivo(service, idChamado)) {
            pastaAluno = criarPastaGoogleDrive(service, idChamado);
        }
        for (Documento documento : documentos) {
            File fileMetadata = new File();
            fileMetadata.setName(documento.getNome());
            fileMetadata.setParents(Collections.singletonList(pastaAluno));
            byte[] bytes;
            try (InputStream inputStream = documento.getDocumento().getBinaryStream()) {
                bytes = inputStream.readAllBytes();
            } catch (SQLException e) {
                System.err.println("::ERRO ao converter BLOB: " + e.getMessage());
                throw new RuntimeException(e);
            }
            InputStreamContent mediaContent = new InputStreamContent("application/octet-stream", new ByteArrayInputStream(bytes));
            try {
                File file = service.files().create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute();
            } catch (GoogleJsonResponseException e) {
                System.err.println("::ERRO ao fazer uploada do arquivo para google drive: " + e.getDetails());
                throw e;
            }
        }
    }
    public String criarPastaGoogleDrive(Drive service, String matriculaAluno) {
        File fileMetadata = new File();
        fileMetadata.setName(matriculaAluno);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        fileMetadata.setParents(Collections.singletonList(pastaSistemaEstagios));
        try {
            File file = service.files().create(fileMetadata)
                    .setFields("id")
                    .execute();
            System.out.println("::PASTA no google drive criada para aluno de matricula: " + matriculaAluno);
            return file.getId();
        } catch (IOException e) {
            System.out.println("::ERRO ao criar pasta no google drive: " + e.getMessage());
        }
        return "";
    }
    public boolean verificarExistenciaArquivo(Drive service, String nome) throws IOException {
        String pageToken = null;
        do {
            FileList result = service.files().list()
                    .setQ("'" + "15KVLbIBFCyDcYvaUHeJndqX-2qh5Mwah" + "' in parents and mimeType='application/vnd.google-apps.folder'")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute();
            for (File file : result.getFiles()) {
                if (file.getName().equals(nome)) {
                    pastaAluno = file.getId();
                    return false;
                }
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);
        return true;
    }
}
