# 🏥 Clínica Médica — Sistema de Gestão em Microsserviços

## 📌 Sobre o projeto

O projeto Clínica Médica é um sistema acadêmico de gestão clínica desenvolvido em arquitetura de microsserviços. A aplicação separa as responsabilidades de cadastro administrativo, agendamento de consultas e atendimento clínico em serviços independentes, cada um com sua própria porta, seu próprio banco MySQL e seu próprio contexto de negócio.

Os principais atores do sistema são o atendente e o médico. O atendente utiliza os recursos administrativos para cadastrar convênios, pacientes, médicos, especialidades e atendentes, além de operar o fluxo de agendamento. O médico consulta sua agenda e registra o atendimento clínico, incluindo prontuário, anotações e solicitações de exames.

A solução é composta pelos módulos `administrativo`, `agendamento`, `atendimento` e `commons`. Os três primeiros são microsserviços Spring Boot executáveis; o `commons` é uma biblioteca compartilhada com entidades, repositories, services e regras comuns usadas pelos serviços.

## 🧭 Arquitetura

```text
                           HTTP REST
                 +---------------------------+
                 |                           v
+------------------------+        +------------------------+        +------------------------+
| administrativo :8081   |<-------| agendamento :8082     |<-------| atendimento :8083      |
| Cadastros e relatórios |        | Consultas e agenda    |        | Prontuário e exames   |
+-----------+------------+        +-----------+------------+        +-----------+------------+
            |                                 |                                 |
            | JDBC                            | JDBC                            | JDBC
            v                                 v                                 v
+------------------------+        +------------------------+        +------------------------+
| clinica_administrativo |        | clinica_agendamento    |        | clinica_atendimento    |
| MySQL :3306            |        | MySQL :3306            |        | MySQL :3306            |
+------------------------+        +------------------------+        +------------------------+

Fluxos HTTP:
- agendamento -> administrativo: valida paciente, médico e convênio.
- atendimento -> agendamento: consulta e atualiza status da consulta atendida.
```

## 🛠️ Stack

| Tecnologia | Versão | Papel |
|---|---:|---|
| Java | 17 | Linguagem principal dos microsserviços |
| Spring Boot | 3.3.5 | Framework web e base das APIs REST |
| Spring Web | Gerenciada pelo Spring Boot | Criação dos controllers REST |
| Spring Data JPA | Gerenciada pelo Spring Boot | Persistência com repositories |
| Hibernate | Gerenciada pelo Spring Boot | Implementação JPA |
| MySQL | 8 | Banco de dados de cada microsserviço |
| Maven | 3.9+ | Build multi-módulo e gerenciamento de dependências |
| Lombok | 1.18.36 | Redução de boilerplate em entidades e DTOs |
| ModelMapper | 3.2.1 | Conversão entre entidades e DTOs |
| SpringDoc OpenAPI | 2.6.0 | Swagger UI e documentação OpenAPI |
| Docker | Atual | Build das imagens dos microsserviços |
| Docker Compose | Atual | Orquestração local dos serviços e bancos |
| Kubernetes | networking.k8s.io/v1 | Deploy e exposição dos microsserviços em cluster |

## ✅ Pré-requisitos

- Java 17 instalado e configurado.
- Maven 3.9 ou superior.
- Docker instalado.
- Docker Compose instalado.
- Opcional para Kubernetes: `kubectl` e um cluster local, como Minikube, Kind ou Docker Desktop Kubernetes.
- Uma IDE Java, como IntelliJ IDEA, para execução individual dos módulos.

## 🐳 Como rodar com Docker Compose

Clone o repositório, crie o arquivo de ambiente e suba a stack:

```bash
git clone https://github.com/prd15/clinica-medica.git
cd clinica-medica
cp .env.example .env
# editar .env com a senha do banco
docker-compose up
```

Exemplo mínimo de `.env`:

```env
DB_USER=root
DB_PASS=suasenha
```

O Docker Compose sobe três bancos MySQL e três microsserviços:

| Serviço | Porta local | Banco |
|---|---:|---|
| administrativo | 8081 | clinica_administrativo |
| agendamento | 8082 | clinica_agendamento |
| atendimento | 8083 | clinica_atendimento |
| db-administrativo | 3307 -> 3306 | clinica_administrativo |
| db-agendamento | 3308 -> 3306 | clinica_agendamento |
| db-atendimento | 3309 -> 3306 | clinica_atendimento |

Para parar os containers:

