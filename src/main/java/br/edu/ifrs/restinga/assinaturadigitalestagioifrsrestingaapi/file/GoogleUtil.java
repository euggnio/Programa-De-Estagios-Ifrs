package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.file;
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
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;


@Component
public class GoogleUtil {

    private static GoogleUtil instance;

    @Value("${google.drive.application-name}")
    private String applicationName;

    @Value("${google.drive.tokens-directory}")
    private String tokensDirectoryPath;

    @Value("${google.drive.credentials-path}")
    private String credentialsPath;

    @Value("${google.oauth.port}")
    private int oauthPort;

    @Value("${google.oauth.user-authorization}")
    private String oauthUserAuthorization;

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Arrays.asList(
            DriveScopes.DRIVE,
            GmailScopes.GMAIL_SEND
    );

    @PostConstruct
    public void init() {
        instance = this;
    }

    public static JsonFactory getJsonFactory(){
        return JSON_FACTORY;
    }
    public static String getApplicationName(){
        return instance.applicationName;
    }


    public  static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        InputStream in;
        if (instance.credentialsPath.startsWith("classpath:")) {
            String resourcePath = instance.credentialsPath.substring("classpath:".length());
            in = GoogleUtil.class.getResourceAsStream("/" + resourcePath);
        } else {
            in = new FileInputStream(instance.credentialsPath);
        }
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + instance.credentialsPath);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(instance.tokensDirectoryPath)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(instance.oauthPort).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize(instance.oauthUserAuthorization);
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