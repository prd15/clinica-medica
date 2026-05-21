# Code Review — Clinica Medica (multi-modulo) — 2026-05-15

**Branch:** main
**Escopo:** projeto inteiro (commons, administrativo, agendamento, atendimento, docs/postman)

## Resumo Executivo

Projeto em estado bom, padrao do Convenio replicado com consistencia em Paciente / Medico / Atendente / Especialidade / Consulta / Atendimento. Arquitetura multi-modulo coerente, sem `@ManyToOne` cross-bank (referencias via `Long id`). Os modulos novos (Agendamento, Atendimento) seguem o padrao, mas concentram problemas pontuais que devem ser tratados antes de "produzir": ausencia de timeouts no `RestTemplate`, ausencia de `@ControllerAdvice` global (tratamento de excecao espalhado em try/catch), suspeita de N+1 quando `MedicoEntity.especialidades` (LAZY) e serializada em `MedicoResponse`, falta de validacao de `medicoAtivo` ao agendar consulta, falta de set automatico de `prontuarioId` no `AtendimentoResponse` (sempre devolve null) e endpoint de relatorio `consultas-diarias` retornando dado mock.

| Categoria | Criticos | Medios | Melhorias |
|---|---|---|---|
| **TOTAL** | **6** | **11** | **9** |

---

## 🔴 CRITICO — corrigir antes de producao

| # | Arquivo | Linha | Problema | Sugestao |
|---|---------|-------|----------|----------|
| C1 | `agendamento/.../RestTemplateConfig.java` + `atendimento/.../AppConfig.java` | 13 / 11 | `new RestTemplate()` sem timeouts. Em producao, se o administrativo ficar lento ou cair, o agendamento bloqueia threads ate o TCP timeout do SO (minutos). Pode derrubar o microsservico inteiro sob carga. | Usar `RestTemplateBuilder.setConnectTimeout(Duration.ofSeconds(3)).setReadTimeout(Duration.ofSeconds(5)).build()`. |
| C2 | `atendimento/.../controllers/AtendimentoController.java` | 41-61, especificamente 60 | `realizar()` SEMPRE devolve `prontuarioId = null` no response (o helper `toResponse(salvo, null)` e chamado com `null` fixo). O Service cria o prontuario mas nao expoe o id. Cliente nao tem como buscar o prontuario recem-criado. | `AtendimentoService.registrar` deve devolver tambem o prontuario salvo (ou um wrapper) e o controller passar `prontuario.getId()` para o response. |
| C3 | `atendimento/.../controllers/AtendimentoController.java` | 54-58 | Notificacao ao agendamento (`confirmarRealizacao`) e capturada com `catch (Exception)` e apenas `System.err.println`. Se cair, atendimento fica REALIZADO no banco do atendimento mas consulta fica PENDENTE/CONFIRMADA no agendamento — divergencia de estado sem retry, sem log estruturado, sem fila/outbox. | No minimo trocar `System.err.println` por SLF4J `log.warn`. Idealmente: outbox/retry, ou compensar (rollback) se a notificacao falhar. Documentar a inconsistencia conhecida. |
| C4 | `agendamento/.../controllers/ConsultaController.java` | 43-66 | `agendar()` valida convenio ativo via REST, mas **nao valida** se o medico existe nem se esta ativo, nem se o paciente existe. `AdministrativoClient` ja possui `buscarMedico`/`buscarPaciente` mas nao sao chamados. Permite agendar com medicoId/pacienteId inventados. | Adicionar `administrativoClient.buscarMedico(...).getStatusCode() == 200 && body.ativo == true` (e equivalente para paciente). Criar `isMedicoAtivo(Long)`. |
| C5 | `commons/.../entities/MedicoEntity.java` | 47-53 + `MedicoResponse.java` 33 | `especialidades` e LAZY mas `MedicoResponse` expoe `Set<EspecialidadeResponse>`. Em endpoints fora de transacao (controllers), ModelMapper acessa a colecao -> N+1 em `findAll()` (uma query extra por medico) e risco de `LazyInitializationException` se a sessao fechar antes do mapeamento. | Anotar listagens com `@Transactional(readOnly = true)` ou usar `@EntityGraph(attributePaths = "especialidades")` em `MedicoRepository.findAll`, ou criar um Response sem especialidades para listagem. |
| C6 | `administrativo/.../controllers/RelatorioController.java` | 38-44 | Endpoint `/v1/relatorios/consultas-diarias` retorna sempre `new ConsultaDiariaRelatorioResponse(data, 0)` — valor 0 hard-coded. Esta documentado no Swagger como um relatorio funcional mas e mock. Iludem-se consumidores. | Implementar consulta real (via `ConsultaService.findByData` chamado do agendamento, ou marcar como `@Hidden` no Swagger ate a integracao existir). |

