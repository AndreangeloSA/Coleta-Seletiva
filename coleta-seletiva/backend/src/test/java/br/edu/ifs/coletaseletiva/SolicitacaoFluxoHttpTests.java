package br.edu.ifs.coletaseletiva;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes HTTP do ciclo de solicitações e da proteção RN05.
 * Requerem um PostgreSQL de desenvolvimento configurado pelas variáveis DB_*.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SolicitacaoFluxoHttpTests {
    @LocalServerPort
    int porta;

    private final HttpClient cliente = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();

    private HttpResponse<String> enviar(String metodo, String caminho, String corpo) throws Exception {
        HttpRequest.Builder pedido = HttpRequest.newBuilder(URI.create("http://localhost:" + porta + caminho))
                .header("Content-Type", "application/json");
        if (corpo == null) {
            pedido.method(metodo, HttpRequest.BodyPublishers.noBody());
        } else {
            pedido.method(metodo, HttpRequest.BodyPublishers.ofString(corpo));
        }
        return cliente.send(pedido.build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void percorreCicloCompletoEGravaHistorico() throws Exception {
        Cenario cenario = prepararCenario();
        HttpResponse<String> criada = criarSolicitacao(cenario);
        assertEquals(201, criada.statusCode(), criada.body());
        JsonNode solicitacao = json.readTree(criada.body());
        long id = solicitacao.get("id").asLong();
        long versao = solicitacao.get("versao").asLong();
        assertEquals("SOLICITADA", solicitacao.get("situacao").asText());
        assertEquals(2, solicitacao.get("itens").size());

        // Iniciar sem agendar é uma transição proibida.
        assertEquals(409, enviar("POST", "/api/solicitacoes/" + id + "/iniciar",
                "{\"versao\":" + versao + "}").statusCode());

        String dataFutura = OffsetDateTime.now(ZoneId.of("America/Maceio"))
                .plusDays(2).withNano(0).toString();
        HttpResponse<String> agendada = enviar("POST", "/api/solicitacoes/" + id + "/agendar",
                "{\"dataAgendamento\":\"" + dataFutura + "\",\"versao\":" + versao + "}");
        assertEquals(200, agendada.statusCode(), agendada.body());
        JsonNode aposAgendamento = json.readTree(agendada.body());
        assertEquals("AGENDADA", aposAgendamento.get("situacao").asText());
        long versaoAgendada = aposAgendamento.get("versao").asLong();

        HttpResponse<String> iniciada = enviar("POST", "/api/solicitacoes/" + id + "/iniciar",
                "{\"versao\":" + versaoAgendada + "}");
        assertEquals(200, iniciada.statusCode(), iniciada.body());
        JsonNode aposInicio = json.readTree(iniciada.body());
        assertEquals("EM_RECOLHIMENTO", aposInicio.get("situacao").asText());
        long versaoEmExecucao = aposInicio.get("versao").asLong();

        long itemKg = itemIdPorMaterial(aposInicio, cenario.materialKgId());
        long itemPeca = itemIdPorMaterial(aposInicio, cenario.materialPecaId());
        String dadosConclusao = "{\"versao\":" + versaoEmExecucao + ",\"itens\":["
                + "{\"itemId\":" + itemKg + ",\"quantidadeEfetiva\":0},"
                + "{\"itemId\":" + itemPeca + ",\"quantidadeEfetiva\":3}]}";
        HttpResponse<String> concluida = enviar("PATCH", "/api/solicitacoes/" + id + "/concluir", dadosConclusao);
        assertEquals(200, concluida.statusCode(), concluida.body());
        JsonNode resultado = json.readTree(concluida.body());
        assertEquals("CONCLUIDA", resultado.get("situacao").asText());
        assertEquals(0, itemPorMaterial(resultado, cenario.materialKgId()).get("quantidadeEfetiva").asInt());
        assertEquals(3, itemPorMaterial(resultado, cenario.materialPecaId()).get("quantidadeEfetiva").asInt());

        HttpResponse<String> historico = enviar("GET", "/api/solicitacoes/" + id + "/historico", null);
        assertEquals(200, historico.statusCode(), historico.body());
        assertEquals(4, json.readTree(historico.body()).size());

        // O cliente que tenta reutilizar uma versão antiga recebe conflito.
        assertEquals(409, enviar("POST", "/api/solicitacoes/" + id + "/iniciar",
                "{\"versao\":" + versaoAgendada + "}").statusCode());
    }

    @Test
    void recusaEFrustracaoRespeitamAsTransicoesEGravamMotivos() throws Exception {
        Cenario cenarioRecusa = prepararCenario();
        JsonNode criadaRecusa = json.readTree(criarSolicitacao(cenarioRecusa).body());
        long idRecusa = criadaRecusa.get("id").asLong();
        HttpResponse<String> recusada = enviar("POST", "/api/solicitacoes/" + idRecusa + "/recusar",
                "{\"motivo\":\"Área fora do atendimento\",\"versao\":"
                        + criadaRecusa.get("versao").asLong() + "}");
        assertEquals(200, recusada.statusCode(), recusada.body());
        assertEquals("RECUSADA", json.readTree(recusada.body()).get("situacao").asText());

        Cenario cenarioFrustrada = prepararCenario();
        JsonNode criada = json.readTree(criarSolicitacao(cenarioFrustrada).body());
        long id = criada.get("id").asLong();
        String dataInicial = OffsetDateTime.now(ZoneId.of("America/Maceio")).plusDays(2).withNano(0).toString();
        HttpResponse<String> agendada = enviar("POST", "/api/solicitacoes/" + id + "/agendar",
                "{\"dataAgendamento\":\"" + dataInicial + "\",\"versao\":"
                        + criada.get("versao").asLong() + "}");
        assertEquals(200, agendada.statusCode(), agendada.body());
        JsonNode agenda = json.readTree(agendada.body());
        HttpResponse<String> iniciada = enviar("POST", "/api/solicitacoes/" + id + "/iniciar",
                "{\"versao\":" + agenda.get("versao").asLong() + "}");
        assertEquals(200, iniciada.statusCode(), iniciada.body());
        JsonNode execucao = json.readTree(iniciada.body());

        String novaData = OffsetDateTime.now(ZoneId.of("America/Maceio")).plusDays(4).withNano(0).toString();
        HttpResponse<String> frustrada = enviar("PATCH", "/api/solicitacoes/" + id + "/frustrada",
                "{\"motivo\":\"Ninguém no endereço\",\"dadosTentativa\":\"Primeira tentativa\","
                        + "\"novaDataHora\":\"" + novaData + "\",\"versao\":"
                        + execucao.get("versao").asLong() + "}");
        assertEquals(200, frustrada.statusCode(), frustrada.body());
        JsonNode retorno = json.readTree(frustrada.body());
        assertEquals("AGENDADA", retorno.get("situacao").asText());

        String novaData2 = OffsetDateTime.now(ZoneId.of("America/Maceio")).plusDays(5).withNano(0).toString();
        HttpResponse<String> reagendada = enviar("PATCH", "/api/solicitacoes/" + id + "/reagendar",
                "{\"motivo\":\"Ajuste de rota\",\"novaDataHora\":\"" + novaData2
                        + "\",\"versao\":" + retorno.get("versao").asLong() + "}");
        assertEquals(200, reagendada.statusCode(), reagendada.body());
        assertEquals("AGENDADA", json.readTree(reagendada.body()).get("situacao").asText());
        assertEquals(5, json.readTree(enviar("GET", "/api/solicitacoes/" + id + "/historico", null).body()).size());
    }

    @Test
    void bloqueiaRemocaoDeVinculoEInativacaoDePontoComSolicitacaoAberta() throws Exception {
        Cenario cenario = prepararCenario();
        HttpResponse<String> criada = criarSolicitacao(cenario);
        assertEquals(201, criada.statusCode(), criada.body());

        assertEquals(409, enviar("DELETE", "/api/pontos/" + cenario.pontoId()
                + "/materiais/" + cenario.materialKgId(), null).statusCode());
        assertEquals(409, enviar("PATCH", "/api/pontos/" + cenario.pontoId() + "/inativacao", null).statusCode());
    }


    @Test
    void rejeitaMaterialNaoAceitoSemGravacaoParcial() throws Exception {
        Cenario cenario = prepararCenario();
        String sufixo = UUID.randomUUID().toString().substring(0, 8);
        HttpResponse<String> materialNaoVinculado = enviar("POST", "/api/materiais",
                "{\"nome\":\"Vidro não aceito " + sufixo
                        + "\",\"unidadeMedida\":\"kg\"}");
        assertEquals(201, materialNaoVinculado.statusCode(), materialNaoVinculado.body());
        long materialId = json.readTree(materialNaoVinculado.body()).get("id").asLong();

        String corpo = "{\"moradorId\":" + cenario.moradorId()
                + ",\"enderecoId\":" + cenario.enderecoId()
                + ",\"pontoId\":" + cenario.pontoId()
                + ",\"itens\":["
                + "{\"materialId\":" + cenario.materialKgId()
                + ",\"unidade\":\"kg\",\"quantidadeEstimada\":1},"
                + "{\"materialId\":" + materialId
                + ",\"unidade\":\"kg\",\"quantidadeEstimada\":2}]}";
        HttpResponse<String> rejeitada = enviar("POST", "/api/solicitacoes", corpo);
        assertEquals(409, rejeitada.statusCode(), rejeitada.body());

        HttpResponse<String> listagem = enviar("GET", "/api/solicitacoes?pontoId="
                + cenario.pontoId() + "&size=100", null);
        assertEquals(200, listagem.statusCode(), listagem.body());
        assertEquals(0, json.readTree(listagem.body()).get("totalElements").asInt());
    }

    @Test
    void falhaAoPersistirItemExecutaRollbackDaSolicitacaoEItens() throws Exception {
        Cenario cenario = prepararCenario(false);
        String corpo = "{\"moradorId\":" + cenario.moradorId()
                + ",\"enderecoId\":" + cenario.enderecoId()
                + ",\"pontoId\":" + cenario.pontoId()
                + ",\"itens\":["
                + "{\"materialId\":" + cenario.materialKgId()
                + ",\"unidade\":\"kg\",\"quantidadeEstimada\":1.5},"
                + "{\"materialId\":" + cenario.materialPecaId()
                + ",\"unidade\":\"unidade\",\"quantidadeEstimada\":1000000000000}]}";

        // O segundo item excede NUMERIC(14,3) somente ao chegar ao banco.
        // A solicitação já foi inserida dentro da transação; o erro deve reverter tudo.
        HttpResponse<String> falha = enviar("POST", "/api/solicitacoes", corpo);
        assertEquals(409, falha.statusCode(), falha.body());

        HttpResponse<String> listagem = enviar("GET", "/api/solicitacoes?pontoId="
                + cenario.pontoId() + "&size=100", null);
        assertEquals(200, listagem.statusCode(), listagem.body());
        assertEquals(0, json.readTree(listagem.body()).get("totalElements").asInt());
    }

    @Test
    void cancelamentoRespeitaAsSituacoesPermitidas() throws Exception {
        Cenario solicitacaoPendente = prepararCenario();
        JsonNode criada = json.readTree(criarSolicitacao(solicitacaoPendente).body());
        long id = criada.get("id").asLong();
        long versao = criada.get("versao").asLong();

        HttpResponse<String> semMotivo = enviar("PATCH", "/api/solicitacoes/" + id + "/cancelar",
                "{\"motivo\":\" \",\"versao\":" + versao + "}");
        assertEquals(400, semMotivo.statusCode(), semMotivo.body());

        HttpResponse<String> cancelada = enviar("PATCH", "/api/solicitacoes/" + id + "/cancelar",
                "{\"motivo\":\"Desistência do morador\",\"versao\":" + versao + "}");
        assertEquals(200, cancelada.statusCode(), cancelada.body());
        JsonNode resultado = json.readTree(cancelada.body());
        assertEquals("CANCELADA", resultado.get("situacao").asText());

        HttpResponse<String> tentativaReabrir = enviar("POST", "/api/solicitacoes/" + id + "/iniciar",
                "{\"versao\":" + resultado.get("versao").asLong() + "}");
        assertEquals(409, tentativaReabrir.statusCode());

        Cenario emExecucao = prepararCenario();
        JsonNode criadaExecucao = json.readTree(criarSolicitacao(emExecucao).body());
        long idExecucao = criadaExecucao.get("id").asLong();
        String data = OffsetDateTime.now(ZoneId.of("America/Maceio")).plusDays(2).withNano(0).toString();
        HttpResponse<String> agendada = enviar("POST", "/api/solicitacoes/" + idExecucao + "/agendar",
                "{\"dataAgendamento\":\"" + data + "\",\"versao\":"
                        + criadaExecucao.get("versao").asLong() + "}");
        assertEquals(200, agendada.statusCode(), agendada.body());
        JsonNode agenda = json.readTree(agendada.body());
        HttpResponse<String> iniciada = enviar("POST", "/api/solicitacoes/" + idExecucao + "/iniciar",
                "{\"versao\":" + agenda.get("versao").asLong() + "}");
        assertEquals(200, iniciada.statusCode(), iniciada.body());
        JsonNode execucao = json.readTree(iniciada.body());
        HttpResponse<String> cancelarEmExecucao = enviar("PATCH", "/api/solicitacoes/" + idExecucao + "/cancelar",
                "{\"motivo\":\"Não deve cancelar\",\"versao\":"
                        + execucao.get("versao").asLong() + "}");
        assertEquals(409, cancelarEmExecucao.statusCode());
    }


    @Test
    void rejeitaAgendamentoPassadoLimiteExcedidoEQuantidadeFracionariaDePecas() throws Exception {
        Cenario dataPassada = prepararCenario();
        JsonNode criada = json.readTree(criarSolicitacao(dataPassada).body());
        long id = criada.get("id").asLong();
        String ontem = OffsetDateTime.now(ZoneId.of("America/Maceio"))
                .minusDays(1).withNano(0).toString();
        HttpResponse<String> agendamentoInvalido = enviar("POST", "/api/solicitacoes/" + id + "/agendar",
                "{\"dataAgendamento\":\"" + ontem + "\",\"versao\":"
                        + criada.get("versao").asLong() + "}");
        assertEquals(400, agendamentoInvalido.statusCode(), agendamentoInvalido.body());
        assertEquals("SOLICITADA", json.readTree(enviar("GET", "/api/solicitacoes/" + id, null).body())
                .get("situacao").asText());

        Cenario limite = prepararCenario();
        String acimaDoLimite = "{\"moradorId\":" + limite.moradorId()
                + ",\"enderecoId\":" + limite.enderecoId()
                + ",\"pontoId\":" + limite.pontoId()
                + ",\"itens\":["
                + "{\"materialId\":" + limite.materialKgId()
                + ",\"unidade\":\"kg\",\"quantidadeEstimada\":31},"
                + "{\"materialId\":" + limite.materialPecaId()
                + ",\"unidade\":\"unidade\",\"quantidadeEstimada\":4}]}";
        assertEquals(409, enviar("POST", "/api/solicitacoes", acimaDoLimite).statusCode());

        Cenario fracionada = prepararCenario();
        String pecasFracionadas = "{\"moradorId\":" + fracionada.moradorId()
                + ",\"enderecoId\":" + fracionada.enderecoId()
                + ",\"pontoId\":" + fracionada.pontoId()
                + ",\"itens\":["
                + "{\"materialId\":" + fracionada.materialKgId()
                + ",\"unidade\":\"kg\",\"quantidadeEstimada\":1.5},"
                + "{\"materialId\":" + fracionada.materialPecaId()
                + ",\"unidade\":\"unidade\",\"quantidadeEstimada\":2.5}]}";
        assertEquals(400, enviar("POST", "/api/solicitacoes", pecasFracionadas).statusCode());
    }

    @Test
    void rejeitaConclusaoSemNenhumaQuantidadeEfetivaPositiva() throws Exception {
        Cenario cenario = prepararCenario();
        JsonNode criada = json.readTree(criarSolicitacao(cenario).body());
        long id = criada.get("id").asLong();
        String data = OffsetDateTime.now(ZoneId.of("America/Maceio")).plusDays(2).withNano(0).toString();
        HttpResponse<String> agendada = enviar("POST", "/api/solicitacoes/" + id + "/agendar",
                "{\"dataAgendamento\":\"" + data + "\",\"versao\":"
                        + criada.get("versao").asLong() + "}");
        assertEquals(200, agendada.statusCode(), agendada.body());
        JsonNode agenda = json.readTree(agendada.body());
        HttpResponse<String> iniciada = enviar("POST", "/api/solicitacoes/" + id + "/iniciar",
                "{\"versao\":" + agenda.get("versao").asLong() + "}");
        assertEquals(200, iniciada.statusCode(), iniciada.body());
        JsonNode execucao = json.readTree(iniciada.body());
        String corpo = "{\"versao\":" + execucao.get("versao").asLong() + ",\"itens\":["
                + "{\"itemId\":" + itemIdPorMaterial(execucao, cenario.materialKgId())
                + ",\"quantidadeEfetiva\":0},"
                + "{\"itemId\":" + itemIdPorMaterial(execucao, cenario.materialPecaId())
                + ",\"quantidadeEfetiva\":0}]}";
        HttpResponse<String> rejeitada = enviar("PATCH", "/api/solicitacoes/" + id + "/concluir", corpo);
        assertEquals(409, rejeitada.statusCode(), rejeitada.body());
        assertEquals("EM_RECOLHIMENTO", json.readTree(enviar("GET", "/api/solicitacoes/" + id, null).body())
                .get("situacao").asText());
    }

    @Test
    void atualizacoesConcorrentesComMesmaVersaoGeramUmSucessoEUmConflito() throws Exception {
        Cenario cenario = prepararCenario();
        HttpResponse<String> criada = criarSolicitacao(cenario);
        assertEquals(201, criada.statusCode(), criada.body());
        JsonNode solicitacao = json.readTree(criada.body());

        String dataFutura = OffsetDateTime.now(ZoneId.of("America/Maceio"))
                .plusDays(2).withNano(0).toString();
        HttpResponse<String> agendada = enviar("POST", "/api/solicitacoes/" + solicitacao.get("id").asLong() + "/agendar",
                "{\"dataAgendamento\":\"" + dataFutura + "\",\"versao\":"
                        + solicitacao.get("versao").asLong() + "}");
        assertEquals(200, agendada.statusCode(), agendada.body());

        long id = solicitacao.get("id").asLong();
        String corpo = "{\"versao\":" + json.readTree(agendada.body()).get("versao").asLong() + "}";
        CompletableFuture<HttpResponse<String>> primeira = CompletableFuture.supplyAsync(() -> {
            try { return enviar("POST", "/api/solicitacoes/" + id + "/iniciar", corpo); }
            catch (Exception ex) { throw new RuntimeException(ex); }
        });
        CompletableFuture<HttpResponse<String>> segunda = CompletableFuture.supplyAsync(() -> {
            try { return enviar("POST", "/api/solicitacoes/" + id + "/iniciar", corpo); }
            catch (Exception ex) { throw new RuntimeException(ex); }
        });

        CompletableFuture.allOf(primeira, segunda).get(20, TimeUnit.SECONDS);
        int statusPrimeira = primeira.join().statusCode();
        int statusSegunda = segunda.join().statusCode();
        assertTrue((statusPrimeira == 200 && statusSegunda == 409)
                        || (statusPrimeira == 409 && statusSegunda == 200),
                "Esperava exatamente um sucesso e um conflito, mas recebeu "
                        + statusPrimeira + " e " + statusSegunda);
    }

    @Test
    void resumoGerencialUsaUnidadeHistoricaDoItem() throws Exception {
        Cenario cenario = prepararCenario();
        HttpResponse<String> criada = criarSolicitacao(cenario);
        assertEquals(201, criada.statusCode(), criada.body());
        JsonNode solicitacao = json.readTree(criada.body());
        long id = solicitacao.get("id").asLong();

        String dataAgendamento = OffsetDateTime.now(ZoneId.of("America/Maceio"))
                .plusDays(2).withNano(0).toString();
        HttpResponse<String> agendada = enviar("POST", "/api/solicitacoes/" + id + "/agendar",
                "{\"dataAgendamento\":\"" + dataAgendamento + "\",\"versao\":"
                        + solicitacao.get("versao").asLong() + "}");
        assertEquals(200, agendada.statusCode(), agendada.body());
        JsonNode agenda = json.readTree(agendada.body());

        HttpResponse<String> iniciada = enviar("POST", "/api/solicitacoes/" + id + "/iniciar",
                "{\"versao\":" + agenda.get("versao").asLong() + "}");
        assertEquals(200, iniciada.statusCode(), iniciada.body());
        JsonNode execucao = json.readTree(iniciada.body());
        long itemKg = itemIdPorMaterial(execucao, cenario.materialKgId());
        long itemPeca = itemIdPorMaterial(execucao, cenario.materialPecaId());
        String conclusao = "{\"versao\":" + execucao.get("versao").asLong() + ",\"itens\":["
                + "{\"itemId\":" + itemKg + ",\"quantidadeEfetiva\":2.5},"
                + "{\"itemId\":" + itemPeca + ",\"quantidadeEfetiva\":3}]}";
        HttpResponse<String> concluida = enviar("PATCH", "/api/solicitacoes/" + id + "/concluir", conclusao);
        assertEquals(200, concluida.statusCode(), concluida.body());

        HttpResponse<String> material = enviar("GET", "/api/materiais/" + cenario.materialKgId(), null);
        assertEquals(200, material.statusCode(), material.body());
        JsonNode materialAtual = json.readTree(material.body());
        String atualizarUnidade = "{\"nome\":\"" + materialAtual.get("nome").asText()
                + "\",\"descricao\":null,\"unidadeMedida\":\"ton\",\"orientacaoPreparo\":null}";
        HttpResponse<String> atualizado = enviar("PUT", "/api/materiais/" + cenario.materialKgId(), atualizarUnidade);
        assertEquals(200, atualizado.statusCode(), atualizado.body());

        String inicio = OffsetDateTime.now(ZoneId.of("America/Maceio")).minusDays(1).withNano(0).toString();
        String fim = OffsetDateTime.now(ZoneId.of("America/Maceio")).plusDays(1).withNano(0).toString();
        HttpResponse<String> resumo = enviar("GET", "/api/solicitacoes/resumo-gerencial?inicio="
                + inicio + "&fim=" + fim, null);
        assertEquals(200, resumo.statusCode(), resumo.body());
        JsonNode totais = json.readTree(resumo.body()).get("totaisPorMaterial");
        boolean encontrouUnidadeHistorica = false;
        for (JsonNode total : totais) {
            if (total.get("materialId").asLong() == cenario.materialKgId()) {
                encontrouUnidadeHistorica = true;
                assertEquals("kg", total.get("unidade").asText());
                assertEquals(2.5, total.get("totalRecolhido").decimalValue().doubleValue(), 0.0001);
            }
        }
        assertTrue(encontrouUnidadeHistorica, "O resumo deve conter o material recolhido.");
    }

    private JsonNode itemPorMaterial(JsonNode solicitacao, long materialId) {
        for (JsonNode item : solicitacao.get("itens")) {
            if (item.get("materialId").asLong() == materialId) {
                return item;
            }
        }
        throw new AssertionError("Item não encontrado para o material " + materialId);
    }

    private long itemIdPorMaterial(JsonNode solicitacao, long materialId) {
        for (JsonNode item : solicitacao.get("itens")) {
            if (item.get("materialId").asLong() == materialId) {
                return item.get("id").asLong();
            }
        }
        throw new AssertionError("Item não encontrado para o material " + materialId);
    }

    private HttpResponse<String> criarSolicitacao(Cenario cenario) throws Exception {
        String corpo = "{\"moradorId\":" + cenario.moradorId()
                + ",\"enderecoId\":" + cenario.enderecoId()
                + ",\"pontoId\":" + cenario.pontoId()
                + ",\"observacoes\":\"Teste de fluxo\",\"itens\":["
                + "{\"materialId\":" + cenario.materialKgId() + ",\"unidade\":\"kg\",\"quantidadeEstimada\":1.5},"
                + "{\"materialId\":" + cenario.materialPecaId() + ",\"unidade\":\"unidade\",\"quantidadeEstimada\":4}]}";
        return enviar("POST", "/api/solicitacoes", corpo);
    }

    private Cenario prepararCenario() throws Exception {
        return prepararCenario(true);
    }

    private Cenario prepararCenario(boolean comLimite) throws Exception {
        String sufixo = UUID.randomUUID().toString().substring(0, 8);
        HttpResponse<String> morador = enviar("POST", "/api/moradores",
                "{\"nome\":\"Morador " + sufixo + "\",\"email\":\"morador-" + sufixo + "@example.org\"}");
        assertEquals(201, morador.statusCode(), morador.body());
        long moradorId = json.readTree(morador.body()).get("id").asLong();

        HttpResponse<String> endereco = enviar("POST", "/api/moradores/" + moradorId + "/enderecos",
                "{\"logradouro\":\"Rua de Teste\",\"numero\":\"10\",\"semNumero\":false,"
                        + "\"bairro\":\"Centro\",\"municipio\":\"Lagarto\",\"uf\":\"SE\",\"cep\":\"49400-000\"}");
        assertEquals(201, endereco.statusCode(), endereco.body());
        long enderecoId = json.readTree(endereco.body()).get("id").asLong();

        HttpResponse<String> kg = enviar("POST", "/api/materiais",
                "{\"nome\":\"Papel " + sufixo + "\",\"unidadeMedida\":\"kg\"}");
        assertEquals(201, kg.statusCode(), kg.body());
        long materialKgId = json.readTree(kg.body()).get("id").asLong();

        HttpResponse<String> peca = enviar("POST", "/api/materiais",
                "{\"nome\":\"Lata " + sufixo + "\",\"unidadeMedida\":\"unidade\"}");
        assertEquals(201, peca.statusCode(), peca.body());
        long materialPecaId = json.readTree(peca.body()).get("id").asLong();

        HttpResponse<String> ponto = enviar("POST", "/api/pontos",
                "{\"nome\":\"Ponto de Teste " + sufixo + "\",\"endereco\":\"Rua de Teste, Centro, Lagarto-SE\","
                        + "\"ativo\":true,\"recolhimentoDomiciliar\":true}");
        assertEquals(201, ponto.statusCode(), ponto.body());
        long pontoId = json.readTree(ponto.body()).get("id").asLong();

        String vinculoKg = comLimite
                ? "{\"observacoes\":\"Aceita papel\",\"limitePorSolicitacao\":30}"
                : "{\"observacoes\":\"Aceita papel\"}";
        String vinculoPeca = comLimite
                ? "{\"observacoes\":\"Aceita latas\",\"limitePorSolicitacao\":30}"
                : "{\"observacoes\":\"Aceita latas\"}";
        assertEquals(201, enviar("POST", "/api/pontos/" + pontoId + "/materiais/" + materialKgId,
                vinculoKg).statusCode());
        assertEquals(201, enviar("POST", "/api/pontos/" + pontoId + "/materiais/" + materialPecaId,
                vinculoPeca).statusCode());

        return new Cenario(moradorId, enderecoId, pontoId, materialKgId, materialPecaId);
    }

    private record Cenario(long moradorId, long enderecoId, long pontoId,
                           long materialKgId, long materialPecaId) { }
}
