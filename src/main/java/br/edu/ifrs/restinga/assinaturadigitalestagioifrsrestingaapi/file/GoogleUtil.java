package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.file;
import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.dto.DadosAutenticacaoGoogle;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;


@Component
public class GoogleUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleUtil.class);

    private static final Object OAUTH_LOCK = new Object();

    private static final ExecutorService OAUTH_EXECUTOR =
            Executors.newSingleThreadExecutor(daemon("google-oauth-consentimento"));

    private static final ScheduledExecutorService OAUTH_WATCHDOG =
            Executors.newSingleThreadScheduledExecutor(daemon("google-oauth-watchdog"));

    private static Future<Credential> consentimentoPendente;

    private static volatile String urlAutorizacaoPendente;

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

    @Value("${google.oauth.callback-timeout-seconds:60}")
    private int callbackTimeoutSeconds;

    @Value("${google.oauth.callback-max-wait-minutes:15}")
    private int callbackMaxWaitMinutes;

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Arrays.asList(
            DriveScopes.DRIVE,
            GmailScopes.GMAIL_SEND
    );

    @PostConstruct
    public void init() {
        instance = this;
        verificarRefreshTokenPersistido();
    }

    private static ThreadFactory daemon(String nome) {
        return runnable -> {
            Thread thread = new Thread(runnable, nome);
            thread.setDaemon(true);
            return thread;
        };
    }

    /**
     * Verificação explícita, em tempo de subida, do refresh_token persistido no volume
     * {@code google.drive.tokens-directory}. Sem refresh_token todo acesso ao Drive/e-mail
     * exige novo consentimento (e um callback em {@code google.oauth.port}).
     */
    private void verificarRefreshTokenPersistido() {
        try {
            Credential credencial = flow(GoogleNetHttpTransport.newTrustedTransport()).loadCredential(oauthUserAuthorization);
            if (possuiRefreshToken(credencial)) {
                LOGGER.info("Refresh token Google presente em '{}' (usuário '{}')", caminhoTokens(), oauthUserAuthorization);
            } else {
                LOGGER.warn("Nenhum refresh token Google em '{}' (usuário '{}'). "
                                + "O próximo uso do Drive/e-mail exigirá consentimento: a URL será logada "
                                + "e a requisição responderá HTTP 503 até a autorização ser concluída.",
                        caminhoTokens(), oauthUserAuthorization);
            }
        } catch (Exception e) {
            LOGGER.warn("Não foi possível verificar o refresh token em '{}': {}", caminhoTokens(), e.toString());
        }
    }

    public static JsonFactory getJsonFactory(){
        return JSON_FACTORY;
    }
    public static String getApplicationName(){
        return instance.applicationName;
    }


    public  static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        GoogleAuthorizationCodeFlow flow = flow(HTTP_TRANSPORT);
        String usuario = instance.oauthUserAuthorization;
        Credential credencial = flow.loadCredential(usuario);
        if (possuiRefreshToken(credencial)) {
            LOGGER.debug("Credencial Google reutilizada de '{}' (refresh_token presente)", caminhoTokens());
            return credencial;
        }
        LOGGER.warn("Nenhum refresh token persistido em '{}' (usuário '{}'): consentimento OAuth necessário",
                caminhoTokens(), usuario);
        return solicitarConsentimento(flow, usuario);
    }

    private static GoogleAuthorizationCodeFlow flow(final NetHttpTransport httpTransport) throws IOException {
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
        try {
            GoogleClientSecrets clientSecrets =
                    GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
            return new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(instance.tokensDirectoryPath)))
                    .setAccessType("offline")
                    .build();
        } finally {
            in.close();
        }
    }

    private static boolean possuiRefreshToken(Credential credencial) {
        return credencial != null
                && credencial.getRefreshToken() != null
                && !credencial.getRefreshToken().isEmpty();
    }

    private static String caminhoTokens() {
        return new java.io.File(instance.tokensDirectoryPath).getAbsolutePath();
    }

    /**
     * Dispara o consentimento em uma thread própria e aguarda no máximo
     * {@code google.oauth.callback-timeout-seconds}. Nunca deixa a requisição bloqueada
     * para sempre: ao estourar o tempo devolve {@link GoogleAuthPendenteException} (HTTP 503)
     * com a URL de autorização, enquanto o fluxo segue em background e grava o refresh_token.
     */
    private static Credential solicitarConsentimento(GoogleAuthorizationCodeFlow flow, String usuario)
            throws IOException {
        Future<Credential> futuro;
        synchronized (OAUTH_LOCK) {
            if (consentimentoPendente == null || consentimentoPendente.isDone()) {
                LOGGER.info("Iniciando fluxo de consentimento Google na porta {}", instance.oauthPort);
                consentimentoPendente = OAUTH_EXECUTOR.submit(() -> executarConsentimento(flow, usuario));
            }
            futuro = consentimentoPendente;
        }
        try {
            Credential credencial = futuro.get(instance.callbackTimeoutSeconds, TimeUnit.SECONDS);
            if (!possuiRefreshToken(credencial)) {
                throw new GoogleAuthPendenteException(urlAutorizacaoPendente,
                        "Consentimento do Google concluído sem refresh_token; refaça a autorização.");
            }
            LOGGER.info("Consentimento Google concluído; refresh_token gravado em '{}'", caminhoTokens());
            return credencial;
        } catch (TimeoutException e) {
            throw new GoogleAuthPendenteException(urlAutorizacaoPendente,
                    "Autorização Google pendente: abra a URL de autorização e repita a requisição em instantes.");
        } catch (ExecutionException e) {
            Throwable causa = e.getCause() == null ? e : e.getCause();
            if (causa instanceof IOException io) {
                throw io;
            }
            throw new IOException("Falha no consentimento OAuth do Google", causa);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrompido aguardando o consentimento OAuth do Google", e);
        }
    }

    private static Credential executarConsentimento(GoogleAuthorizationCodeFlow flow, String usuario)
            throws IOException {
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(instance.oauthPort).build();
        long minutos = instance.callbackMaxWaitMinutes;
        OAUTH_WATCHDOG.schedule(() -> pararReceiver(receiver), minutos, TimeUnit.MINUTES);
        AuthorizationCodeInstalledApp app = new AuthorizationCodeInstalledApp(flow, receiver) {
            @Override
            protected void onAuthorization(AuthorizationCodeRequestUrl authorizationUrl) {
                authorizationUrl.set("prompt", "consent");
                urlAutorizacaoPendente = authorizationUrl.build();
                LOGGER.warn("Autorização Google pendente. Abra no navegador: {}", urlAutorizacaoPendente);
            }
        };
        return app.authorize(usuario);
    }

    private static void pararReceiver(LocalServerReceiver receiver) {
        try {
            receiver.stop();
            LOGGER.warn("Fluxo de consentimento Google encerrado por limite de tempo "
                    + "({} min) sem autorização.", instance.callbackMaxWaitMinutes);
        } catch (Exception e) {
            LOGGER.warn("Falha ao encerrar o servidor de callback do Google: {}", e.toString());
        }
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
        } catch (Exception e) {
            return null;
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