---

## 🟡 MEDIO — tratar nesta sprint

| # | Arquivo | Linha | Problema | Sugestao |
|---|---------|-------|----------|----------|
| M1 | TODO o projeto | — | Nao existe `@ControllerAdvice` / `@RestControllerAdvice` global. Cada controller repete try/catch para `IllegalStateException`, `IllegalArgumentException`, `NoSuchElementException`. Codigo duplicado, formato de erro inconsistente (uns retornam String, outros body vazio). | Criar `GlobalExceptionHandler` mapeando essas excecoes para `ResponseEntity` com payload padronizado `{timestamp, status, error, message}`. |
| M2 | `agendamento/.../controllers/ConsultaController.java` | 43, 73, 88, 104 | Tipos de retorno `ResponseEntity<Object>` para conseguir misturar String/DTO no body. Cliente nao tem schema previsivel. | Padronizar com um `ErrorResponse` DTO e `ResponseEntity<ConsultaResponse>` no caminho feliz. Mover tratamento para o `@ControllerAdvice`. |
| M3 | `agendamento/.../controllers/ConsultaController.java` | 130 | `LocalDate.parse(data)` sem try/catch. Param invalido (ex.: `?data=hoje`) gera `DateTimeParseException` -> 500. | Trocar por `@RequestParam @DateTimeFormat(iso=DATE) LocalDate data` ou capturar e devolver 400. |
| M4 | `agendamento/.../controllers/ConsultaController.java` | 132-135 | Listar sem filtros retorna lista vazia silenciosamente — comportamento surpresa. Deveria ser 400 "filtro obrigatorio" ou retornar todos paginados. | Documentar no Swagger e/ou retornar 400 explicito. |
| M5 | `atendimento/.../controllers/AtendimentoController.java` | 66, 74 | Retornam `ResponseEntity<AtendimentoEntity>` e `ResponseEntity<ProntuarioEntity>` (Entity, nao DTO). Viola o padrao do projeto e expõe campos internos. | Criar `ProntuarioResponse`/`HistoricoResponse` e mapear com ModelMapper. |
| M6 | `commons/.../services/AtendimentoService.java` | 78-87 | `solicitarExame` busca o atendimento so para validar existencia mas nao usa o objeto; em paralelo, `prontuarioRepository.findFirstByAtendimentoIdOrderByIdDesc` revela que pode existir mais de um prontuario por atendimento — modelagem ambigua. | Definir 1:1 atendimento/prontuario por design (unique constraint em `prontuarios.atendimento_id`). |
| M7 | `commons/.../services/MedicoService.java` | 45-55 | `update` reescreve `senha` com o valor recebido — se o request omitir senha, ela vira null (e o `@NotBlank` no Request impede isso na criacao mas o Service nao se defende). Mesma situacao em `AtendenteService.update` linha 45. | Conferir `dadosAtualizados.getSenha() != null && !blank` antes de sobrescrever; ou separar endpoint `PATCH /senha`. |
| M8 | `commons/.../services/AtendimentoService.java` | 37-55 | `@Transactional` esta correto, mas os tres repositorios envolvidos (atendimento + prontuario + anotacoes) compartilham o mesmo `DataSource` (banco `clinica_atendimento`) — OK. Porem nao ha rollback orquestrado caso a notificacao REST ao agendamento falhe (vide C3). | Decisao explicita: ou outbox, ou aceitar inconsistencia documentada. |
| M9 | `commons/.../services/ConsultaService.java` | 35-39 | Conflito de horario considera apenas `dataHora` exata (== timestamp). Duas consultas com 1 minuto de diferença nao conflitam, mas na pratica e um conflito (consulta dura ~30min). | Definir SLOT (ex.: 30min) e checar overlap com `findByMedicoIdAndDataHoraBetween`. |
| M10 | `agendamento/.../clients/AdministrativoClient.java` | 27, 53, 63 | Uso de `Map` cru com `@SuppressWarnings("rawtypes")` para serializar resposta. Frágil: qualquer renomeação de campo em `ConvenioResponse` quebra silenciosamente (`get("ativo")` devolve null -> trata como inativo). | Criar DTO compartilhado em `commons/dtos/` (ou no proprio agendamento) tipado: `ConvenioRefDTO { id, ativo }`. |
| M11 | `atendimento/.../clients/AgendamentoClient.java` | 22 | `restTemplate.patchForObject(...)` retorna `Void.class` — para PATCH o RestTemplate por default usa HttpUrlConnection que **nao suporta PATCH** sem um `HttpComponentsClientHttpRequestFactory`. Pode falhar em runtime dependendo do JDK / config. | Configurar `HttpComponentsClientHttpRequestFactory` no bean RestTemplate, ou substituir por POST/PUT. |

