# Relatorio de Validacao - clinica-medica

Data da rodada: 2026-05-26
Status geral: Aprovado com ressalvas

## Resumo executivo

A aplicacao compilou, os testes Maven passaram, os tres microsservicos subiram via Docker, os health checks ficaram `UP`, Swagger/OpenAPI respondeu em todos os servicos e todas as colecoes Postman existentes foram executadas com Newman sem falhas.

Foram encontrados dois pontos de melhoria fora dos fluxos principais: uma URL malformada em `agendamento` retorna `500` em vez de erro de cliente, e `ConvenioRequest.cnpj` sem validacao de tamanho/formato permite chegar em erro de integridade do banco.

## Ambiente usado

- Java: 17.0.12
- Maven: 3.9.16 em `C:\Users\pedro\Tools\apache-maven-3.9.16\bin\mvn.cmd`
- Docker: 29.4.3
- Docker Compose: `docker-compose` standalone 5.1.3
- Node: v24.15.0
- npm: 11.13.0
- Newman: 6.2.2 via `npx -y newman`
- `.env`: existente, com `DB_USER` e `DB_PASS` preenchidos. Valores nao foram expostos.

Observacoes de ambiente:

- `mvn` nao esta no PATH.
- `docker compose` nao esta disponivel como subcomando; foi usado `docker-compose`.
- Comandos Docker precisaram permissao elevada para acessar o Docker Engine.
- `kubectl` esta instalado, mas nao havia API server local disponivel em `localhost:8080`.

## Comandos executados

Principais comandos:

```bash
C:\Users\pedro\Tools\apache-maven-3.9.16\bin\mvn.cmd -q -DskipTests compile
C:\Users\pedro\Tools\apache-maven-3.9.16\bin\mvn.cmd clean test
docker-compose config
docker-compose up -d --build
docker-compose ps
curl.exe -i http://localhost:8081/actuator/health
curl.exe -i http://localhost:8082/actuator/health
curl.exe -i http://localhost:8083/actuator/health
npx -y newman run docs/<collection>.json -e docs/local.postman_environment.json --reporters cli,json --reporter-json-export target/newman/<collection>.json
kubectl apply --dry-run=client -f k8s\
kubectl apply --dry-run=client --validate=false --recursive -f k8s\
```

## Maven

Resultado: sucesso.

- `mvn -q -DskipTests compile`: passou.
- `mvn clean test`: passou.
- Total: 134 testes, 0 falhas, 0 erros.
- `administrativo`: 76 testes.
- `atendimento`: 27 testes.
- `agendamento`: 31 testes.
- `commons`: sem testes.

Avisos observados:

- Maven usou encoding de plataforma em alguns modulos.
- Hibernate avisou que `MySQLDialect` nao precisa ser definido explicitamente.

## Docker e health checks

Resultado: sucesso.

- `docker-compose config`: valido.
- `docker-compose up -d --build`: imagens construidas e containers iniciados.
- Bancos MySQL: `healthy`.
- Servicos Java: `Up`.

Health checks:

- `administrativo`: `200 {"status":"UP"}`
- `agendamento`: `200 {"status":"UP"}`
- `atendimento`: `200 {"status":"UP"}`

Os primeiros curls receberam `Empty reply from server` enquanto os apps ainda inicializavam. Apos aguardar, os tres health checks passaram.

## Swagger e OpenAPI

Resultado: sucesso.

- `http://localhost:8081/v3/api-docs`: 200 `application/json`
- `http://localhost:8081/swagger-ui/index.html`: 200 `text/html`
- `http://localhost:8082/v3/api-docs`: 200 `application/json`
- `http://localhost:8082/swagger-ui/index.html`: 200 `text/html`
- `http://localhost:8083/v3/api-docs`: 200 `application/json`
- `http://localhost:8083/swagger-ui/index.html`: 200 `text/html`

## Postman e Newman

Foi criado o environment local:

- `docs/local.postman_environment.json`

Relatorios JSON gerados em:

- `target/newman/`

Resultado por colecao:

| Colecao | Requests | Falhas request | Assertions | Falhas assertion |
| --- | ---: | ---: | ---: | ---: |
| atendente | 8 | 0 | 15 | 0 |
| convenio | 12 | 0 | 21 | 0 |
| especialidade | 9 | 0 | 15 | 0 |
| medico | 23 | 0 | 40 | 0 |
| paciente | 14 | 0 | 24 | 0 |
| consulta | 34 | 0 | 53 | 0 |
| atendimento | 18 | 0 | 30 | 0 |
| atendimento-notificacao | 11 | 0 | 15 | 0 |
| relatorios | 4 | 0 | 11 | 0 |

Total Newman:

- 133 requests.
- 224 assertions.
- 0 falhas.

## Rotas e fluxos validados

Administrativo:

- Atendentes: criar, listar, atualizar, excluir, duplicidade e validacao.
- Convenios: criar, listar, buscar, atualizar, status, excluir, 404 e validacao.
- Especialidades: criar, listar, buscar, atualizar, excluir, 404 e validacao.
- Medicos: criar, listar, ativos, buscar, atualizar, associar/remover especialidade, inativar, excluir, duplicidade e validacao.
- Pacientes: criar, listar, filtros, buscar, atualizar, excluir, 404 e validacao.
- Relatorios: consultas diarias e pacientes por convenio.

