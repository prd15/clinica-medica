# Relatório de Testes — Clínica Médica
**Data:** 2026-05-09
**Ambiente:** Docker Desktop (Windows) — localhost
**Executado por:** Agente de Testes (Claude Code)

---

## Problemas encontrados e corrigidos durante os testes

### 1. Containers sem port binding no host
O `docker-compose.yml` não tinha `ports:` para os serviços de aplicação (projetado para OrbStack).
**Fix:** Adicionados `ports: 8081:8081`, `8082:8082`, `8083:8083` nos 3 microsserviços.

### 2. Bug: `@PathVariable` sem nome explícito
`GET /v1/convenios/{id}`, `PUT /v1/convenios/{id}` e `DELETE /v1/convenios/{id}` retornavam 500.
Causa: compilador sem flag `-parameters`, Spring 6 exige nome explícito.
**Fix:** `@PathVariable Long id` → `@PathVariable("id") Long id` em ConvenioController e PacienteController.

---

## Resultado por Serviço

### Administrativo (porta 8081)
| Teste | Rota | Esperado | Obtido | Status |
|-------|------|----------|--------|--------|
| 1.1 | POST /v1/convenios | 201 | 201 | ✅ |
| 1.2 | GET /v1/convenios | 200 | 200 | ✅ |
| 1.3 | GET /v1/convenios/{id} | 200 | 200 | ✅ |
| 1.4 | PUT /v1/convenios/{id} | 200 | 200 | ✅ |
| 1.5 | GET /v1/convenios/99999 | 404 | 404 | ✅ |
| 1.6 | POST /v1/convenios (inválido) | 400 | 400 | ✅ |
| 1.7 | POST /v1/especialidades | 201 | 404 | ⏳ |
| 1.8 | POST /v1/medicos | 201 | 404 | ⏳ |
| 1.9 | POST /v1/pacientes | 201 | 201 | ✅ |

### Agendamento (porta 8082)
| Teste | Rota | Esperado | Obtido | Status |
|-------|------|----------|--------|--------|
| 2.1 | POST /v1/consultas | 201 | — | ⏳ |
| 2.2 | POST /v1/consultas (conflito) | 400/409 | — | ⏳ |
| 2.3 | POST /v1/consultas (convênio inválido) | 404/400 | — | ⏳ |
| 2.4 | PATCH /v1/consultas/{id}/confirmar | 200 | — | ⏳ |
| 2.5 | GET /v1/consultas?medicoId | 200 | — | ⏳ |
| 2.6 | GET /v1/consultas?pacienteId | 200 | — | ⏳ |
| 2.7 | GET /v1/consultas?data | 200 | — | ⏳ |
| 2.8 | GET /v1/consultas/minha-agenda | 200 | — | ⏳ |
| 2.9 | PATCH /v1/consultas/{id}/reagendar | 200 | — | ⏳ |
| 2.10 | DELETE /v1/consultas/{id} | 200/204 | — | ⏳ |

> Bloqueado: MedicoController não implementado — sem MEDICO_ID para agendar consulta.

### Atendimento (porta 8083)
| Teste | Rota | Esperado | Obtido | Status |
|-------|------|----------|--------|--------|
| 3.1 | POST /v1/atendimentos | 201 | — | ⏳ |
| 3.2 | Consulta marcada REALIZADA | status=REALIZADA | — | ⏳ |
| 3.3 | GET /v1/atendimentos/{consultaId} | 200 | — | ⏳ |
| 3.4 | GET /v1/atendimentos/historico | 200 | — | ⏳ |
| 3.5 | POST /v1/atendimentos/{id}/anotacoes | 201 | — | ⏳ |
| 3.6 | POST /v1/atendimentos/{id}/exames | 201 | — | ⏳ |

> Bloqueado: depende de consulta agendada (Etapa 2).

---

## Integridade dos bancos

| Banco | Tabela | Registros |
|-------|--------|-----------|
| clinica_administrativo | convenios | 4 |
| clinica_administrativo | pacientes | 1 |
| clinica_agendamento | (sem tabelas) | — |
| clinica_atendimento | (sem tabelas) | — |

> Bancos de agendamento e atendimento ainda sem tabelas — módulos não implementados.

---

## Legenda
✅ Passou | ❌ Falhou | ⏳ Não implementado ainda

---

## Resumo
- Total de testes executados: 9 (dos 25 planejados)
- Passou: 7 ✅
- Falhou: 0 ❌
- Não implementado / bloqueado: 18 ⏳

---

## Comunicação entre serviços
- Agendamento consultou Administrativo: ⏳ (módulo não implementado)
- Atendimento atualizou Agendamento: ⏳ (módulo não implementado)

---

## Próximos testes a executar quando implementado
1. `POST /v1/especialidades` — EspecialidadeController pendente
2. `POST /v1/medicos` — MedicoController pendente
3. Toda a Etapa 2 (Agendamento) — depende de MedicoController
4. Toda a Etapa 3 (Atendimento) — depende da Etapa 2
