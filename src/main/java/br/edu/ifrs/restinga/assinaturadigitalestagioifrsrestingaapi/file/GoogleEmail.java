package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.file;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.*;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import static com.google.api.services.gmail.GmailScopes.GMAIL_SEND;

import static javax.mail.Message.RecipientType.TO;

@Component
public class GoogleEmail {

    @Value("${google.drive.tokens-directory}")
    private String tokensDirectoryPath;

    @Value("${google.oauth.port}")
    private int oauthPort;

    @Value("${google.drive.application-name}")
    private String applicationName;

    private static GoogleEmail instance;

    @jakarta.annotation.PostConstruct
    public void init() {
        instance = this;
    }

    private static Credential getCredentials(final NetHttpTransport httpTransport, GsonFactory jsonFactory)
            throws IOException {
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(Objects.requireNonNull(GoogleEmail.class.getResourceAsStream("/clientKey.json"))));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, clientSecrets, Set.of(GMAIL_SEND))
                .setDataStoreFactory(new FileDataStoreFactory(Paths.get(instance.tokensDirectoryPath).toFile()))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(instance.oauthPort).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static void sendMail(String paraEmail, String subject, String message, String  htmlBody) throws Exception {
        try {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        Gmail service = new Gmail.Builder(httpTransport, jsonFactory, GoogleUtil.getCredentials(httpTransport))
                .setApplicationName(instance.applicationName)
                .build();
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(paraEmail));
        email.addRecipient(TO, new InternetAddress(paraEmail));
        email.setSubject(subject);
        email.setText(message);
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(htmlBody, "text/html");
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);
        email.setContent(multipart);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] rawMessageBytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(rawMessageBytes);
        Message msg = new Message();
        msg.setRaw(encodedEmail);
            msg = service.users().messages().send("me", msg).execute();
            System.out.println("Message id: " + msg.getId());
            System.out.println(msg.toPrettyString());
        } catch (GoogleJsonResponseException e) {
            GoogleJsonError error = e.getDetails();
            if (error.getCode() == 403) {
                System.err.println("ERRO AO ENVIAR EMAIL: " + e.getDetails());
            } else {
                throw e;
            }
        }
    }
}