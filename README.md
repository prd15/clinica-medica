# Clínica Médica — Plataforma de Gestão Clínica

Plataforma de gestão para clínicas médicas construída em arquitetura de microsserviços. O sistema cobre o ciclo completo de operação clínica — cadastros administrativos, agendamento de consultas e atendimento médico — com cada domínio isolado em seu próprio serviço, banco de dados e ciclo de vida de implantação.

A borda da plataforma é protegida por um API Gateway com autenticação OAuth2/JWT delegada ao Keycloak, e a comunicação entre serviços é resiliente, com chamadas assíncronas via Outbox Pattern onde a consistência eventual é aceitável.

---

## Índice

- [Visão geral](#visão-geral)
- [Arquitetura](#arquitetura)
- [Stack tecnológica](#stack-tecnológica)
- [Domínios e serviços](#domínios-e-serviços)
- [Segurança e autenticação](#segurança-e-autenticação)
- [Pré-requisitos](#pré-requisitos)
- [Execução com Docker Compose](#execução-com-docker-compose)
- [Execução local (sem Docker)](#execução-local-sem-docker)
- [Documentação da API (Swagger)](#documentação-da-api-swagger)
- [Testes e qualidade](#testes-e-qualidade)
- [Observabilidade e logs](#observabilidade-e-logs)
- [CI/CD (GitHub Actions)](#cicd-github-actions)
- [Implantação em Kubernetes](#implantação-em-kubernetes)
- [Variáveis de ambiente](#variáveis-de-ambiente)
- [Estrutura do repositório](#estrutura-do-repositório)
- [Decisões arquiteturais](#decisões-arquiteturais)
- [Fluxo de negócio](#fluxo-de-negócio)
- [Convenções de contribuição](#convenções-de-contribuição)

---

## Visão geral

A plataforma é composta por quatro serviços executáveis, uma biblioteca compartilhada e um provedor de identidade:

| Componente | Tipo | Responsabilidade |
|---|---|---|
| **gateway** | API Gateway | Roteamento, validação de JWT na borda |
| **administrativo** | Microsserviço | Convênios, pacientes, médicos, especialidades, atendentes, relatórios |
| **agendamento** | Microsserviço | Consultas e agenda médica |
| **atendimento** | Microsserviço | Atendimento clínico, prontuário, anotações, exames |
| **commons** | Biblioteca | Tipos base, exceções e handlers compartilhados |
| **keycloak** | Identity Provider | Emissão e validação de tokens OAuth2/JWT |

Cada microsserviço é autocontido: entidades, repositories e services residem dentro do próprio serviço. A biblioteca `commons` carrega apenas o que é genuinamente transversal (entidade base de auditoria, exceções de negócio, handler global de erros, configuração do ModelMapper).

---

## Arquitetura

```text
                          ┌──────────────────┐
                          │     Keycloak     │  realm: clinica
                          │       :8180      │  (OAuth2 / JWT)
                          └────────▲─────────┘
                                   │ valida assinatura (JWKS)
                                   │
   Cliente HTTP ──── Bearer JWT ──►┌──────────────────┐
                                   │   API Gateway    │  :8080
                                   │  (Spring Cloud   │
                                   │   Gateway WebFlux)│
                                   └───┬──────┬──────┬─┘
              /api/admin/**           │      │      │   /api/atendimentos/**
              /api/agendamentos/**    │      │      │
                  ┌───────────────────┘      │      └────────────────────┐
                  ▼                           ▼                           ▼
        ┌──────────────────┐      ┌──────────────────┐      ┌──────────────────┐
        │  administrativo  │◄─────│   agendamento    │◄─────│   atendimento    │
        │      :8081       │ Feign│      :8082       │Outbox│      :8083       │
        └────────┬─────────┘      └────────┬─────────┘      └────────┬─────────┘
                 │ JPA                      │ JPA                     │ JPA
                 ▼                          ▼                         ▼
       ┌──────────────────┐      ┌──────────────────┐      ┌──────────────────┐
       │clinica_           │      │clinica_          │      │clinica_          │
       │administrativo     │      │agendamento       │      │atendimento       │
       │ MySQL :3307       │      │ MySQL :3308      │      │ MySQL :3309      │
       └──────────────────┘      └──────────────────┘      └──────────────────┘

Integrações entre serviços:
- agendamento → administrativo : valida paciente, médico e convênio (OpenFeign).
- administrativo → agendamento : contagem de consultas para relatórios (OpenFeign).
- atendimento → agendamento   : marca consulta como realizada (Outbox Pattern).
```

Princípios:

- **Defesa em profundidade** — o gateway valida o JWT na borda e cada microsserviço revalida o token (OAuth2 Resource Server), aplicando autorização por papel no nível do endpoint.
- **Database per service** — sem acoplamento de schema entre domínios; referências cruzadas usam identificadores (`Long id`), nunca chaves estrangeiras entre bancos.
- **Desacoplamento transacional** — a notificação de conclusão de atendimento usa Outbox Pattern, garantindo entrega mesmo com o serviço de destino indisponível.

---

## Stack tecnológica

| Tecnologia | Versão | Papel |
|---|---:|---|
| Java | 17 | Linguagem dos serviços |
| Spring Boot | 3.3.5 | Base das APIs REST |
| Spring Cloud Gateway | 2023.0.3 | API Gateway reativo (WebFlux) |
| Spring Security / OAuth2 Resource Server | Gerenciada pelo Boot | Validação de JWT no gateway e nos serviços |
| Spring Data JPA / Hibernate | Gerenciada pelo Boot | Persistência |
| SLF4J + Logback | Gerenciada pelo Boot | Logging com correlation-id por request |
| Spring Cloud OpenFeign | 2023.0.3 | Comunicação HTTP entre serviços |
| OkHttp | Gerenciada pelo Boot | Cliente HTTP do Feign (timeouts 3s/5s) |
| Keycloak | 24 | Identity Provider (OAuth2/OIDC) |
| MySQL | 8 | Banco por serviço |
| ModelMapper | 3.2.1 | Conversão entidade ↔ DTO |
| Lombok | 1.18.36 | Redução de boilerplate |
| SpringDoc OpenAPI | 2.6.0 | Swagger UI / OpenAPI |
| Maven | 3.9+ | Build multi-módulo |
| Docker / Docker Compose | Atual | Empacotamento e orquestração local |
| Kubernetes | networking.k8s.io/v1 | Implantação em cluster |
| JUnit 5 + Mockito | Gerenciada pelo Boot | Testes unitários |
| Newman | 6.x | Execução automatizada das collections Postman |

---

## Domínios e serviços

### administrativo — `:8081`

Cadastros base e relatórios gerenciais.

- `/v1/convenios` — CRUD + `PATCH /{id}/status`
- `/v1/pacientes` — CRUD
- `/v1/especialidades` — CRUD
- `/v1/atendentes` — CRUD
- `/v1/medicos` — CRUD, `/ativos`, associação de especialidades, `PATCH /{id}/inativar`
- `/v1/relatorios/consultas-diarias`, `/v1/relatorios/pacientes-por-convenio`

### agendamento — `:8082`

Ciclo de vida da consulta (`AGENDADA → CONFIRMADA → REALIZADA / CANCELADA`).

- `POST /v1/consultas`, `GET/DELETE /v1/consultas/{id}`
- `PATCH /v1/consultas/{id}/reagendar | /confirmar | /realizar`
- `GET /v1/consultas` (filtros), `GET /v1/consultas/contagem`, `GET /v1/consultas/minha-agenda`

### atendimento — `:8083`

Atendimento clínico e registros associados (`INICIADO → FINALIZADO / CANCELADO`).

- `POST /v1/atendimentos`, `GET /v1/atendimentos/historico`, `GET /v1/atendimentos/{consultaId}`
- `POST/GET /v1/atendimentos/{id}/anotacoes`
- `POST/GET /v1/atendimentos/{id}/exames`
- Outbox Pattern: persistência atômica do evento + reprocessamento agendado com retry.

Todas as rotas são acessíveis através do gateway com os prefixos `/api/admin/**`, `/api/agendamentos/**` e `/api/atendimentos/**`.

---

## Segurança e autenticação

A partir da Fase 3, todas as rotas de negócio exigem um JWT válido emitido pelo Keycloak. Apenas `/actuator/health` é público.

O Keycloak sobe na porta **8180** e importa automaticamente o realm `clinica` de `keycloak/realm-clinica.json`.
Console administrativo: <http://localhost:8180> (`admin` / `admin`).

### Papéis (realm `clinica`)

| Papel | Representa | Permissões |
|---|---|---|
| `ADMIN` | Administrador | Acesso total |
| `ATENDENTE` | Recepção | Cadastros, agenda, leitura |
| `MEDICO` | Médico | Agenda, atendimento clínico, prontuário |
| `SERVICE` | Comunicação interna | Chamadas Feign entre serviços (Outbox) |

### Usuários de demonstração

| Usuário | Senha | Papel |
|---|---|---|
| `admin` | `Admin123!` | ADMIN |
| `atendente` | `Atend123!` | ATENDENTE |
| `medico` | `Medico123!` | MEDICO |

### Clientes OAuth2

| Client | Tipo | Uso |
|---|---|---|
| `clinica-frontend` | Público | Testes manuais / Postman (`password` grant) |
| `clinica-service` | Confidencial | Comunicação interna (`client_credentials` grant) |

### Obtenção de token e chamada via gateway

```bash
TOKEN=$(curl -s -X POST http://localhost:8180/realms/clinica/protocol/openid-connect/token \
  -d "grant_type=password&client_id=clinica-frontend&username=admin&password=Admin123!" \
  | jq -r .access_token)

# Com token → 200
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/admin/v1/pacientes

# Sem token → 401
curl -i http://localhost:8080/api/admin/v1/pacientes

# Papel insuficiente → 403 (ex.: MEDICO tentando criar convênio)
```

---

## Pré-requisitos

- Java 17 (JDK 17 — versões mais novas são incompatíveis com a versão do Lombok usada).
- Maven 3.9+.
- Docker e Docker Compose.
- `jq` (opcional, para extrair o token nos exemplos).
- Para Kubernetes: `kubectl` e um cluster local (Kind, Minikube ou Docker Desktop).

---

## Execução com Docker Compose

```bash
git clone https://github.com/prd15/clinica-medica.git
cd clinica-medica
cp .env.example .env
# editar .env com DB_USER, DB_PASS e KEYCLOAK_SERVICE_SECRET
mvn package -DskipTests       # gera os JARs que as imagens copiam
docker compose up -d --build
```

> As imagens usam um **Dockerfile único na raiz** (runtime-only): elas apenas copiam o JAR já compilado, sem rodar Maven dentro do build. Por isso o `mvn package -DskipTests` precisa rodar antes do `docker compose up --build`. Resultado: build de imagem muito mais rápido e containers mais leves (`MaxRAMPercentage=75`, shutdown limpo via PID 1).

A stack sobe **8 containers**: 3 bancos MySQL, Keycloak, o gateway e os 3 microsserviços.

| Serviço | Porta local | Observação |
|---|---:|---|
| gateway | 8080 | Ponto de entrada único |
| administrativo | 8081 | |
| agendamento | 8082 | |
| atendimento | 8083 | |
| keycloak | 8180 | Realm `clinica` autoimportado |
| db-administrativo | 3307 → 3306 | clinica_administrativo |
| db-agendamento | 3308 → 3306 | clinica_agendamento |
| db-atendimento | 3309 → 3306 | clinica_atendimento |

Verificação rápida de saúde:

```bash
docker compose ps
for p in 8080 8081 8082 8083; do curl -s http://localhost:$p/actuator/health; echo; done
```

Encerrar a stack (com remoção de volumes):

```bash
docker compose down        # mantém dados
docker compose down -v     # remove volumes dos bancos
```

---

## Execução local (sem Docker)

Suba o Keycloak e os bancos via Compose e execute os serviços Java pela IDE ou linha de comando. Configure o SDK do projeto para Java 17.

```bash
# infraestrutura
docker compose up -d db-administrativo db-agendamento db-atendimento keycloak

# build e execução
mvn clean install
mvn -pl administrativo spring-boot:run
mvn -pl agendamento  spring-boot:run
mvn -pl atendimento  spring-boot:run
mvn -pl gateway      spring-boot:run
```

Variáveis mínimas por serviço (host local): `DB_HOST=localhost`, `DB_USER`, `DB_PASS` e a `DB_PORT` correspondente (3307 / 3308 / 3309). O `agendamento` usa `ADMINISTRATIVO_URL`; o `atendimento` usa `AGENDAMENTO_URL`.

---

## Documentação da API (Swagger)

Swagger UI ativo nos três microsserviços:

| Serviço | URL |
|---|---|
| administrativo | <http://localhost:8081/swagger-ui/index.html> |
| agendamento | <http://localhost:8082/swagger-ui/index.html> |
| atendimento | <http://localhost:8083/swagger-ui/index.html> |

OpenAPI bruto disponível em `/v3/api-docs` de cada serviço.

---

## Testes e qualidade

### Testes unitários (JUnit 5 + Mockito)

```bash
mvn clean test
```

Cobre os services de todos os módulos. Os relatórios Surefire ficam em `*/target/surefire-reports/`.

### Testes de API (Postman + Newman)

As collections em `docs/` são executadas contra o gateway com autenticação real, usando o environment `docs/keycloak.postman_environment.json` (já busca o token JWT no pre-request). Comece pela collection de autenticação/RBAC:

```bash
ENV=docs/keycloak.postman_environment.json
npx newman run docs/gateway-auth-collection.json -e $ENV
npx newman run docs/convenio-collection.json     -e $ENV
npx newman run docs/especialidade-collection.json -e $ENV
npx newman run docs/medico-collection.json       -e $ENV
npx newman run docs/paciente-collection.json     -e $ENV
npx newman run docs/atendente-collection.json    -e $ENV
npx newman run docs/consulta-collection.json     -e $ENV
npx newman run docs/atendimento-collection.json  -e $ENV
npx newman run docs/atendimento-notificacao-collection.json -e $ENV
npx newman run docs/relatorios-collection.json   -e $ENV
```

> O environment `docs/local.postman_environment.json` aponta para as portas diretas sem token e resultará em `401` — use-o apenas em cenários sem segurança.

---

## Observabilidade e logs

Logging com **SLF4J + Logback** e rastreabilidade por **correlation-id** de ponta a ponta entre os serviços.

**Fluxo do correlation-id:**

1. O gateway gera um `X-Correlation-Id` na borda (ou reaproveita o enviado pelo cliente) e o injeta no request encaminhado aos microsserviços.
2. Cada microsserviço lê o header, coloca o id no MDC e o devolve no header da resposta.
3. As chamadas internas via OpenFeign propagam o mesmo id, de modo que toda a request — gateway → administrativo → agendamento, por exemplo — compartilha um único identificador nos logs.

**Formato da linha de log** (`[serviço] [correlation-id]`):

```
2026-06-21 14:30:01.123 INFO  [administrativo] [a1b2c3d4-...] b.e.i.c.logging.CorrelationIdFilter - GET /v1/convenios -> 200 (45ms)
```

O padrão usa `%clr` do Spring Boot: ANSI colorido em terminal, texto puro em container (stdout — coletável por Docker/k8s). Cada request HTTP de negócio gera uma linha de acesso (`método rota -> status (tempoms)`); o `/actuator/health` é omitido para não poluir.

**Configuração:**

- O nível de log da aplicação é ajustável por ambiente: `LOG_LEVEL_APP=DEBUG` (padrão `INFO`).
- A configuração compartilhada está em `commons/src/main/resources/logback-base.xml`, incluída por cada microsserviço; o gateway (WebFlux) tem o seu próprio `logback-spring.xml`.

**Rastrear uma request nos logs:**

```bash
# o uuid vem no header X-Correlation-Id da resposta
docker compose logs | grep "<correlation-id>"
```

---

## CI/CD (GitHub Actions)

Dois workflows em `.github/workflows/`:

| Workflow | Gatilho | O que faz |
|----------|---------|-----------|
| `ci.yml` (job `build-test`) | push e pull request em `main`/`development` | `mvn clean verify` na raiz + publicação dos resultados Surefire como check |
| `ci.yml` (job `smoke`) | após `build-test` | Sobe a stack completa via Docker Compose (valida os 4 Dockerfiles), aguarda Keycloak e os health checks, obtém JWT real e testa auth/RBAC via gateway (401 sem token, 200 com ADMIN, 403 com role insuficiente) |
| `docker-publish.yml` | push em `main`/`development` | Build das 4 imagens Docker (matrix) e push para o GHCR com cache de camadas |

Imagens publicadas (tags: nome da branch, `sha-<short>` e `latest` apenas na `main`):

- `ghcr.io/prd15/clinica-medica-administrativo`
- `ghcr.io/prd15/clinica-medica-agendamento`
- `ghcr.io/prd15/clinica-medica-atendimento`
- `ghcr.io/prd15/clinica-medica-gateway`

A autenticação no GHCR usa o `GITHUB_TOKEN` nativo (`packages: write`) — nenhum secret adicional é necessário. Para baixar as imagens localmente, faça `docker login ghcr.io` com um PAT com escopo `read:packages`.

---

## Implantação em Kubernetes

Manifests em `k8s/` (namespace `clinica`). Crie o Secret real a partir do exemplo antes de aplicar:

```bash
cp k8s/secrets.example.yaml k8s/secrets.yaml
# editar k8s/secrets.yaml com db-username e db-password em base64
```

Build das imagens locais e, em Kind, carga no cluster:

```bash
mvn package -DskipTests       # JARs que as imagens copiam
docker compose build
kind load docker-image clinica/administrativo:latest
kind load docker-image clinica/agendamento:latest
kind load docker-image clinica/atendimento:latest
kind load docker-image clinica/gateway:latest
```

Aplicação dos recursos:

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/secrets.yaml
kubectl apply -f k8s/databases/
kubectl apply -f k8s/keycloak/
kubectl apply -f k8s/administrativo/
kubectl apply -f k8s/agendamento/
kubectl apply -f k8s/atendimento/
kubectl apply -f k8s/gateway/
kubectl apply -f k8s/ingress.yaml
kubectl get pods -n clinica
```

Acesso via port-forward (sem Ingress):

```bash
kubectl port-forward -n clinica svc/administrativo 8081:8081
kubectl port-forward -n clinica svc/agendamento  8082:8082
kubectl port-forward -n clinica svc/atendimento  8083:8083
```

Para Ingress local, instale o NGINX Ingress Controller e aponte os hosts no arquivo `hosts`
(`C:\Windows\System32\drivers\etc\hosts` no Windows, `/etc/hosts` em Linux/macOS).

---

## Variáveis de ambiente

| Variável | Descrição | Padrão |
|---|---|---|
| `DB_HOST` | Host do MySQL | localhost |
| `DB_PORT` | Porta do MySQL | 3307 / 3308 / 3309 (por serviço) |
| `DB_USER` | Usuário do MySQL | root |
| `DB_PASS` | Senha do MySQL | (vazio) |
| `SPRING_JPA_SHOW_SQL` | Exibe SQL no log | false |
| `LOG_LEVEL_APP` | Nível de log do pacote `br.edu.imepac` | INFO |
| `ADMINISTRATIVO_URL` | URL do administrativo (usada pelo agendamento) | http://localhost:8081 |
| `AGENDAMENTO_URL` | URL do agendamento (usada pelo atendimento) | http://localhost:8082 |
| `KEYCLOAK_SERVICE_SECRET` | Secret do client `clinica-service` | (definir no `.env`) |
| `OUTBOX_POLL_INTERVAL_MS` | Intervalo do scheduler de Outbox | 10000 |
| `OUTBOX_MAX_RETRY` | Tentativas máximas de reprocessamento | 3 |
| `MYSQL_DATABASE` | Banco criado pelo container MySQL | por serviço (Compose) |
| `MYSQL_ROOT_PASSWORD` | Senha root do MySQL nos containers | valor de `DB_PASS` |

No Docker Compose, os serviços usam a porta interna `3306`; `3307/3308/3309` são mapeamentos para acesso a partir da máquina host.

---

## Estrutura do repositório

```text
clinica-medica/
├── gateway/                # API Gateway (Spring Cloud Gateway WebFlux + OAuth2)
│   └── src/main/java/br/edu/imepac/gateway/
│       ├── security/       # Validação de JWT e conversão de papéis
│       └── filter/         # Filtros customizados (ex.: InvalidPathFilter)
├── administrativo/         # Microsserviço :8081
│   └── src/main/java/br/edu/imepac/administrativo/
│       ├── entities/  repositories/  services/  controllers/  dtos/
│       ├── config/         # Security, Swagger, Feign
│       └── integration/    # Clients Feign + token de serviço
├── agendamento/            # Microsserviço :8082 (estrutura análoga)
├── atendimento/            # Microsserviço :8083 (+ Outbox Pattern)
├── commons/                # Biblioteca compartilhada
│   └── src/main/java/br/edu/imepac/commons/
│       ├── entities/       # BaseEntity (auditoria JPA)
│       ├── exceptions/     # BusinessException, EntityNotFound, ... + handler global
│       ├── dtos/           # ErrorResponse
│       └── config/         # ModelMapperConfig, CommonsAutoConfiguration
├── keycloak/realm-clinica.json   # Realm autoimportado
├── docs/                   # Collections Postman, environments e revisões
├── k8s/                    # Manifests Kubernetes
├── docker-compose.yml      # Stack local completa (8 containers)
├── .env.example            # Modelo de variáveis de ambiente
└── pom.xml                 # Maven multi-módulo
```

> A biblioteca `commons` **não** contém entidades de domínio, repositories ou services de negócio — esses são autocontidos em cada microsserviço.

---

## Decisões arquiteturais

- **API Gateway como ponto único de entrada** — roteamento e validação de JWT centralizados; serviços não são expostos diretamente em produção.
- **Autenticação delegada (Keycloak)** — OAuth2/OIDC padrão de mercado, com autorização por papel e tokens de serviço (`client_credentials`) para comunicação interna.
- **Database per service** — isolamento total de schema; referências entre contextos por `Long id`, sem `@ManyToOne` entre bancos.
- **Comunicação síncrona com OpenFeign + OkHttp** — clients tipados com timeouts explícitos (3s conexão / 5s leitura).
- **Outbox Pattern** — desacopla a transação de negócio da notificação remota, garantindo entrega resiliente mesmo com o destino indisponível.
- **Tratamento de erros padronizado** — `GlobalExceptionHandler` e `ErrorResponse` compartilhados via `commons`, com respostas consistentes e sem vazamento de stack trace.
- **Build multi-módulo e imagens multi-stage** — build coordenado pelo POM raiz; runtime enxuto sobre Eclipse Temurin JRE 17.
- **Cloud-agnóstico** — manifests Kubernetes usam apenas recursos padrão (Deployment, Service, ConfigMap, Secret, Ingress).

---

## Fluxo de negócio

1. Cadastro de convênio, especialidade, médico (com especialidade) e paciente no **administrativo**.
2. Agendamento de consulta no **agendamento**, informando paciente, médico, convênio e data/hora.
3. O **agendamento** valida via Feign, no **administrativo**, se paciente, médico e convênio existem e estão aptos.
4. O médico consulta sua agenda.
5. No horário, o médico inicia o **atendimento**, gerando prontuário, anotações e solicitações de exame.
6. O **atendimento** notifica o **agendamento**, via Outbox, para marcar a consulta como realizada.
7. Relatórios consolidam os dados (pacientes por convênio, consultas diárias).

---

## Convenções de contribuição

- Trabalhe a partir da branch `development`; `main` é a branch estável.
- Padrão de commits: `tipo(escopo): descrição` (ex.: `feat(medico): adiciona inativação`).
- Todo novo `Service` deve vir acompanhado de testes com Mockito.
- Rotas sempre versionadas com prefixo `/v1/`; `@Valid` obrigatório nos `@RequestBody`.
- Nunca versione segredos: use `${VAR:}` no `application.properties` e mantenha `k8s/secrets.yaml` fora do controle de versão.