Agendamento:

- Criar consulta.
- Listar por medico, paciente, data e medico+data.
- `minha-agenda`.
- Confirmar.
- Reagendar.
- Cancelar.
- Conflito de horario.
- Convenio inexistente/inativo.
- Data passada.
- Consulta cancelada.
- Consulta inexistente.
- `PATCH /v1/consultas/{id}/realizar` validado manualmente: `PENDENTE -> REALIZADA`.
- ID invalido `abc` em `PATCH /v1/consultas/{id}/realizar`: `400`.

Atendimento:

- Criar atendimento.
- Criar prontuario.
- Buscar prontuario por consulta.
- Criar/listar anotacoes.
- Criar/listar exames.
- Historico por paciente.
- Atendimento duplicado.
- Consulta cancelada.
- Consulta inexistente.
- Notificacao atendimento -> agendamento validada: consulta ficou `REALIZADA`.

## Kubernetes

Resultado: bloqueado por ambiente.

Arquivos YAML existem em `k8s/`, incluindo namespace, ingress, secrets de exemplo, configmaps, deployments, services e bancos.

O comando `kubectl apply --dry-run=client -f k8s\` falhou porque `kubectl` tentou baixar OpenAPI de `localhost:8080`. A repeticao com `--validate=false --recursive` tambem tentou consultar a API do cluster. Nao havia API server local disponivel, entao a validacao Kubernetes nao foi concluida nesta rodada.

## Bugs encontrados

### BUG-001 - URL malformada em consultas retorna 500

- Severidade: Media
- Modulo: agendamento
- Endpoint/comando: `PATCH http://localhost:8082/v1/consultas//realizar`
- Ambiente: Docker Compose local
- Passos para reproduzir:
  - Executar `curl.exe -i -X PATCH http://localhost:8082/v1/consultas//realizar`
- Resultado esperado:
  - `400 Bad Request` ou `404 Not Found`, por ser uma URL sem ID valido.
- Resultado obtido:
  - `500 Internal Server Error`
  - Body: `{"status":500,"error":"Internal Server Error","message":"Erro interno do servidor"}`
- Evidencia:
  - Logs do `agendamento` registraram `NoResourceFoundException: No static resource v1/consultas/realizar`.
- Hipotese tecnica:
  - `NoResourceFoundException` cai no handler generico de excecao e vira 500.
- Sugestao de correcao:
  - Tratar `NoResourceFoundException`/`NoHandlerFoundException` como 404 no `GlobalExceptionHandler`, ou ajustar configuracao de erro para nao converter recurso inexistente em erro interno.

### BUG-002 - CNPJ longo em convenio chega ao banco e retorna 409 generico

- Severidade: Baixa
- Modulo: administrativo
- Endpoint/comando: `POST /v1/convenios`
- Ambiente: Docker Compose local
- Passos para reproduzir:
  - Enviar payload com `cnpj` maior que o tamanho aceito pela coluna.
- Resultado esperado:
  - `400 Bad Request` com mensagem de validacao de campo.
- Resultado obtido:
  - `409 Conflict`
  - Body generico: `Violacao de integridade: registro duplicado ou referencia invalida`
  - Log SQL: `Data too long for column 'cnpj'`.
- Evidencia:
  - Logs do `administrativo` registraram `SQL Error: 1406` e `Data truncation`.
- Hipotese tecnica:
  - `ConvenioRequest` valida apenas `@NotBlank` em `cnpj`; tamanho/formato nao sao barrados antes da persistencia.
- Sugestao de correcao:
  - Adicionar validacao de tamanho/formato no DTO, por exemplo `@Size` e/ou `@Pattern`, e retornar erro 400 padronizado.

## Riscos e lacunas

- Validacao Kubernetes ficou bloqueada por falta de cluster/API server local.
- As bases Docker ja tinham volumes existentes; as colecoes passaram mesmo assim, mas os testes nao rodaram em banco zerado.
- Nao foi feito teste de carga, concorrencia ou resiliencia prolongada.
- A validacao de indisponibilidade real entre microsservicos nao foi executada nesta rodada para evitar parar containers no meio da suite.
- Logs contem warnings esperados de testes negativos e warnings tecnicos de configuracao, mas sem falha nos fluxos principais.

## Evidencias

- Relatorios Newman: `target/newman/*.json`
- Environment Newman: `docs/local.postman_environment.json`
- Surefire: `administrativo/target/surefire-reports/`, `agendamento/target/surefire-reports/`, `atendimento/target/surefire-reports/`
- Containers verificados com `docker-compose ps`
- Health checks HTTP dos tres servicos retornaram `UP`

## Proximas acoes recomendadas

1. Corrigir `BUG-001` no handler global do `agendamento`.
2. Adicionar validacao de tamanho/formato para `ConvenioRequest.cnpj`.
3. Adicionar `mvn` ao PATH ou documentar Maven local usado no projeto.
4. Padronizar `project.build.sourceEncoding=UTF-8` no `pom.xml`.
5. Executar a suite em banco zerado antes de release.
6. Reexecutar validacao Kubernetes com um cluster local ativo.

