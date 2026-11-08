package br.edu.ifs.coletaseletiva;

import java.net.URI;
import java.net.http.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.jpa.hibernate.ddl-auto=validate")
class EnderecoHttpTests {
    @LocalServerPort int porta;
    private final HttpClient cliente = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();

    private HttpResponse<String> enviar(String metodo, String caminho, String dados) throws Exception {
        var pedido = HttpRequest.newBuilder(URI.create("http://localhost:" + porta + caminho))
                .header("Content-Type", "application/json")
                .method(metodo, HttpRequest.BodyPublishers.ofString(dados)).build();
        return cliente.send(pedido, HttpResponse.BodyHandlers.ofString());
    }

    private String criarMorador() throws Exception {
        String dados = "{\"nome\":\"Ana\",\"email\":\"" + UUID.randomUUID() + "@example.org\"}";
        var resposta = enviar("POST", "/api/moradores", dados);
        assertEquals(201, resposta.statusCode());
        return resposta.headers().firstValue("Location").orElseThrow() + "/enderecos";
    }

    @Test
    void cadastroListagemEAtualizacaoPorMorador() throws Exception {
        String titular = criarMorador();
        String outro = criarMorador();
        String dados = """
                {"logradouro":"Rua A","numero":"12A","bairro":"Centro",
                 "municipio":"Lagarto","uf":"SE","cep":"49400-000"}
                """;
        var primeiro = enviar("POST", titular, dados);
        assertEquals(201, primeiro.statusCode(), primeiro.body());
        var endereco = json.readTree(primeiro.body());
        String caminho = "/api/enderecos/" + endereco.get("id").asLong();
        String semNumero = dados.replace("\"numero\":\"12A\"", "\"semNumero\":true");
        assertEquals(201, enviar("POST", titular, semNumero).statusCode());
        assertEquals(400, enviar("POST", titular, dados.replace("Centro", " ")).statusCode());
        assertEquals(400, enviar("PUT", caminho, dados.replace("12A", "")).statusCode());
        assertEquals(400, enviar("PUT", caminho, semNumero.replace("true", "true,\"numero\":\"2\"")).statusCode());
        var atualizado = enviar("PUT", caminho, semNumero.replace("Rua A", "Rua B"));
        assertEquals(200, atualizado.statusCode(), atualizado.body());
        var lista = enviar("GET", titular, "");
        assertEquals(200, lista.statusCode());
        var itens = json.readTree(lista.body());
        assertEquals(2, itens.size());
        assertEquals("Rua B", itens.get(0).get("logradouro").asString());
        assertEquals(endereco.get("moradorId"), itens.get(0).get("moradorId"));
        assertEquals("[]", enviar("GET", outro, "").body());
        String ausente = "/api/moradores/-1/enderecos";
        assertEquals(404, enviar("GET", ausente, "").statusCode());
        assertEquals(404, enviar("POST", ausente, dados).statusCode());
        assertEquals(404, enviar("PUT", "/api/enderecos/-1", dados).statusCode());
    }
}
