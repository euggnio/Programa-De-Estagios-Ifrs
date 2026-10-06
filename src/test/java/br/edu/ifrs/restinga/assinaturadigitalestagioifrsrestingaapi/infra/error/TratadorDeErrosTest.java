package br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.infra.error;

import br.edu.ifrs.restinga.assinaturadigitalestagioifrsrestingaapi.file.GoogleAuthPendenteException;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TratadorDeErrosTest {

    @RestController
    static class Probe {
        @GetMapping("/probe/autorizacao-pendente")
        void autorizacaoPendente() {
            throw new RuntimeException(
                    new GoogleAuthPendenteException("http://localhost:8888/Callback", "Autorização pendente"));
        }

        @GetMapping("/probe/autorizacao-direta")
        void autorizacaoDireta() throws GoogleAuthPendenteException {
            throw new GoogleAuthPendenteException("http://localhost:8888/Callback", "Autorização pendente");
        }
    }

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new Probe())
            .setControllerAdvice(new TratadorDeErros())
            .build();

    @Test
    void devolve503ComUrlQuandoAutorizacaoPendenteVemEnrolada() throws Exception {
        mockMvc.perform(get("/probe/autorizacao-pendente"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(result -> result.getResponse().getContentAsString()
                        .contains("http://localhost:8888/Callback"));
    }

    @Test
    void devolve503QuandoAutorizacaoPendenteEhLancadaDiretamente() throws Exception {
        mockMvc.perform(get("/probe/autorizacao-direta"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void relancaRuntimeGenericoSemTratar() {
        TratadorDeErros advice = new TratadorDeErros();
        IllegalStateException excecao = new IllegalStateException("boom");
        assertThrows(IllegalStateException.class,
                () -> advice.tratarRuntimeComAutorizacaoGooglePendente(excecao));
    }

    @Test
    void ignoraCadeiaDeCausasSemAutorizacaoPendente() {
        TratadorDeErros advice = new TratadorDeErros();
        RuntimeException excecao = new RuntimeException(new IllegalStateException("boom"));
        assertThrows(RuntimeException.class,
                () -> advice.tratarRuntimeComAutorizacaoGooglePendente(excecao));
    }
}