```bash
docker-compose down
```

Para parar e remover volumes dos bancos:

```bash
docker-compose down -v
```

## 💻 Como rodar individualmente (sem Docker)

No IntelliJ IDEA, abra a pasta raiz `clinica-medica` como projeto Maven. Garanta que o SDK do projeto esteja configurado para Java 17.

Crie três bancos MySQL locais:

```sql
CREATE DATABASE clinica_administrativo;
CREATE DATABASE clinica_agendamento;
CREATE DATABASE clinica_atendimento;
```

Configure as variáveis de ambiente de cada Run Configuration:

Administrativo:

```env
DB_HOST=localhost
DB_PORT=3307
DB_USER=root
DB_PASS=suasenha
```

Agendamento:

```env
DB_HOST=localhost
DB_PORT=3308
DB_USER=root
DB_PASS=suasenha
ADMINISTRATIVO_URL=http://localhost:8081
```

Atendimento:

```env
DB_HOST=localhost
DB_PORT=3309
DB_USER=root
DB_PASS=suasenha
AGENDAMENTO_URL=http://localhost:8082
```

Execute as classes principais nesta ordem:

```text
administrativo/src/main/java/br/edu/imepac/administrativo/AdministrativoApplication.java
agendamento/src/main/java/br/edu/imepac/agendamento/AgendamentoApplication.java
atendimento/src/main/java/br/edu/imepac/atendimento/AtendimentoApplication.java
```

Também é possível executar pelo terminal, caso o Maven esteja instalado:

```bash
mvn clean install
mvn -pl administrativo spring-boot:run
mvn -pl agendamento spring-boot:run
mvn -pl atendimento spring-boot:run
```

## 📚 Swagger UI

| Serviço | URL |
|---------|-----|
| Administrativo | http://localhost:8081/swagger-ui.html |
| Agendamento | http://localhost:8082/swagger-ui.html |
| Atendimento | http://localhost:8083/swagger-ui.html |

Também podem ser usados os caminhos canônicos:

| Serviço | URL |
|---|---|
| Administrativo | http://localhost:8081/swagger-ui/index.html |
| Agendamento | http://localhost:8082/swagger-ui/index.html |
| Atendimento | http://localhost:8083/swagger-ui/index.html |

## 🗂️ Estrutura do repositório

```text
clinica-medica/
├── administrativo/
│   ├── Dockerfile
│   └── src/main/java/br/edu/imepac/administrativo/
│       ├── controllers/    # Endpoints de cadastros e relatórios
│       ├── dtos/           # DTOs de entrada e saída do administrativo
│       └── config/         # Configurações do módulo, incluindo Swagger
├── agendamento/
│   ├── Dockerfile
│   └── src/main/java/br/edu/imepac/agendamento/
│       ├── clients/        # Cliente HTTP para o administrativo
│       ├── controllers/    # Endpoints de consultas
│       ├── dtos/           # DTOs de agendamento
│       └── config/         # Swagger e RestTemplate
├── atendimento/
│   ├── Dockerfile
│   └── src/main/java/br/edu/imepac/atendimento/
│       ├── clients/        # Cliente HTTP para o agendamento
│       ├── controllers/    # Endpoints de atendimento clínico
│       ├── dtos/           # DTOs de prontuário, anotações e exames
│       └── config/         # Swagger e configurações do módulo
├── commons/
│   └── src/main/java/br/edu/imepac/commons/
│       ├── entities/       # Entidades compartilhadas
│       ├── repositories/   # Repositories JPA
│       └── services/       # Regras de negócio compartilhadas
├── docs/                   # Collections, revisões e documentação auxiliar
├── k8s/                    # Manifests Kubernetes
├── docker-compose.yml      # Stack local com serviços e bancos
├── .env.example            # Exemplo de variáveis de ambiente
├── CLAUDE.md               # Guia operacional do projeto
└── pom.xml                 # Maven multi-módulo
```

## 🔐 Variáveis de ambiente

| Variável | Descrição | Valor padrão |
|---|---|---|
| DB_HOST | Host do banco MySQL usado pelo microsserviço | localhost |
| DB_PORT | Porta do banco MySQL | administrativo: 3307, agendamento: 3308, atendimento: 3309 |
| DB_USER | Usuário do banco MySQL | root |
| DB_PASS | Senha do banco MySQL | vazio |
| SPRING_JPA_SHOW_SQL | Habilita exibição de SQL no log | false |
| ADMINISTRATIVO_URL | URL usada pelo agendamento para chamar o administrativo | http://localhost:8081 |
| AGENDAMENTO_URL | URL usada pelo atendimento para chamar o agendamento | http://localhost:8082 |
| MYSQL_DATABASE | Nome do banco criado pelo container MySQL | definido por serviço no Docker Compose |
| MYSQL_ROOT_PASSWORD | Senha root do MySQL nos containers | valor de DB_PASS |

