# Guia da API — Sistema de Coleta Seletiva

Base local: `http://localhost:8080`. Todas as operações usam JSON. A especificação OpenAPI complementar está em `docs/openapi.yaml`.

## Regras gerais

- Criações retornam HTTP 201; consultas e alterações bem-sucedidas retornam HTTP 200; exclusão de vínculo retorna HTTP 204.
- Dados inválidos retornam HTTP 400; registro inexistente retorna HTTP 404; conflito de regra, integridade ou versão retorna HTTP 409.
- Erros seguem o formato `{ "dataHora", "status", "codigo", "mensagem", "erros" }`. Cada item em `erros` contém `campo` e `mensagem`. A resposta não expõe stack trace.
- Listagens de pontos e solicitações limitam o tamanho da página a 100.
- Datas de eventos usam ISO 8601 com offset, por exemplo `2026-11-20T10:30:00-03:00`.

## Cadastro e catálogo

| Método | Caminho | Descrição |
|---|---|---|
| POST | `/api/moradores` | Cadastra morador. |
| GET | `/api/moradores/{id}` | Consulta morador. |
| PUT | `/api/moradores/{id}` | Atualiza morador. |
| POST | `/api/moradores/{moradorId}/enderecos` | Cadastra endereço do morador. |
| GET | `/api/moradores/{moradorId}/enderecos` | Lista endereços do morador. |
| PUT | `/api/enderecos/{id}` | Atualiza endereço. |
| POST | `/api/materiais` | Cadastra material. |
| GET | `/api/materiais` e `/api/materiais/{id}` | Lista/consulta materiais. |
| PUT | `/api/materiais/{id}` | Atualiza material. |
| PATCH | `/api/materiais/{id}/inativacao` | Inativa material sem exclusão física. |
| POST | `/api/pontos` | Cadastra ponto. |
| GET | `/api/pontos` | Consulta pontos ativos; filtros `nome`, `municipio`, `bairro`, `materialId`, `material`, `pagina`, `tamanho`. |
| GET | `/api/pontos/{id}` | Consulta ponto e materiais aceitos. |
| PUT | `/api/pontos/{id}` | Atualiza ponto. |
| PATCH | `/api/pontos/{id}/inativacao` | Inativa ponto, desde que não existam solicitações abertas. |
| POST | `/api/pontos/{pontoId}/materiais/{materialId}` | Vincula material ao ponto. |
| GET | `/api/pontos/{pontoId}/materiais` | Lista vínculos. |
| DELETE | `/api/pontos/{pontoId}/materiais/{materialId}` | Remove vínculo, desde que não existam solicitações abertas afetadas. |
| POST | `/api/operadores` | Cadastra operador. |
| GET | `/api/operadores` e `/api/operadores/{id}` | Lista/consulta operadores. |
| PUT | `/api/operadores/{id}` | Atualiza operador. |
| POST/GET/DELETE | `/api/operadores/{operadorId}/pontos/{pontoId}` | Cria/lista/remove vínculos de operadores e pontos (a listagem usa `/api/operadores/{operadorId}/pontos`). |

## Criar solicitação — RF08

`POST /api/solicitacoes`

Exemplo de corpo:

```json
{
  "moradorId": 1,
  "enderecoId": 1,
  "pontoId": 1,
  "observacoes": "Materiais separados e limpos",
  "itens": [
    { "materialId": 1, "unidade": "kg", "quantidadeEstimada": 2.5 },
    { "materialId": 2, "unidade": "unidade", "quantidadeEstimada": 4 }
  ]
}
```

A API valida o titular do endereço, situação do ponto, recolhimento domiciliar, materiais ativos e aceitos, unidade, quantidade estimada, limite e duplicidade de material. A gravação da solicitação, itens e histórico inicial é transacional.

A resposta inclui `id`, `protocolo`, `situacao`, `dataCriacao`, `dataAgendamento`, `versao`, endereço histórico e itens. Guarde `versao` para as operações seguintes.

## Consultar e corrigir antes do agendamento — RF09/RN04

- `GET /api/solicitacoes`: filtros opcionais `moradorId` (para a seleção demonstrativa do morador), `pontoId` (para a seleção demonstrativa do operador), `situacao`, `inicio`, `fim` (ISO 8601 com offset), além de paginação Spring (`page`, `size`, `sort`). `size` máximo: 100. Os IDs são filtros de demonstração, não autenticação nem autorização; sem Spring Security/JWT, um cliente pode informar outro ID.
- `GET /api/solicitacoes/{id}`: detalhe e itens.
- `GET /api/solicitacoes/{id}/historico`: histórico cronológico.
- `PUT /api/solicitacoes/{id}`: corrige endereço e itens enquanto ainda estiver `SOLICITADA`. Envie os campos de criação e `versao`; morador e ponto não podem ser alterados. A compatibilidade é validada novamente.

