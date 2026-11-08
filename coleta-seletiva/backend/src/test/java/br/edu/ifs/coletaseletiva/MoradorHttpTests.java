package br.edu.ifs.coletaseletiva;

import java.net.URI;
import java.net.http.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.jpa.hibernate.ddl-auto=validate")
class MoradorHttpTests {
    @LocalServerPort int porta;
    private final HttpClient cliente = HttpClient.newHttpClient();

    private HttpResponse<String> enviar(String metodo, String caminho, String dados) throws Exception {
        var pedido = HttpRequest.newBuilder(URI.create("http://localhost:" + porta + caminho))
                .header("Content-Type", "application/json")
                .method(metodo, HttpRequest.BodyPublishers.ofString(dados)).build();
        return cliente.send(pedido, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void cicloDeCadastro() throws Exception {
        String base = "/api/moradores";
        String dados = "{\"nome\":\"Ana\",\"email\":\" Ana@Example.org \"}";
        var criado = enviar("POST", base, dados);
        assertEquals(201, criado.statusCode());
        String caminho = criado.headers().firstValue("Location").orElseThrow();
        assertTrue(enviar("GET", caminho, "").body().contains("ana@example.org"));
        assertEquals(409, enviar("POST", base, dados).statusCode());
        assertEquals(200, enviar("PUT", caminho, dados.replace("Ana", "Maria")).statusCode());
        assertTrue(enviar("GET", caminho, "").body().contains("maria@example.org"));
        assertEquals(400, enviar("POST", base, "{}").statusCode());
        assertEquals(404, enviar("GET", base + "/999999", "").statusCode());
    }
}
