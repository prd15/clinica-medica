# Changelog de Implementacoes — Agentes

Este arquivo e atualizado automaticamente pelo agente `backend-dev` apos cada implementacao.
O agente `doc-writer` le este arquivo para gerar documentacao no Obsidian.

---

### 2026-05-15 — Consulta (Agendamento)
**Agente:** backend-dev (Pessoa 4 — roadmap pessoal)
**Arquivos criados:**
- commons/src/main/java/br/edu/imepac/commons/entities/StatusConsulta.java
- commons/src/main/java/br/edu/imepac/commons/entities/ConsultaEntity.java
- commons/src/main/java/br/edu/imepac/commons/entities/HorarioDisponivelEntity.java
- commons/src/main/java/br/edu/imepac/commons/repositories/ConsultaRepository.java
- commons/src/main/java/br/edu/imepac/commons/repositories/HorarioDisponivelRepository.java
- commons/src/main/java/br/edu/imepac/commons/services/ConsultaService.java
- commons/src/test/java/br/edu/imepac/commons/services/ConsultaServiceTest.java
- agendamento/src/main/java/br/edu/imepac/agendamento/config/RestTemplateConfig.java
- agendamento/src/main/java/br/edu/imepac/agendamento/config/SwaggerConfig.java
- agendamento/src/main/java/br/edu/imepac/agendamento/clients/AdministrativoClient.java
- agendamento/src/main/java/br/edu/imepac/agendamento/dtos/ConsultaRequest.java
- agendamento/src/main/java/br/edu/imepac/agendamento/dtos/ConsultaResponse.java
- agendamento/src/main/java/br/edu/imepac/agendamento/dtos/ReagendarRequest.java
- agendamento/src/main/java/br/edu/imepac/agendamento/controllers/ConsultaController.java
**Rotas expostas (porta 8082):**
- POST   /v1/consultas                       — Agenda nova consulta (valida convenio ativo + conflito)
- DELETE /v1/consultas/{id}                  — Cancela consulta (soft delete via status)
- PATCH  /v1/consultas/{id}/reagendar        — Reagenda com revalidacao de conflito
- PATCH  /v1/consultas/{id}/confirmar        — Confirma consulta pendente
- GET    /v1/consultas?medicoId|pacienteId|data — Lista com filtros prioritarios
- GET    /v1/consultas/minha-agenda?medicoId — Pendentes do medico
**Entidades:**
- StatusConsulta (enum): PENDENTE, CONFIRMADA, REALIZADA, CANCELADA
- ConsultaEntity: id, pacienteId, medicoId, convenioId, dataHora, status, observacoes
- HorarioDisponivelEntity: id, medicoId, dataHora, ocupado
**Comunicacao HTTP:**
- AdministrativoClient consulta /v1/convenios/{id}, /v1/medicos/{id}, /v1/pacientes/{id} no administrativo (porta 8081)
- isConvenioAtivo() trata 404 como inativo
**Validacoes:**
- Conflito de horario (no Service): existsByMedicoIdAndDataHoraAndStatusNot(medicoId, dataHora, CANCELADA)
- Convenio ativo (no Controller): chamada HTTP antes de salvar
**Testes:** 9 testes Mockito cobrindo agendar (com/sem conflito), cancelar, reagendar, confirmar, finders
**Observacoes:**
- Referencias por Long id — sem @ManyToOne entre bancos diferentes (admin vs agendamento)
- @Value("${administrativo.url}") usa exatamente a chave do application.properties
- Toda consulta nasce PENDENTE; agendar() sobrescreve status enviado pelo cliente

---

