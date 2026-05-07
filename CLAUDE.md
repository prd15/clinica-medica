# CLAUDE.md — Clinica Medica (Microsservicos)

## Sobre este projeto
Sistema de gestao de clinica medica em arquitetura de microsservicos.
Stack: Java 17, Spring Boot 3.3.5, Spring Web, Spring Data JPA, MySQL, Lombok, ModelMapper, Maven.

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

## Regras de commit
- NUNCA adicionar Co-authored-by nas mensagens de commit
- NUNCA mencionar Claude, IA ou ferramentas de assistencia nos commits
- O autor do commit e SOMENTE o dono do repositorio
- Formato: tipo(escopo): descricao (ex: feat(paciente): adiciona CRUD completo)

## Agentes disponiveis neste projeto
- @agent-backend-dev → implementa codigo Java seguindo o padrao do convenio
- @agent-doc-writer → documenta no Obsidian apos cada implementacao
- @agent-code-reviewer → revisa antes de finalizar