---

## 🟢 MELHORIA — backlog

| # | Arquivo | Linha | Observacao | Sugestao |
|---|---------|-------|------------|----------|
| L1 | `commons/.../entities/MedicoEntity.java`, `AtendenteEntity.java` | 39 / 36 | Senha em plaintext (sabido — fora de escopo academico). | Usar BCrypt quando seguranca virar requisito. |
| L2 | `atendimento/.../controllers/AtendimentoController.java` | 100-110 | Mapping manual `toResponse(...)`. Padrao do projeto usa ModelMapper. | Substituir por `modelMapper.map(salvo, AtendimentoResponse.class)`. |
| L3 | `commons/.../services/ConsultaService.java` | 13-119 | Service NAO valida medico/convenio/paciente — toda validacao cross-bank fica no controller. Acoplamento de regra com transporte HTTP. | Criar `ConsultaValidationService` em `agendamento/` que orquestra `AdministrativoClient` + delega ao `ConsultaService`. |
| L4 | `commons/.../services/AtendimentoService.java` | 43-44 | `status` modelado como String literal `"REALIZADO"`. | Criar `enum StatusAtendimento { EM_ANDAMENTO, REALIZADO }`. |
| L5 | `commons/.../entities/AtendimentoEntity.java` | 35 | Campo `status` sem `@Enumerated`. Ao virar enum lembrar de `@Enumerated(EnumType.STRING)`. | Documentado: `StatusConsulta` ja usa STRING. |
| L6 | `commons/.../services/AtendimentoService.java` | 90-94 | Sem rota para listar `anotacoes` nem `exames` de um atendimento, embora repositorios tenham `findByProntuarioId`/`findByAtendimentoId`. | Adicionar `GET /v1/atendimentos/{id}/anotacoes` e `/exames`. |
| L7 | `commons/.../test/AtendimentoServiceTest.java` | 1-84 | Apenas 3 cenarios; nao cobre `solicitarExame`, nem caminho feliz de `adicionarAnotacao`, nem `buscarHistoricoPorPaciente`. Tamanho 84 vs 230 do ConsultaServiceTest. | Adicionar testes: sucesso de anotacao, sucesso de exame, atendimento inexistente em `solicitarExame`, historico vazio. |
| L8 | `docs/*.json` (postman) | — | Boa cobertura geral. | Adicionar testes negativos no atendimento (consulta inexistente em POST). |
| L9 | `commons/.../config/ModelMapperConfig.java` | 16 | `setSkipNullEnabled(true)` ajuda em update; matching loose (default) ainda pode mapear `pacienteId -> id`. | Considerar `MatchingStrategies.STRICT` global. |

---

## Microsservicos Agendamento + Atendimento — qualidade dos modulos novos

### Agendamento (porta 8082)
- `ConsultaEntity` segue padrao: ids como `Long`, sem `@ManyToOne` cross-bank. ✓
- `StatusConsulta` enum persistido com `@Enumerated(EnumType.STRING)`. ✓
- `ConsultaService` valida data passado, conflito de horario, transicoes terminais (REALIZADA/CANCELADA). ✓
- `ConsultaServiceTest` cobre 12 cenarios incluindo time-bomb protection (datas relativas). ✓ Excelente cobertura.
- **Lacunas**: validacao de medico ativo (C4); conflito por slot (M9); error handling em controller (M2/M3/M4); RestTemplate sem timeout (C1); DTO cru `Map` (M10).

### Atendimento (porta 8083)
- Entidades `AtendimentoEntity`, `ProntuarioEntity`, `AnotacaoEntity`, `SolicitacaoExameEntity` corretas, ids como Long. ✓
- `@Transactional` em `registrar()`. ✓
- `AgendamentoClient` desacoplado, URL via `@Value`. ✓
- **Lacunas**: `prontuarioId` sempre null no response (C2); notificacao silenciosa (C3); PATCH sem HttpComponents (M11); retorna Entity ao inves de DTO (M5); cobertura de teste insuficiente (L7); enum de status (L4); endpoints faltantes para listar anotacoes/exames (L6).