### 2026-05-07 — Convenio
**Agente:** backend-dev (referencia — implementado manualmente)
**Arquivos criados:**
- commons/src/main/java/br/edu/imepac/commons/entities/ConvenioEntity.java
- commons/src/main/java/br/edu/imepac/commons/repositories/ConvenioRepository.java
- commons/src/main/java/br/edu/imepac/commons/services/ConvenioService.java
- commons/src/test/java/br/edu/imepac/commons/services/ConvenioServiceTest.java
- administrativo/src/main/java/br/edu/imepac/administrativo/dtos/ConvenioRequest.java
- administrativo/src/main/java/br/edu/imepac/administrativo/dtos/ConvenioResponse.java
- administrativo/src/main/java/br/edu/imepac/administrativo/controllers/ConvenioController.java
**Rotas expostas:**
- GET    /v1/convenios        — Lista todos os convenios
- GET    /v1/convenios/{id}   — Busca convenio por ID
- POST   /v1/convenios        — Cria novo convenio
- PUT    /v1/convenios/{id}   — Atualiza convenio
- DELETE /v1/convenios/{id}   — Remove convenio
**Entidades:**
- ConvenioEntity: id, nome, descricao
**Observacoes:**
- Modulo de referencia. Todo novo modulo deve seguir esta estrutura.
- Testes cobrem: findAll, findById (encontrado e nao encontrado), save, update, delete

---

### 2026-05-07 — Paciente
**Agente:** backend-dev
**Arquivos criados:**
- commons/src/main/java/br/edu/imepac/commons/entities/PacienteEntity.java
- commons/src/main/java/br/edu/imepac/commons/repositories/PacienteRepository.java
- commons/src/main/java/br/edu/imepac/commons/services/PacienteService.java
- commons/src/test/java/br/edu/imepac/commons/services/PacienteServiceTest.java
- administrativo/src/main/java/br/edu/imepac/administrativo/dtos/PacienteRequest.java
- administrativo/src/main/java/br/edu/imepac/administrativo/dtos/PacienteResponse.java
- administrativo/src/main/java/br/edu/imepac/administrativo/controllers/PacienteController.java
**Rotas expostas:**
- GET    /v1/pacientes        — Lista todos os pacientes
- GET    /v1/pacientes/{id}   — Busca paciente por ID
- POST   /v1/pacientes        — Cria novo paciente
- PUT    /v1/pacientes/{id}   — Atualiza paciente
- DELETE /v1/pacientes/{id}   — Remove paciente
**Entidades:**
- PacienteEntity: id, nome, cpf, telefone, email, endereco, convenioId
**Observacoes:**
- convenioId como Long (referencia ao ConvenioEntity) — sem @ManyToOne pois estao no mesmo banco mas seguindo o padrao do projeto
- Testes cobrem: findAll, findById (encontrado e nao encontrado), save, update (encontrado e nao encontrado), delete (encontrado e nao encontrado)

---

### 2026-05-09 — Docker e Infraestrutura
**Agente:** backend-dev
**Arquivos modificados:**
- docker-compose.yml — adicionado multi-container setup
- Dockerfile (agendamento) — multi-stage build
**Conteudo:**
- 6 containers: administrativo, agendamento, atendimento, 3x MySQL (clinica_administrativo, clinica_agendamento, clinica_atendimento)
- Networking via docker-compose (containers acessam-se por hostname)
- Validacao: 6 containers up, 3 Swaggers respondendo 200
**Observacoes:**
- cada microsservico tem seu proprio banco de dados
- Ready para CI/CD e deploy

---

