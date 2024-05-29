package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.service;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.controller.BaseController;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.file.GoogleUtil;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.model.Documento;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import lombok.Getter;
import org.springframework.stereotype.Service;
import com.google.api.services.drive.Drive.Permissions.Update;
import javax.mail.MessagingException;
import javax.sql.rowset.serial.SerialBlob;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Getter
public class SalvarDocumentoService extends BaseController {

    private String pastaAluno;
    private final String pastaSistemaEstagios = "15KVLbIBFCyDcYvaUHeJndqX-2qh5Mwah";

    public void salvarDocumentoDeSolicitacao(String idChamado,long idCurso, List<Documento> documentos, String email) throws IOException, GeneralSecurityException {
        Drive service = GoogleUtil.createDriveService();
        String pastaCursoId = verificarExistenciaPastaCurso(service, idCurso);
        if (pastaCursoId.isEmpty()) {
            pastaCursoId = criarPastaCurso(service, idCurso);
        }
        System.out.println("::Pasta do curso: " + pastaCursoId);
        if (verificarExistenciaArquivo(service, idChamado, pastaCursoId)) {
            pastaAluno = criarPastaGoogleDrive(service, idChamado, pastaCursoId, email);
        }

        for (Documento documento : documentos) {
            File fileMetadata = new File();
            fileMetadata.setName(documento.getNome());
            fileMetadata.setParents(Collections.singletonList(pastaCursoId));
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
    public String criarPastaGoogleDrive(Drive service, String matriculaAluno, String pastaCursoId, String email) {
        Permission permission = new Permission();
        permission.setEmailAddress(email);
        permission.setType("user");
        permission.setRole("reader");

        File fileMetadata = new File();
        fileMetadata.setName(matriculaAluno);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        fileMetadata.setParents(Collections.singletonList(pastaCursoId));
        try {
            File file = service.files().create(fileMetadata)
                    .setFields("id")
                    .execute();
            service.permissions().create(file.getId(), permission).setFields("id").execute();
            System.out.println("::PASTA no google drive criada para aluno de matricula: " + matriculaAluno);
            System.out.println("::ID da pasta: " + file.getId());
            return file.getId();
        } catch (IOException e) {
            System.out.println("::ERRO ao criar pasta no google drive: " + e.getMessage());
        }
        return "";
    }
    public boolean verificarExistenciaArquivo(Drive service, String nome, String pastaCursoId) throws IOException {
        String pageToken = null;
        do {
            System.out.println("::Pasta do curso: " + pastaCursoId);
            FileList result = service.files().list()

                    .setQ("'" +pastaCursoId + "' in parents and mimeType='application/vnd.google-apps.folder'")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute();
            for (File file : result.getFiles()) {
                System.out.println("::Pasta do aluno: " + file.getName());
                if (file.getName().equals(nome)) {
                    System.out.println("::Pasta do aluno encontrada: " + file.getId());
                    pastaAluno = file.getId();
                    return false;
                }
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);
        return true;
    }

    public String verificarExistenciaPastaCurso(Drive service, long idCurso) throws IOException {
        String pageToken = null;
        do {
            FileList result = service.files().list()
                    .setQ("'" + "15KVLbIBFCyDcYvaUHeJndqX-2qh5Mwah" + "' in parents and mimeType='application/vnd.google-apps.folder'")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute();
            for (File file : result.getFiles()) {
                if (file.getName().equals(getNomeCursoPorId(idCurso))) {
                    return file.getId();
                }
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);
        return "";
    }

    public String criarPastaCurso(Drive service, long idCurso) {
        File fileMetadata = new File();
        fileMetadata.setName(""+ getNomeCursoPorId(idCurso));
        fileMetadata.setMimeType("application/vnd.google-apps.folder");
        fileMetadata.setParents(Collections.singletonList(pastaSistemaEstagios));
        try {
            File file = service.files().create(fileMetadata)
                    .setFields("id")
                    .execute();
            System.out.println("::PASTA no Google Drive criada para o curso: " + idCurso);
            System.out.println("::ID da pasta do curso: " + file.getId());
            return file.getId();
        } catch (IOException e) {
            System.out.println("::ERRO ao criar pasta do curso no Google Drive: " + e.getMessage());
        }
        return "";
    }

    public String getNomeCursoPorId(long idCurso) {
        if(idCurso == 10) return "Análise e Desenvolvimento de Sistemas - " + idCurso;
        if(idCurso == 11) return "Letras Português e Espanhol - " + idCurso;
        if(idCurso == 12) return "Eletrônica Industrial - " + idCurso;
        if(idCurso == 13) return "Gestão Desportiva e de Lazer - " + idCurso;
        if(idCurso == 14) return "Processos Gerenciais - " + idCurso;
        if(idCurso == 15) return "Setor Estágio - " + idCurso;
        if(idCurso == 16) return "Diretor - " + idCurso;
        if(idCurso == 17) return "Lazer - " + idCurso;
        if(idCurso == 18) return "Informática - " + idCurso;
        if(idCurso == 19) return "Eletrônica - " + idCurso;
        if(idCurso == 20) return "Guia de Turismo - " + idCurso;
        return "Curso não encontrado ";
    }
    public static void main(String... args) throws IOException, GeneralSecurityException, SQLException, MessagingException {
        SalvarDocumentoService salvarDocumentoService = new SalvarDocumentoService();
        // Criar arquivo falso para teste
        Documento documento = new Documento();
        File filePath = new File();
        documento.setNome("teste.pdf");
            Blob blob = new SerialBlob("teste".getBytes()); // Criação do blob usando o array de bytes
            documento.setDocumento(blob);
        List<Documento> docs = new ArrayList<>();
        docs.add(documento);
        salvarDocumentoService.salvarDocumentoDeSolicitacao("test2331", 123, docs, "euggnio@gmail.com");
    }
}
