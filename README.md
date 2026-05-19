# Clínica Médica

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)

Sistema acadêmico de gestão clínica desenvolvido com Java 17, Spring Boot 3 e arquitetura de microsserviços. O projeto organiza as rotinas administrativas, o agendamento de consultas e o atendimento clínico em módulos independentes, cada um com seu próprio banco de dados MySQL.

## Visão Geral

A aplicação foi desenhada para separar responsabilidades de negócio em três serviços principais:

- **Administrativo**: cadastro e manutenção de pacientes, médicos, especialidades, convênios, atendentes e relatórios.
- **Agendamento**: criação, consulta, confirmação, reagendamento e cancelamento de consultas.
- **Atendimento**: registro clínico da consulta, prontuário, anotações, exames e histórico do paciente.

O módulo **commons** centraliza entidades, repositories, services compartilhados, configurações comuns e testes de regra de negócio.

## Arquitetura

```mermaid
flowchart LR
    Cliente["Cliente / Postman / Swagger UI"]
    ADM["administrativo :8081"]
    AGE["agendamento :8082"]
    ATE["atendimento :8083"]
    CADM[("clinica_administrativo")]
    CAGE[("clinica_agendamento")]
    CATE[("clinica_atendimento")]

    Cliente --> ADM
    Cliente --> AGE
    Cliente --> ATE
    AGE --> ADM
    ATE --> AGE
    ADM --> CADM
    AGE --> CAGE
    ATE --> CATE
```

## Stack Técnica

| Camada | Tecnologias |
|---|---|
| Linguagem | Java 17 |
| Framework | Spring Boot 3.3.5 |
| API | Spring Web, Bean Validation, SpringDoc OpenAPI |
| Persistência | Spring Data JPA, Hibernate, MySQL 8 |
| Integração interna | RestTemplate com Apache HttpClient 5 |
| Mapeamento | ModelMapper |
| Build | Maven multi-módulo |
| Infraestrutura local | Docker Compose |
| Produtividade | Lombok, spring-dotenv |
| Testes | Spring Boot Starter Test, JUnit |

## Estrutura do Repositório

```text
clinica-medica/
├── administrativo/      # Microsserviço de cadastros, usuários administrativos e relatórios
├── agendamento/         # Microsserviço responsável pelo ciclo de vida das consultas
├── atendimento/         # Microsserviço de prontuário, anotações, exames e histórico
├── commons/             # Entidades, repositories, services e configurações compartilhadas
├── docs/                # Collections Postman, changelog técnico e material de revisão
├── docker-compose.yml   # Orquestração local dos serviços e bancos MySQL
└── pom.xml              # Projeto Maven agregador
```

## Módulos

| Módulo | Tipo | Porta | Banco | Responsabilidade |
|---|---:|---:|---|---|
| `commons` | Biblioteca interna | - | - | Regras compartilhadas, entidades, repositories e services |
| `administrativo` | Microsserviço | `8081` | `clinica_administrativo` | Cadastros, atendentes, convênios, médicos, pacientes e relatórios |
| `agendamento` | Microsserviço | `8082` | `clinica_agendamento` | Agenda médica, status da consulta e validações com administrativo |
| `atendimento` | Microsserviço | `8083` | `clinica_atendimento` | Atendimento clínico, prontuário, anotações, exames e histórico |

## Regras de Separação

Cada microsserviço possui seu próprio banco de dados e não compartilha relacionamentos JPA diretos com tabelas de outro módulo. Quando um serviço precisa referenciar dados externos, ele armazena apenas o identificador (`Long id`) e consulta o serviço dono da informação por HTTP.

Esse desenho mantém o isolamento entre contextos e evita acoplamento por `@ManyToOne` entre bancos diferentes.

## Comunicação Entre Serviços

| Origem | Destino | Uso |
|---|---|---|
| `agendamento` | `administrativo` | Valida se convênio, médico e paciente existem e estão aptos antes de agendar |
| `atendimento` | `agendamento` | Notifica a realização da consulta depois do registro clínico |

No Docker, os serviços se comunicam pelos nomes dos containers (`http://administrativo:8081` e `http://agendamento:8082`). Em execução local, os fallbacks usam `localhost`.

## Pré-requisitos

Para executar o projeto com conforto, tenha instalado:

- JDK 17.
- Maven 3.9 ou superior.
- Docker e Docker Compose.
- Um cliente HTTP, como Postman, Insomnia ou o próprio Swagger UI.

## Configuração de Ambiente

Crie um arquivo `.env` na raiz do projeto a partir do exemplo:

```env
DB_USER=root
DB_PASS=sua_senha
```

O arquivo `.env.example` já existe no repositório e serve como referência mínima. As aplicações também possuem valores padrão para desenvolvimento local, mas o Docker Compose espera essas variáveis para subir os bancos e serviços.
