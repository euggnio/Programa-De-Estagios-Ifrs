package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.file;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.service.SalvarDocumentoService;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;


import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;


public class GoogleUtil {
    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "My First Project";
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    /**
     * Directory to store authorization tokens for this application.
     */
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES =
            Collections.singletonList(DriveScopes.DRIVE);
    private static final String CREDENTIALS_FILE_PATH = "C:\\Users\\eugen\\Documents\\ProgramaDeEstagioIf\\keys\\credentialsAppDrive.json";

    public static JsonFactory getJsonFactory(){
        return JSON_FACTORY;
    }
    public static String getApplicationName(){
        return APPLICATION_NAME;
    }


    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */

    public  static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        InputStream in = new FileInputStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        //returns an authorized Credential object.
        return credential;
    }

    public static Drive createDriveService() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        //Essa linha inicia o serviço do drive, com o transporte definido na linha acima, a  interface do JSON e as credenciais.
        //Além disso, temos o set para o nome da aplicação.
        return new Drive.Builder(HTTP_TRANSPORT, GoogleUtil.getJsonFactory(), GoogleUtil.getCredentials(HTTP_TRANSPORT))
                .setApplicationName(GoogleUtil.getApplicationName())
                .build();
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {
        SalvarDocumentoService salvarDocumentoService = new SalvarDocumentoService();
        salvarDocumentoService.salvarDocumentoDeSolicitacao("BABABA2344","teste");

    }


}