No Docker Compose, os serviços usam `DB_PORT=3306` internamente, porque essa é a porta do MySQL dentro da rede Docker. As portas `3307`, `3308` e `3309` são apenas mapeamentos para acesso local pela máquina host.

## ☸️ Como rodar com Kubernetes

Antes de aplicar os manifests, crie o Secret real a partir do exemplo:

```bash
cp k8s/secrets.example.yaml k8s/secrets.yaml
# editar k8s/secrets.yaml com db-username e db-password em base64
```

Aplique os recursos no cluster:

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/secrets.yaml
kubectl apply -f k8s/administrativo/
kubectl apply -f k8s/agendamento/
kubectl apply -f k8s/atendimento/
kubectl apply -f k8s/ingress.yaml
kubectl get pods -n clinica
```

Para acompanhar os Services:

```bash
kubectl get svc -n clinica
```

Para testar sem Ingress, use port-forward:

```bash
kubectl port-forward -n clinica svc/administrativo 8081:8081
kubectl port-forward -n clinica svc/agendamento 8082:8082
kubectl port-forward -n clinica svc/atendimento 8083:8083
```

Para testar com Ingress local, instale um Ingress Controller NGINX e adicione os hosts locais apontando para o IP do cluster:

```text
127.0.0.1 administrativo.clinica.local
127.0.0.1 agendamento.clinica.local
127.0.0.1 atendimento.clinica.local
```

No Windows, o arquivo fica em:

```text
C:\Windows\System32\drivers\etc\hosts
```

No Linux/macOS, o arquivo fica em:

```text
/etc/hosts
```

## 🔄 Fluxo de uso da aplicação

1. O atendente cadastra um convênio no serviço administrativo.
2. O atendente cadastra uma especialidade médica.
3. O atendente cadastra um médico e vincula esse médico a uma especialidade.
4. O atendente cadastra um paciente e informa o convênio associado.
5. O atendente agenda uma consulta no serviço de agendamento, informando paciente, médico, convênio e data/hora.
6. O serviço de agendamento valida via HTTP se paciente, médico e convênio existem e estão aptos no administrativo.
7. O médico consulta sua agenda no serviço de agendamento.
8. No momento da consulta, o médico registra o atendimento no serviço de atendimento.
9. O atendimento gera prontuário, permite anotações clínicas e solicitações de exames.
10. O serviço de atendimento notifica o agendamento para marcar a consulta como realizada.

## 🧱 Decisões arquiteturais

- Cloud agnóstico: os manifests Kubernetes usam recursos padrão, como Deployment, Service, ConfigMap, Secret e Ingress, evitando dependência direta de um provedor específico.
- Banco por serviço: cada microsserviço possui seu próprio banco MySQL, preservando isolamento de dados e autonomia de contexto.
- IDs em vez de FK entre serviços: referências entre contextos usam campos `Long id`, sem `@ManyToOne` entre entidades de bancos diferentes.
- Comunicação HTTP: integrações entre microsserviços são feitas por REST com `RestTemplate`, mantendo os bancos isolados.
- Maven multi-módulo: a raiz agrega `commons`, `administrativo`, `agendamento` e `atendimento`, garantindo build coordenado.
- Dockerfiles multi-stage: cada serviço usa Maven com JDK 17 para build e Eclipse Temurin JRE 17 para runtime.
- Secrets no Kubernetes: credenciais ficam separadas de ConfigMaps e o arquivo real `k8s/secrets.yaml` não deve ser versionado.

## 👥 Equipe

| Membro | Responsabilidade |
|---|---|
| Pessoa 1 | Módulos iniciais e estrutura base do projeto |
| Pessoa 2 | Cadastros administrativos e regras compartilhadas |
| Pessoa 3 | Fluxo de agendamento de consultas |
| Pessoa 4 | Fluxo de atendimento clínico |
| Pessoa 5 | Integração, testes e ajustes de revisão |
| Pessoa 6 | Manifests Kubernetes, revisão Swagger e README final |
