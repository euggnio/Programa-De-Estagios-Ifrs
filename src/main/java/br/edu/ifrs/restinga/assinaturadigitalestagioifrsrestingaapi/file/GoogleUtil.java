package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.file;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.domain.service.SalvarDocumentoService;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosAutenticacaoGoogle;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import static com.google.api.services.gmail.GmailScopes.GMAIL_SEND;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.gmail.GmailScopes;


import javax.mail.MessagingException;
import java.io.*;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class GoogleUtil {


    private static final String APPLICATION_NAME = "MailSender";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Arrays.asList(
            DriveScopes.DRIVE,
            GmailScopes.GMAIL_SEND
    );
    static final String CREDENTIALS_FILE_PATH = "C:\\Users\\eugen\\Documents\\ProgramaDeEstagioIf\\keys\\clientKey.json";

    public static JsonFactory getJsonFactory(){
        return JSON_FACTORY;
    }
    public static String getApplicationName(){
        return APPLICATION_NAME;
    }


    public  static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        InputStream in = new FileInputStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static DadosAutenticacaoGoogle verificarToken(String token){
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                .build();
        try {
            GoogleIdToken idToken = verifier.verify(token);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                if(payload.getEmailVerified()) {
                    DadosAutenticacaoGoogle dados = new DadosAutenticacaoGoogle(
                             payload.getEmail()
                            ,payload.get("family_name").toString()
                            ,payload.get("given_name").toString()
                            ,payload.get("name").toString()
                            ,payload.get("sub").toString());
                    return dados;
                }
            }
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static Drive createDriveService() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        return new Drive.Builder(HTTP_TRANSPORT, GoogleUtil.getJsonFactory(), GoogleUtil.getCredentials(HTTP_TRANSPORT))
                .setApplicationName(GoogleUtil.getApplicationName())
                .build();
    }


}