# Changelog de Implementacoes — Agentes

Este arquivo e atualizado automaticamente pelo agente `backend-dev` apos cada implementacao.
O agente `doc-writer` le este arquivo para gerar documentacao no Obsidian.

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
