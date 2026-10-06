package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.file;

import java.io.IOException;

public class GoogleAuthPendenteException extends IOException {

    private final transient String urlAutorizacao;

    public GoogleAuthPendenteException(String urlAutorizacao, String mensagem) {
        super(mensagem);
        this.urlAutorizacao = urlAutorizacao;
    }

    public String getUrlAutorizacao() {
        return urlAutorizacao;
    }
}