## Fluxo de atendimento — RF10 a RF14

Toda operação abaixo exige a `versao` atual. O cabeçalho `X-Usuario-Responsavel` é opcional e serve somente para registrar o nome informado no histórico; não constitui autenticação.

### Agendar — RF10

`POST /api/solicitacoes/{id}/agendar`

```json
{ "dataAgendamento": "2026-11-20T10:30:00-03:00", "versao": 0 }
```

Somente `SOLICITADA` pode ser agendada e a data precisa ser futura. A situação passa a `AGENDADA`.

### Recusar — RF11

`POST /api/solicitacoes/{id}/recusar`

```json
{ "motivo": "O ponto não poderá atender nesta região.", "versao": 0 }
```

Somente `SOLICITADA` pode ser recusada. Justificativa obrigatória. `RECUSADA` é final.

### Cancelar — RF12

`PATCH /api/solicitacoes/{id}/cancelar`

```json
{ "motivo": "O morador não precisa mais do recolhimento.", "versao": 0 }
```

Somente `SOLICITADA` ou `AGENDADA` pode ser cancelada. `CANCELADA` é final.

### Iniciar — RF13

`POST /api/solicitacoes/{id}/iniciar`

```json
{ "versao": 1 }
```

Somente `AGENDADA` passa para `EM_RECOLHIMENTO`.

### Concluir — RF13

`PATCH /api/solicitacoes/{id}/concluir`

```json
{
  "versao": 2,
  "observacoes": "Recolhimento realizado.",
  "itens": [
    { "itemId": 10, "quantidadeEfetiva": 0 },
    { "itemId": 11, "quantidadeEfetiva": 3 }
  ]
}
```

A solicitação precisa estar `EM_RECOLHIMENTO`. Informe todos os itens exatamente uma vez; a quantidade efetiva pode ser zero, não pode ser negativa, e pelo menos uma deve ser positiva. Para unidades de peça (por exemplo, `unidade`/`peça`), a quantidade deve ser inteira.

### Reagendar — RF14

`PATCH /api/solicitacoes/{id}/reagendar`

```json
{
  "motivo": "Solicitado pelo morador.",
  "novaDataHora": "2026-11-22T14:00:00-03:00",
  "versao": 1
}
```

Somente `AGENDADA` pode ser reagendada. O histórico registra a alteração mesmo sem mudança de situação.

### Registrar tentativa sem sucesso — RF14

`PATCH /api/solicitacoes/{id}/frustrada`

```json
{
  "motivo": "Não havia pessoa no endereço.",
  "dadosTentativa": "Primeira tentativa de atendimento.",
  "novaDataHora": "2026-11-22T14:00:00-03:00",
  "versao": 2
}
```

Somente `EM_RECOLHIMENTO` pode retornar para `AGENDADA`. A justificativa e a nova data futura são obrigatórias; a tentativa permanece no histórico.

## Resumo gerencial — RF15

`GET /api/solicitacoes/resumo-gerencial?inicio=2026-11-01T00:00:00-03:00&fim=2026-11-30T23:59:59-03:00`

Retorna a contagem por situação (pela data de criação) e os totais efetivamente recolhidos por material/unidade (pela data do evento de conclusão). Quantidades estimadas não são somadas. Unidades diferentes permanecem separadas.

## Restrições e limitações do protótipo

- O protótipo ainda não possui autenticação/autorização. Portanto, a consulta de solicitações ainda não restringe os dados ao morador ou operador autenticado.
- O nome no cabeçalho de responsável não prova a identidade de quem chamou a API. Não use o protótipo com dados pessoais reais em produção.


## Resumo e integridade histórica

O resumo gerencial agrega quantidades efetivamente recolhidas apenas para solicitações `CONCLUIDA`, agrupando por material e pela unidade copiada no item no momento da solicitação. Assim, uma alteração posterior da unidade no cadastro do material não altera a interpretação do histórico.

Em bancos legados com datas armazenadas como `TIMESTAMP WITHOUT TIME ZONE`, consulte `backend/src/main/resources/db/atualizacoes/migracao_datas_offset.sql`. Esse arquivo é uma migração manual, não executada automaticamente pela inicialização do Spring.
