package br.edu.ifs.coletaseletiva;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.jpa.hibernate.ddl-auto=validate")
class Rf06Rf07HttpTests {

    @LocalServerPort
    int porta;

    private final HttpClient cliente = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();

    private HttpResponse<String> enviar(String metodo, String caminho, String dados) throws Exception {
        HttpRequest pedido = HttpRequest.newBuilder(URI.create("http://localhost:" + porta + caminho))
                .header("Content-Type", "application/json")
                .method(metodo, HttpRequest.BodyPublishers.ofString(dados == null ? "" : dados))
                .build();
        return cliente.send(pedido, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void rf06PesquisaPontosPorNomeLocalidadeEMaterialComPaginacao() throws Exception {
        String sufixo = UUID.randomUUID().toString().substring(0, 8);
        var material = enviar("POST", "/api/materiais", """
                {"nome":"Papel-%s","descricao":"Papel reciclável","unidadeMedida":"kg",
                 "orientacaoPreparo":"Manter seco"}
                """.formatted(sufixo));
        assertEquals(201, material.statusCode(), material.body());
        long materialId = json.readTree(material.body()).get("id").asLong();

        String nomePonto = "Ponto RF06 " + sufixo;
        var ponto = enviar("POST", "/api/pontos", """
                {"nome":"%s","endereco":"Rua A, Centro, Lagarto - SE",
                 "contato":"(79) 0000-0000","horariosFuncionamento":"Seg-Sex, 8h-17h",
                 "ativo":true,"recolhimentoDomiciliar":true}
                """.formatted(nomePonto));
        assertEquals(201, ponto.statusCode(), ponto.body());
        long pontoId = json.readTree(ponto.body()).get("id").asLong();

        var vinculo = enviar("POST", "/api/pontos/" + pontoId + "/materiais/" + materialId,
                "{\"observacoes\":\"Aceita limpo\",\"limitePorSolicitacao\":20}");
        assertEquals(201, vinculo.statusCode(), vinculo.body());

        String consulta = "/api/pontos?nome=" + nomePonto.replace(" ", "%20")
                + "&municipio=Lagarto&bairro=Centro&materialId=" + materialId + "&pagina=0&tamanho=10";
        var resposta = enviar("GET", consulta, null);
        assertEquals(200, resposta.statusCode(), resposta.body());
        JsonNode pagina = json.readTree(resposta.body());
        assertEquals(1, pagina.get("content").size());
        JsonNode pontoRetornado = pagina.get("content").get(0);
        assertEquals(nomePonto, pontoRetornado.get("nome").asString());
        assertEquals("Papel-" + sufixo, pontoRetornado.get("materiaisAceitos").get(0).get("nomeMaterial").asString());
        assertEquals("Manter seco", pontoRetornado.get("materiaisAceitos").get(0).get("orientacaoPreparo").asString());

        assertEquals(400, enviar("GET", "/api/pontos?tamanho=101", null).statusCode());
    }

    @Test
    void rf07CadastraOperadorEVinculaPonto() throws Exception {
        String sufixo = UUID.randomUUID().toString().substring(0, 8);
        String email = "operador-" + sufixo + "@example.org";
        var criado = enviar("POST", "/api/operadores",
                "{\"nome\":\"Operador Teste\",\"email\":\"" + email + "\"}");
        assertEquals(201, criado.statusCode(), criado.body());
        long operadorId = json.readTree(criado.body()).get("id").asLong();
        assertEquals(409, enviar("POST", "/api/operadores",
                "{\"nome\":\"Duplicado\",\"email\":\"" + email.toUpperCase() + "\"}").statusCode());

        var ponto = enviar("POST", "/api/pontos", """
                {"nome":"Ponto do operador %s","endereco":"Rua B, Centro, Lagarto - SE",
                 "contato":"Contato","horariosFuncionamento":"8h-17h",
                 "ativo":true,"recolhimentoDomiciliar":false}
                """.formatted(sufixo));
        assertEquals(201, ponto.statusCode(), ponto.body());
        long pontoId = json.readTree(ponto.body()).get("id").asLong();

        String vinculo = "/api/operadores/" + operadorId + "/pontos/" + pontoId;
        var associado = enviar("POST", vinculo, null);
        assertEquals(201, associado.statusCode(), associado.body());
        assertEquals(409, enviar("POST", vinculo, null).statusCode());

        var lista = enviar("GET", "/api/operadores/" + operadorId + "/pontos", null);
        assertEquals(200, lista.statusCode(), lista.body());
        assertEquals(pontoId, json.readTree(lista.body()).get(0).get("pontoId").asLong());
        assertEquals(204, enviar("DELETE", vinculo, null).statusCode());
        assertEquals(404, enviar("DELETE", vinculo, null).statusCode());
    }
}
