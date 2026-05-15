# Clínica Médica

Sistema de gestão de clínica médica em arquitetura de microsserviços. Projeto acadêmico desenvolvido com Java 17 e Spring Boot 3.

## Tecnologias

- Java 17 / Spring Boot 3.3.5
- Spring Web, Spring Data JPA, Spring Validation
- MySQL 8 · Lombok · Maven multi-módulo
- Springdoc OpenAPI · Docker Compose

## Módulos

| Módulo | Tipo | Porta | Banco |
|---|---|---|---|
| `commons` | Biblioteca interna | — | — |
| `administrativo` | Microsserviço | 8081 | clinica_administrativo |
| `agendamento` | Microsserviço | 8082 | clinica_agendamento |
| `atendimento` | Microsserviço | 8083 | clinica_atendimento |

`commons` contém as entities, repositories e services compartilhados. Referências entre serviços de bancos diferentes usam `Long id` — sem `@ManyToOne` entre módulos.

## Endpoints

### Administrativo · `localhost:8081`

| Método | Rota |
|---|---|
| GET / POST | `/v1/convenios` |
| GET / PUT / DELETE | `/v1/convenios/{id}` |
| GET / POST | `/v1/pacientes` |
| GET / PUT / DELETE | `/v1/pacientes/{id}` |
| GET / POST | `/v1/medicos` |
| GET / POST | `/v1/especialidades` |
| GET / POST | `/v1/atendentes` |

### Agendamento · `localhost:8082`

| Método | Rota |
|---|---|
| GET / POST | `/v1/consultas` |
| GET | `/v1/consultas/{id}` |
| PATCH | `/v1/consultas/{id}/realizar` |

### Atendimento · `localhost:8083`

| Método | Rota |
|---|---|
| POST | `/v1/atendimentos` |
| GET | `/v1/atendimentos/{consultaId}` |
| GET | `/v1/atendimentos/historico?pacienteId=` |
| POST | `/v1/atendimentos/{id}/anotacoes` |
| POST | `/v1/atendimentos/{id}/exames` |

Documentação interativa disponível em `/swagger-ui.html` em cada serviço.

## Variáveis de ambiente

```env
DB_USER=root
DB_PASS=
```
