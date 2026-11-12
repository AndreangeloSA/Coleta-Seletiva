# Coleta-Seletiva
Projeto das etapas 1 e 2 de Programação Web I do IFS. A API reúne pontos de coleta, moradores e operadores para solicitar, agendar e acompanhar recolhimentos.

## Executar
Instale JDK 21 e PostgreSQL e crie um banco vazio chamado `coleta_seletiva`. O Maven Wrapper baixa o Maven e as dependências na primeira execução.

Na raiz do repositório, use PowerShell. Informe o usuário que possui permissão para criar e alterar tabelas no banco escolhido.

```powershell
$env:DB_URL = 'jdbc:postgresql://localhost:5432/coleta_seletiva'

$env:DB_USERNAME = Read-Host 'Usuário PostgreSQL'
digite seu usuário

$env:DB_PASSWORD = Read-Host 'Senha PostgreSQL' -MaskInput
digite sua senha

cd backend
.\mvnw.cmd spring-boot:run
```

O comando de senha requer PowerShell 7. No Linux ou macOS, exporte as mesmas três variáveis e execute `sh mvnw spring-boot:run` em `backend/`. Flyway aplica as migrações automaticamente. A API responde em `http://localhost:8081`.

## Dados de demonstração
Com as migrações aplicadas, execute a partir da raiz, em outro terminal, substituindo USUARIO pelo usuário do banco:

```sh
psql -h localhost -U USUARIO -d coleta_seletiva -v ON_ERROR_STOP=1 -f scripts/demonstracao.sql
```

O script imprime o ID de Lia. Use a documentação da API para consultar o ponto de Lagarto, solicitar o recolhimento e percorrer o fluxo de atendimento com dados fictícios.

## Testes
Crie um banco separado, por exemplo `coleta_seletiva_test`, aponte `DB_URL` para ele e execute `.\mvnw.cmd test` no diretório backend. A suíte usa HTTP real e PostgreSQL, cria registros e testa rollback e concorrência. Os resultados ficam em `backend/target/surefire-reports`.

## Organização
- `backend/` contém a API Spring Boot 3.2.5, o Maven Wrapper, os testes e as migrações.
- `scripts/` contém a carga fictícia.
- `docs/` contém o [contrato OpenAPI](docs/openapi.yaml), o [guia da API](docs/API.md) e o [relatório com o modelo de dados](docs/RELATORIO.md).

Use esta pasta como raiz do repositório Git. Dados locais do PostgreSQL, credenciais, metadados de IDE e saídas de compilação ficam fora do controle de versão.


