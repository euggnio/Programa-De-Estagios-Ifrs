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
        //returns an authorized Credential object.
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static DadosAutenticacaoGoogle verificarToken(String token){
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                .build();
        try {
            System.out.println("DADA " + token);
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

    public static void main(String... args) throws IOException, GeneralSecurityException, SQLException, MessagingException {
        verificarToken("eyJhbGciOiJSUzI1NiIsImtpZCI6IjQ4YTYzYmM0NzY3Zjg1NTBhNTMyZGM2MzBjZjdlYjQ5ZmYzOTdlN2MiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI2MDgzMzc5OTM2NzktamJoNTc2NDJyamhrdXVhZWZnNWxpazN2b2wxdGs0amMuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI2MDgzMzc5OTM2NzktamJoNTc2NDJyamhrdXVhZWZnNWxpazN2b2wxdGs0amMuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMTcwMTkwMDc2Njk3ODMzODAwMzEiLCJoZCI6InJlc3RpbmdhLmlmcnMuZWR1LmJyIiwiZW1haWwiOiIyMDIwMDA2MjkzQHJlc3RpbmdhLmlmcnMuZWR1LmJyIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsIm5iZiI6MTcwNjEzMzg0NywibmFtZSI6IkVVR8OKTklPIENBUlRBR0VOQSBST0RSSUdVRVMiLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDMuZ29vZ2xldXNlcmNvbnRlbnQuY29tL2EvQUNnOG9jS3VTTXVNd3FoOWtMbHA2UjFjd0c0TUZSN0Vzd2VHZXhHdFhoWkdaSllPPXM5Ni1jIiwiZ2l2ZW5fbmFtZSI6IkVVR8OKTklPIENBUlRBR0VOQSIsImZhbWlseV9uYW1lIjoiUk9EUklHVUVTIiwibG9jYWxlIjoicHQtQlIiLCJpYXQiOjE3MDYxMzQxNDcsImV4cCI6MTcwNjEzNzc0NywianRpIjoiOWI2ODYzMmYwOTVlYzNlZThjOWQ0MDBhOGMxYmJmN2MxZDQzMDhhOSJ9.ho_SbM-c4LUvCTobxKdv_M1UM8GVrAWLYPiacKn4aDjdj6BivkmN9CpYQ_Ypei8Q3cBFa1H3lCD1YbF9oeWowFmin4poG5L8SCFNDrzyx4dv4VWxCu_GM6XhknfDc_iBBzrwD3qSZhhqxHnxr68ZkyBE6diqjirROHcvT-cQOX0ika7k4Iuy5C54Jkn1YpZj_bafrcfMJRo7U5KUP36vNhzICZkPyyjSHxOgF97NNrZwGtjhfNqznytBFPukak7dGX4TyG6BmF5GH1a74SpPjBlXPfMvTsZ2RggJNT4eB-4RssO6974MFWF07YuR8QimAlR6uL9Evb-ESRFEG3dmrQ");

    }


}