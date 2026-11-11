# Sistema de Coleta Seletiva

Projeto acadêmico da disciplina Programação Web I — Bacharelado em Sistemas de Informação, IFS Campus Lagarto.

## Escopo desta versão

Esta versão concentra-se no backend: cadastros, consulta de pontos, operadores, solicitações de recolhimento, regras de negócio, persistência PostgreSQL e histórico. A interface web ainda não está incluída nesta etapa.

## Tecnologias

- Java 21
- Spring Boot 4.1.2
- Spring Web MVC
- Spring Data JPA / Hibernate
- Jakarta Bean Validation
- PostgreSQL
- Maven Wrapper

## Pré-requisitos

1. JDK 21 instalado e disponível no `PATH`.
2. PostgreSQL instalado e em execução.
3. Banco criado, por exemplo `coleta_seletiva`.


## Executar o backend

No terminal, entre na pasta `backend`:

```powershell
cd .\coleta-seletiva\backend
```

Inicie a aplicação no Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

A API ficará disponível por padrão em `http://localhost:8080`.

## Executar os testes

Com o PostgreSQL configurado e as variáveis de ambiente exportadas no mesmo terminal:

```powershell
.\mvnw.cmd test
```

Os testes HTTP existentes e os testes de fluxo usam um banco PostgreSQL acessível. Execute-os preferencialmente em um banco de desenvolvimento/testes, nunca em uma base com dados de produção.

## Registro desta versão

- Resumo das correções e limites de validação: `docs/ALTERACOES-IMPLEMENTADAS.md`.

## Documentação da API

- Especificação OpenAPI: `docs/openapi.yaml`.
- Guia funcional e exemplos de fluxo: `docs/API.md`.

## Endpoints principais

| Recurso | Métodos e caminhos |
|---|---|
| Moradores | `POST /api/moradores`; `GET/PUT /api/moradores/{id}` |
| Endereços | `POST/GET /api/moradores/{moradorId}/enderecos`; `PUT /api/enderecos/{id}` |
| Materiais | `POST/GET/PUT /api/materiais`; `PATCH /api/materiais/{id}/inativacao` |
| Pontos | `POST/GET/PUT /api/pontos`; `PATCH /api/pontos/{id}/inativacao` |
| Materiais aceitos | `POST/GET/DELETE /api/pontos/{pontoId}/materiais/{materialId}` |
| Operadores | `POST/GET/PUT /api/operadores`; vínculos em `/api/operadores/{operadorId}/pontos/{pontoId}` |
| Solicitações | `POST/GET /api/solicitacoes`; `GET/PUT /api/solicitacoes/{id}` |
| Atendimento | `POST /api/solicitacoes/{id}/agendar`, `/recusar`, `/iniciar`; `PATCH .../cancelar`, `/concluir`, `/frustrada`, `/reagendar` |
| Histórico e resumo | `GET /api/solicitacoes/{id}/historico`; `GET /api/solicitacoes/resumo-gerencial` |

## Fluxo de atendimento e versão

1. Criar solicitação: situação `SOLICITADA`.
2. Agendar: `SOLICITADA` → `AGENDADA`.
3. Iniciar: `AGENDADA` → `EM_RECOLHIMENTO`.
4. Concluir: `EM_RECOLHIMENTO` → `CONCLUIDA`.
5. Se a tentativa não tiver sucesso: `EM_RECOLHIMENTO` → `AGENDADA`, com justificativa e nova data futura.

O resumo gerencial usa a unidade copiada no item da solicitação, preservando a unidade registrada no momento do pedido mesmo que o cadastro do material seja alterado depois.

Também são permitidos recusa de `SOLICITADA` e cancelamento de `SOLICITADA` ou `AGENDADA`. `CONCLUIDA`, `RECUSADA` e `CANCELADA` são estados finais.

As respostas de solicitação incluem `versao`. Toda operação de alteração de situação exige que o cliente envie a versão recebida, para evitar atualização sobre dados desatualizados. Datas de eventos e datas enviadas à API usam ISO 8601 com offset, por exemplo `2026-11-20T10:30:00-03:00`.

## Limitações conhecidas

- O projeto ainda não implementa autenticação Spring Security/JWT. O cabeçalho `X-Usuario-Responsavel` identifica textualmente o responsável no histórico, mas **não autentica nem autoriza** o usuário. Os dados de demonstração devem ser fictícios.
- A listagem de solicitações aceita `moradorId` e `pontoId` como filtros de contexto para a demonstração. Esses parâmetros não autenticam nem autorizam o usuário; sem Spring Security/JWT, um cliente pode informar outro ID. A API não deve ser usada em produção com dados pessoais reais.
- Os testes de integração precisam de PostgreSQL configurado. A execução em outro ambiente deve ser registrada com data, versão do Java, banco e resultado.
- A interface web responsiva pertence a uma etapa posterior do trabalho.

## Estrutura

```text
coleta-seletiva/
├── backend/
│   ├── src/main/java/br/edu/ifs/coletaseletiva/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── enums/
│   │   ├── repository/
│   │   └── service/
│   └── src/main/resources/
│       ├── application.properties
│       └── schema.sql
└── docs/
    ├── API.md
    └── openapi.yaml
```