---

## Pontos positivos

- **Estrutura multi-modulo limpa**: entities/repositories/services em `commons/`, controllers/DTOs nos microsservicos — `CLAUDE.md` rigorosamente respeitado.
- **Sem `@ManyToOne` cross-bank**: 100% das referencias inter-microsservico via `Long id` com comentarios explicativos.
- **Seguranca de senha em response**: `@JsonIgnore` + `@ToString(exclude = "senha")` em `MedicoEntity` / `AtendenteEntity`, `AccessMode.WRITE_ONLY` no Schema dos Requests, ausencia de campo `senha` nos Response DTOs.
- **Versionamento `/v1/` consistente em TODOS os controllers**.
- **`@Valid` em todos `@RequestBody`**.
- **Testes Mockito puros**: nenhum `@SpringBootTest`, todos usam `@ExtendWith(MockitoExtension.class)`.
- **`StatusConsulta` persistido como STRING** (nunca ORDINAL).
- **`application.properties` SEM senha hardcoded**: `${DB_PASS:}` em todos os modulos.
- **Postman collections completas (8 arquivos)** cobrindo setup + casos felizes + casos de erro (400, 404, conflito, status terminais). Total: 198 assertions, 0 falhas.
- **Swagger documentado** com `@Operation`, `@ApiResponse`, `@Schema` em praticamente todos os endpoints.
- **Sem `@CrossOrigin(*)`** em codigo de producao.

---

## Faltando implementar / pendencias

- **`@ControllerAdvice` global** (resolve M1, M2 de uma vez).
- **Validacao cruzada medico/paciente no agendar** (C4).
- **Exposicao do `prontuarioId` no response do atendimento** (C2).
- **Relatorio `consultas-diarias` real** (C6).
- **Listagem de anotacoes e exames** (L6).
- **Cobertura de testes do Atendimento** (L7).
- **Timeouts no RestTemplate** (C1).
- **Configuracao do PATCH com Apache HttpComponents** (M11).

---

## Proximos passos (ordem sugerida antes do merge para producao)

1. **(C1)** Configurar timeouts no RestTemplate (resolve dois modulos em uma PR de ~10 linhas).
2. **(C2)** Devolver `prontuarioId` no response do atendimento.
3. **(C4)** Validar medicoAtivo no agendamento.
4. **(M1)** Criar `GlobalExceptionHandler` em ambos microsservicos.
5. **(C5)** `@EntityGraph` ou Response sem especialidades em listagens de medico.
6. **(C6)** Implementar relatorio real ou `@Hidden`.
7. **(M11)** HttpComponents client factory.
8. **(L7)** Subir testes do Atendimento para paridade com ConsultaServiceTest.

---

## Status final

**Aprovado com ressalvas.** O codigo esta solido e segue o padrao do projeto com qualidade acima da media para um trabalho academico. Os 6 itens criticos sao todos enderecaveis em PRs pequenas e isoladas. Nenhum deles bloqueia uso em ambiente de desenvolvimento; em producao apenas C1, C3 e C4 sao bloqueantes.

---

## Rodada de follow-up — melhorias pos review

Depois que os PRs #13 e #14 fecharam praticamente todos os itens criticos e medios deste relatorio, sobraram apenas ajustes pequenos e seguros, tratados na branch `refactor/code-review-melhorias`:

- **show-sql configuravel** nos tres modulos: deixou de ser `true` fixo e passou a `${SPRING_JPA_SHOW_SQL:false}`, com o `format_sql` seguindo o mesmo toggle. Padrao desligado em producao, ligavel por variavel de ambiente quando o dev quer ver as queries.
- **DRY no AdministrativoClient**: os tres `isXAtivo` repetiam o mesmo try/catch; viraram um helper unico que centraliza o caminho fail-safe.
- **`@Transactional(readOnly = true)`** nos metodos de leitura dos services. Em `MedicoService` resolve risco real de `LazyInitializationException` (especialidades LAZY); nos demais e convencao de camada.
- **Collection da consulta** realinhada ao comportamento atual (409 para conflito/status terminal, 400 quando falta filtro).

Verificacao: 100 testes unitarios verdes, 205 assertions no Postman (0 falhas) e Swagger carregando nos tres servicos.

> Este projeto vem sendo documentado e os code reviews conduzidos com o apoio do Claude.
