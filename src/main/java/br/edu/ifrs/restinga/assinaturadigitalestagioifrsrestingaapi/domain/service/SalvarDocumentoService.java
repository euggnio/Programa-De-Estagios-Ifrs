package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.service;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.file.GoogleUtil;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import static br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.file.GoogleUtil.*;


public class SalvarDocumentoService {




    public void salvarDocumentoDeSolicitacao(String idChamado, String nomeDocumento) throws IOException, GeneralSecurityException {
        Drive service = GoogleUtil.createDriveService();
        FileList result = service.files().list()
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name)")
                .execute();
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:");
            for (File file : files) {
                System.out.printf("%s (%s)\n", file.getName(), file.getId());
            }
        }

        if(verificarExistenciaArquivo(service,idChamado,result)){
            criarPastaGoogleDrive(service,idChamado);
        }

        File fileMetadata = new File();
        fileMetadata.setName(nomeDocumento);
        fileMetadata.setParents(Collections.singletonList("15KVLbIBFCyDcYvaUHeJndqX-2qh5Mwah"));
        // File's content.
        java.io.File filePath = new java.io.File("C:\\Users\\eugen\\Videos\\Captures\\2.png");
        // Specify media type and file-path for file.
        FileContent mediaContent = new FileContent("image/jpeg", filePath);
        try {
            File file = service.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();
            System.out.println("File ID: " + file.getId());
        } catch (GoogleJsonResponseException e) {
            // TODO(developer) - handle error appropriately
            System.err.println("Unable to upload file: " + e.getDetails());
            throw e;
        }
    }

    public void criarPastaGoogleDrive(Drive service, String nomePasta) {
        File fileMetadata = new File();
        fileMetadata.setName(nomePasta);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        fileMetadata.setParents(Collections.singletonList("15KVLbIBFCyDcYvaUHeJndqX-2qh5Mwah"));
        try {
            File file = service.files().create(fileMetadata)
                    .setFields("id")
                    .execute();
            System.out.println("Folder ID CREATED: " + file.getId());

        } catch (IOException e ) {
            System.out.println("erro" + e.getMessage());
        }
    }
    public boolean verificarExistenciaArquivo(Drive service, String nome, FileList result) throws IOException {
        String pageToken = null;
        do {
            FileList result2 = service.files().list()
                    .setQ("'" + "15KVLbIBFCyDcYvaUHeJndqX-2qh5Mwah" + "' in parents and mimeType='application/vnd.google-apps.folder'")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute();
            for (File file : result.getFiles()) {
                if(file.getName().equals(nome)){
                    System.out.println("ID ENCONTRADO ESSA PASTA JÁ EXISTE!!!!");
                    return false;
                }
            }
            pageToken = result2.getNextPageToken();
        } while (pageToken != null);
        return true;
    }


}