### 2026-05-11 — Patches criticos em Convenio e Complementos em Paciente
**Agente:** backend-dev
**Branch:** patches-convenio-paciente (PR #3, mergeada em main)
**Periodo:** 08:30-11:38, 41 commits

**Arquivos modificados — Convenio:**
- commons/src/main/java/br/edu/imepac/commons/entities/ConvenioEntity.java
  - Adicionado: ativo (Boolean, default=true), cnpj (String, @NotBlank, unique), telefone (String, nullable)
  - Soft delete pattern — convenios inativos ficam no banco mas nao aparecem em listagens de agendamento
- commons/src/main/java/br/edu/imepac/commons/repositories/ConvenioRepository.java
  - Adicionado: findByAtivo(Boolean ativo)
- commons/src/main/java/br/edu/imepac/commons/services/ConvenioService.java
  - Adicionado: findByAtivo(), alterarStatus() com tratamento de 404
- commons/src/test/java/br/edu/imepac/commons/services/ConvenioServiceTest.java
  - +6 testes (findByAtivo 3 cenarios, alterarStatus 3 cenarios) → total 13 testes, 0 falhas
- administrativo/src/main/java/br/edu/imepac/administrativo/controllers/ConvenioController.java
  - Novo endpoint: PATCH /v1/convenios/{id}/status
  - Documentacao completa com @Tag, @Operation, @ApiResponse
- administrativo/src/main/java/br/edu/imepac/administrativo/dtos/AlterarStatusRequest.java (novo arquivo)
  - DTO minimo para PATCH: { ativo: Boolean }
  - @Schema, @NotNull com validacao

**Arquivos modificados — Paciente:**
- commons/src/main/java/br/edu/imepac/commons/entities/PacienteEntity.java
  - Adicionado: dataNascimento (LocalDate, nullable) — compatibilidade com registros antigos
- commons/src/main/java/br/edu/imepac/commons/repositories/PacienteRepository.java
  - Adicionado: findByNomeContainingIgnoreCase(String nome)
  - Adicionado: findByCpf(String cpf) → Optional
  - Adicionado: findByConvenioId(Long convenioId)
- commons/src/main/java/br/edu/imepac/commons/services/PacienteService.java
  - Adicionado: buscarComFiltros(nome, cpf, convenioId) com prioridade nome > cpf > convenioId > findAll
  - Adicionado: findByNome(), findByCpf(), findByConvenioId()
- commons/src/test/java/br/edu/imepac/commons/services/PacienteServiceTest.java
  - +10 testes (buscarComFiltros 5 cenarios, metodos de busca 5 cenarios) → total 18 testes, 0 falhas
- administrativo/src/main/java/br/edu/imepac/administrativo/controllers/PacienteController.java
  - GET /v1/pacientes agora com query params opcionais (nome, cpf, convenioId)
  - Documentacao @Tag, @Operation, @ApiResponse, @Parameter
- administrativo/src/main/java/br/edu/imepac/administrativo/dtos/PacienteRequest.java
  - Adicionado: dataNascimento (LocalDate)
  - @JsonFormat(pattern="yyyy-MM-dd") em dataNascimento
  - @Schema em todos os campos
- administrativo/src/main/java/br/edu/imepac/administrativo/dtos/PacienteResponse.java
  - Adicionado: dataNascimento (LocalDate)
  - @JsonFormat(pattern="yyyy-MM-dd") em dataNascimento
  - @Schema em todos os campos

**Rotas expostas — NOVAS:**
- PATCH   /v1/convenios/{id}/status — Ativa ou inativa convenio
- GET    /v1/pacientes?nome=X      — Busca parcial por nome
- GET    /v1/pacientes?cpf=X       — Busca por CPF exato
- GET    /v1/pacientes?convenioId=X — Filtra por convenio

**Cobertura de testes:**
- ConvenioServiceTest: 13 testes (findAll, findById, findByAtivo, save, update, delete, alterarStatus)
- PacienteServiceTest: 18 testes (findAll, findById, findByNome, findByCpf, findByConvenioId, save, update, buscarComFiltros, delete)
- BUILD SUCCESS: 31 testes, 0 falhas

**Documentacao:**
- Swagger/OpenAPI 3.0 completo em ConvenioController e PacienteController
- @Tag, @Operation, @ApiResponse em todos os endpoints
- @Parameter com descricoes nos query params
- @Schema em todos os DTOs com exemplos
- Comentarios inline naturais em todos os arquivos modificados (19 commits de refatoracao/documentacao)

**Decisoes:**
- Soft delete (campo ativo) para convenios — auditoria; convenios inativos nao deletados
- PATCH em vez de PUT para alterar status — menos transferencia de dados
- Busca prioritaria em Paciente — nome tem prioridade sobre CPF, etc.
- LocalDate em vez de LocalDateTime — simplifica; sem confusoes de timezone
- @JsonFormat nos DTOs — explícita a esperado em JSON (yyyy-MM-dd)

**Impacto:**
- ConvenioEntity.ativo existe e documentado → **Pessoa 4 desbloqueada** para validacao de convenio no Agendamento

**Observacoes gerais:**
- Padrão de implementação consolidado (7 passos)
- Committers: Pessoa 1 do trabalho em grupo (41 commits)
- Documentacao no Obsidian criada para referenciar estas mudancas

---

### 2026-05-11 — Especialidade e Médico
**Agente:** backend-dev (PR #4, mergeada em main)
**Colaborador:** Pedro Henrique Fernandes
**Data:** 2026-05-11, apos validacao com Postman

**Arquivos criados — Especialidade:**
- commons/src/main/java/br/edu/imepac/commons/entities/EspecialidadeEntity.java
  - Campos: id (PK), nome (String, @NotBlank, length 100), descricao (String, length 300, nullable)
- commons/src/main/java/br/edu/imepac/commons/repositories/EspecialidadeRepository.java
  - Heranca: JpaRepository<EspecialidadeEntity, Long>
  - Nenhum metodo customizado
- commons/src/main/java/br/edu/imepac/commons/services/EspecialidadeService.java
  - Metodos: findAll(), findById(Long), save(), update(), deleteById()
- commons/src/test/java/br/edu/imepac/commons/services/EspecialidadeServiceTest.java
  - 7 testes: findAll, findById, save, update, updateNaoExistir, deleteById, deleteByIdNaoExistir
- administrativo/src/main/java/br/edu/imepac/administrativo/dtos/EspecialidadeRequest.java
  - @Schema, @NotBlank nos campos
- administrativo/src/main/java/br/edu/imepac/administrativo/dtos/EspecialidadeResponse.java
  - @Schema em todos os campos
- administrativo/src/main/java/br/edu/imepac/administrativo/controllers/EspecialidadeController.java
  - 5 endpoints em /v1/especialidades
  - Documentacao completa: @Tag, @Operation, @ApiResponse

**Arquivos criados — Médico:**
- commons/src/main/java/br/edu/imepac/commons/entities/MedicoEntity.java
  - Campos: id (PK), nome (String, @NotBlank, length 150), crm (String, @NotBlank, unique), senha (String, @NotBlank), telefone (String, length 20, nullable), ativo (Boolean, default true), especialidades (Set<EspecialidadeEntity>, @ManyToMany)
  - Tabela de juncao: medico_especialidade (medico_id, especialidade_id)
- commons/src/main/java/br/edu/imepac/commons/repositories/MedicoRepository.java
  - Heranca: JpaRepository<MedicoEntity, Long>
  - Metodos customizados: findByCrm(String crm), findByAtivo(Boolean ativo)
- commons/src/main/java/br/edu/imepac/commons/services/MedicoService.java
  - Metodos: findAll(), findById(), save(), update(), inativar(), associarEspecialidade(), removerEspecialidade(), deleteById()
- commons/src/test/java/br/edu/imepac/commons/services/MedicoServiceTest.java
  - 12 testes: findAll, findById, save, update, updateNaoExistir, deleteById, deleteByIdNaoExistir, inativar, inativarNaoExistir, associarEspecialidade, associarEspecialidadeNaoExistir, removerEspecialidade
- administrativo/src/main/java/br/edu/imepac/administrativo/dtos/MedicoRequest.java
  - @Schema, @NotBlank nos campos obrigatorios
- administrativo/src/main/java/br/edu/imepac/administrativo/dtos/MedicoResponse.java
  - Inclui especialidades (List<EspecialidadeResponse>)
  - **Nota IMPORTANTE:** Senha NUNCA exposta na resposta
- administrativo/src/main/java/br/edu/imepac/administrativo/controllers/MedicoController.java
  - 8 endpoints em /v1/medicos
  - Documentacao completa: @Tag, @Operation, @ApiResponse

**Arquivos adicionados:**
- docs/especialidade-medico-collection.json (Postman Collection com 14 requests)

**Rotas expostas — Especialidade:**
- GET    /v1/especialidades        — Lista todas
- GET    /v1/especialidades/{id}   — Busca por ID
- POST   /v1/especialidades        — Cria nova (201 ou 400)
- PUT    /v1/especialidades/{id}   — Atualiza (200 ou 404)
- DELETE /v1/especialidades/{id}   — Remove (204 ou 404)

**Rotas expostas — Médico:**
- GET    /v1/medicos               — Lista todos
- GET    /v1/medicos/{id}          — Busca por ID
- POST   /v1/medicos               — Cria novo (201 ou 400)
- PUT    /v1/medicos/{id}          — Atualiza (200 ou 404)
- DELETE /v1/medicos/{id}          — Remove (204 ou 404)
- PATCH  /v1/medicos/{id}/inativar — Inativa medico (soft delete)
- POST   /v1/medicos/{id}/especialidades/{especialidadeId} — Associa especialidade
- DELETE /v1/medicos/{id}/especialidades/{especialidadeId} — Remove especialidade

**Cobertura de testes:**
- EspecialidadeServiceTest: 7 testes
- MedicoServiceTest: 12 testes
- Postman Collection: 14 requests com 33 assertions
- BUILD SUCCESS: 50 testes totais (31 anteriores + 19 novos), 0 falhas

**Documentacao:**
- Swagger/OpenAPI 3.0 completo em EspecialidadeController e MedicoController
- @Tag, @Operation, @ApiResponse em todos os endpoints
- @Schema em todos os DTOs
- Postman tests validam ausencia de senha no response (seguranca)
- Documentacao no Obsidian atualizada: entidades.md, rotas-api.md, modulo-compartilhado.md, diario-de-desenvolvimento.md

**Decisoes:**
- Especialidade como entidade separada — reutilizavel por multiplos medicos
- @ManyToMany medico-especialidade — medico pode ter varias especialidades
- Soft delete em medico (campo ativo) — nao deletar dados; consistente com padrao de Convenio
- Senha em MedicoEntity mas NUNCA exposta em MedicoResponse — seguranca; preparacao para JWT futuro
- @NotBlank em nome, crm, senha — validacoes obrigatorias
- unique=true em crm — um medico por CRM

**Impacto:**
- Modulos Especialidade e Medico prontos para Agendamento fazer reservas com validacao de medico disponivel
- MedicoEntity.ativo existe → próximo: validacao de medicos ativos no agendamento

**Observacoes gerais:**
- Padrão CRUD completamente estabelecido (7 passos)
- Testes com Mockito + Postman = cobertura dupla
- Senha fieldnao exposado em response = seguindo boas praticas de seguranca

---

### 2026-05-12 — Segurança: proteção de senha e validação de CRM duplicado
**Branch:** fix/security-crm-validation
**Periodo:** sessao de revisao pós-merge do PR #6

**Problema corrigido:**
- `MedicoEntity.senha` e `AtendenteEntity.senha` podiam vazar em logs via `toString()` gerado pelo Lombok `@Data`
- `MedicoService` não validava CRM duplicado antes de salvar — dependia só da constraint de banco (lançava 500 em vez de 409)

**Arquivos modificados:**
- `commons/entities/MedicoEntity.java` — @JsonIgnore + @ToString(exclude="senha")
- `commons/entities/AtendenteEntity.java` — @JsonIgnore + @ToString(exclude="senha")
- `commons/config/ModelMapperConfig.java` — setSkipNullEnabled(true) + comentario explicativo
- `commons/services/MedicoService.java` — validarCrmDisponivel(), findByCrm() publico
- `commons/test/services/MedicoServiceTest.java` — +8 testes (crm duplicado, findById vazio, findByAtivo vazio, especialidade nao existe)
- `administrativo/controllers/MedicoController.java` — 409 no POST e PUT, comentarios
- `administrativo/controllers/AtendenteController.java` — comentarios
- `administrativo/services/AtendenteService.java` — comentarios
- `administrativo/dtos/MedicoRequest.java` — descricoes Swagger atualizadas
- `administrativo/dtos/MedicoResponse.java` — nota de omissao de senha
- `administrativo/dtos/AtendenteResponse.java` — nota de omissao de senha
- `docs/especialidade-medico-collection.json` — sem-senha em PUT e PATCH, request 409 CRM duplicado

**Cobertura de testes:**
- MedicoServiceTest: 20 testes (eram 12)
- Postman: 2 novos asserts + 1 novo request
- BUILD SUCCESS: 70 testes, 0 falhas

**Decisoes:**
- @JsonIgnore na entity em vez de só confiar no DTO — defesa em profundidade
- @ToString(exclude) para nao vazar senha em stack traces
- validarCrmDisponivel segue exatamente o padrao de validarUsuarioDisponivel do AtendenteService
