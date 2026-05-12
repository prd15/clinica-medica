# CLAUDE.md — Clinica Medica (Microsservicos)

## Sobre este projeto
Sistema de gestao de clinica medica em arquitetura de microsservicos.
Stack: Java 17, Spring Boot 3.3.5, Spring Web, Spring Data JPA, MySQL, Lombok 1.18.36, ModelMapper 3.2.1, Maven.
Swagger: springdoc-openapi-starter-webmvc-ui 2.6.0 (administrativo)

## Estrutura do repositorio
- `commons/` — biblioteca compartilhada (NAO e microsservico). Contem entities, repositories, services.
- `administrativo/` — microsservico porta 8081, banco clinica_administrativo
- `agendamento/` — microsservico porta 8082, banco clinica_agendamento
- `atendimento/` — microsservico porta 8083, banco clinica_atendimento

## REGRA DE OURO — Padrao de implementacao
O modulo de Convenio e o padrao de referencia. SEMPRE siga esta estrutura:

1. Entity → em `commons/entities/`
2. Repository → em `commons/repositories/`
3. Service → em `commons/services/`
4. Testes unitarios do Service → em `commons/test/`
5. Request DTO (com @NotBlank) → no microsservico em `dtos/`
6. Response DTO → no microsservico em `dtos/`
7. Controller → no microsservico em `controllers/`

## Nomenclatura obrigatoria
- Entidades: `NomeEntity`
- Repositories: `NomeRepository` extends JpaRepository
- Services: `NomeService`
- DTOs: `NomeRequest` e `NomeResponse`
- Controllers: `NomeController`
- Rotas: sempre com prefixo `/v1/`

## Regras de codigo
- NUNCA usar @ManyToOne entre entidades de bancos diferentes — usar Long id como referencia
- NUNCA commitar senha no application.properties — usar ${DB_PASS:}
- SEMPRE versionar rotas com /v1/
- SEMPRE incluir @Valid nos @RequestBody dos controllers
- SEMPRE escrever testes com Mockito para todo Service novo
- Comunicacao entre microsservicos: apenas via HTTP REST (RestTemplate)

## Microsservicos — Portas e bancos
| Servico         | Porta | Banco                   |
|-----------------|-------|-------------------------|
| administrativo  | 8081  | clinica_administrativo  |
| agendamento     | 8082  | clinica_agendamento     |
| atendimento     | 8083  | clinica_atendimento     |

## Atores
- Atendente: acesso ao administrativo + agendamento
- Medico: visualiza agenda + realiza atendimento

## Estado atual do projeto
Implementado:
- Convenio: ConvenioEntity, ConvenioRepository, ConvenioService (com testes), ConvenioController, ConvenioRequest, ConvenioResponse.
- Paciente: PacienteEntity, PacienteRepository, PacienteService (com testes), PacienteController, PacienteRequest, PacienteResponse.
Proximo: MedicoEntity → MedicoRepository → MedicoService → MedicoController.

## Swagger UI
- Dependencia: springdoc-openapi-starter-webmvc-ui 2.6.0
- Versao centralizada no pom.xml raiz (dependencyManagement)
- Adicionada apenas no administrativo (unico microsservico com controllers)
- URL: http://localhost:8081/swagger-ui/index.html
- API Docs: http://localhost:8081/v3/api-docs

## Configuracao do Maven
- Lombok 1.18.36 configurado via annotationProcessorPaths no maven-compiler-plugin (commons e administrativo)
- Para compilar via CLI usar JDK 17: JAVA_HOME=$(/usr/libexec/java_home -v 17) mvn compile
- JDK 25 instalado no sistema NAO e compativel com Lombok — sempre usar JDK 17

## Regras de commit
- NUNCA adicionar Co-authored-by nas mensagens de commit
- NUNCA mencionar Claude, IA ou ferramentas de assistencia nos commits
- O autor do commit e SOMENTE o dono do repositorio
- Formato: tipo(escopo): descricao (ex: feat(paciente): adiciona CRUD completo)

## Agentes disponiveis neste projeto
- @agent-backend-dev → implementa codigo Java seguindo o padrao do convenio
- @agent-doc-writer → documenta no Obsidian apos cada implementacao
- @agent-code-reviewer → revisa antes de finalizar

## Documentacao
- Changelog de agentes: `docs/CHANGELOG_AGENTES.md`
- Vault Obsidian: `/Users/luscas/IdeaProjects/clinica-medica-docs/`
- Apos cada implementacao, atualizar o Obsidian (rotas-api, entidades, diario)

## Repositorio
- Remote: https://github.com/prd15/clinica-medica.git
- Conta do usuario: https://github.com/Lucks026
- Branch principal: main
- Ultimo push: 3 commits (setup + swagger + comentarios)

## O que falta implementar
1. Medico (Entity, Repository, Service, Testes, Controller, DTOs) — administrativo
2. Especialidade — administrativo
3. Atendente — administrativo
4. Consulta — agendamento
5. Atendimento — atendimento
6. Comunicacao HTTP entre microsservicos (RestTemplate)
