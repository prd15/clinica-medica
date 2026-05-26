# Plano de Execução — Refatoração Arquitetural

**Contexto:** acadêmico, foco em impressionar prof. 2 devs Claude Code, monorepo, sem frontend, OpenFeign confirmado, Flyway opcional.

**Objetivo da demo:** prof vê Postman → JWT do Keycloak → Gateway roteando → 3 microsserviços com domínio próprio → comunicação via Feign declarativo.

---

## Divisão de responsabilidades

| Fase | Tema | Quem | Status |
|------|------|------|--------|
| **0** | Commons + domínio + exceptions | **Pedro (solo, agora)** | 🔄 em andamento |
| **1** | OpenFeign | A ou B (dividir depois) | ⏳ |
| **2** | API Gateway | A ou B (dividir depois) | ⏳ |
| **3** | Keycloak | A ou B (dividir depois) | ⏳ |

---

## Fase 0 — Arquitetura base (Pedro, solo)

Tudo que está aqui deve estar mergeado antes de dividir o trabalho com Dev B.

### 0A — Exceptions tipadas em commons

**Branch:** `refactor/commons-exceptions`

Criar em `commons/src/main/java/br/edu/imepac/commons/exceptions/`:

```java
// BusinessException.java — regra de negócio violada → HTTP 409
public class BusinessException extends RuntimeException {
    public BusinessException(String message) { super(message); }
}

// EntityNotFoundException.java — entidade não encontrada → HTTP 404
public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String entity, Object id) {
        super(entity + " não encontrado com id: " + id);
    }
}

// IntegrationException.java — falha de comunicação → HTTP 503
public class IntegrationException extends RuntimeException {
    public IntegrationException(String message, Throwable cause) { super(message, cause); }
}
```

Atualizar `GlobalExceptionHandler` nos 3 módulos com mapeamento:
- `EntityNotFoundException` → 404
- `BusinessException` → 409
- `IntegrationException` → 503

Refatorar services pra lançar essas exceptions em vez de `IllegalArgumentException`.

**Esforço:** ~2h | **Risco:** baixo

---

### 0B — Quebrar commons + domínio por serviço

**Branch:** `refactor/dominio-por-servico` (parte em cima da 0A)

**Estrutura alvo:**

```
administrativo/src/main/java/br/edu/imepac/administrativo/
├── convenio/
│   ├── ConvenioEntity.java
│   ├── ConvenioRepository.java
│   ├── ConvenioService.java
│   ├── ConvenioController.java        ← já existe, só muda imports
│   └── dto/
│       ├── ConvenioRequest.java       ← já existe
│       └── ConvenioResponse.java      ← já existe
├── paciente/
├── medico/
├── especialidade/
├── atendente/
├── relatorio/
│   └── RelatorioController.java       ← já existe
└── AdministrativoApplication.java     ← remove @EntityScan/@EnableJpaRepositories externos

agendamento/src/main/java/br/edu/imepac/agendamento/
├── consulta/
│   ├── ConsultaEntity.java
│   ├── StatusConsulta.java
│   ├── ConsultaRepository.java
│   ├── ConsultaService.java
│   ├── ConsultaController.java        ← já existe
│   └── dto/
└── clients/
    ├── AdministrativoClient.java      ← já existe
    └── dto/

atendimento/src/main/java/br/edu/imepac/atendimento/
├── atendimento/
├── prontuario/
├── outbox/
│   ├── OutboxEvent.java               ← vem de commons.entities.atendimento
│   ├── OutboxStatus.java              ← vem de commons.entities.atendimento
│   ├── OutboxEventRepository.java     ← vem de commons.repositories.atendimento
│   ├── OutboxEventProcessor.java      ← já existe em atendimento.outbox
│   ├── OutboxScheduler.java           ← já existe em atendimento.outbox
│   └── EventoPermanenteException.java ← já existe em atendimento.outbox
├── anotacao/
├── exame/
└── clients/
```

**O que FICA em commons (lib técnica pura):**
- `commons/config/ModelMapperConfig.java`
- `commons/entities/BaseEntity.java`
- `commons/exceptions/ServicoIndisponivelException.java`
- `commons/exceptions/BusinessException.java` (novo, 0A)
- `commons/exceptions/EntityNotFoundException.java` (novo, 0A)
- `commons/exceptions/IntegrationException.java` (novo, 0A)

**O que SAI de commons (vai pro microsserviço dono):**

| Arquivo | Destino |
|---------|---------|
| `entities/administrativo/*` | `administrativo/<feature>/` |
| `entities/agendamento/*` | `agendamento/consulta/` |
| `entities/atendimento/*` | `atendimento/<feature>/` |
| `repositories/administrativo/*` | `administrativo/<feature>/` |
| `repositories/agendamento/*` | `agendamento/consulta/` |
| `repositories/atendimento/*` | `atendimento/<feature>/` |
| `services/administrativo/*` | `administrativo/<feature>/` |
| `services/agendamento/*` | `agendamento/consulta/` |
| `services/atendimento/*` | `atendimento/<feature>/` |

**Ordem de migração (importa não quebrar tudo de vez):**
1. `administrativo` — menos dependências cross. Migra, atualiza Application, `mvn test`. Commit.
2. `agendamento` — mesmo processo. Commit.
3. `atendimento` — migra. `StatusConsulta` do agendamento: duplicar em `atendimento/clients/dto/` (DDD correto). Commit.

**Atualizar Application de cada serviço:**

Antes (referenciando commons):
```java
@SpringBootApplication(scanBasePackages = {
    "br.edu.imepac.administrativo",
    "br.edu.imepac.commons.config",
    "br.edu.imepac.commons.services.administrativo"
})
@EntityScan(basePackages = "br.edu.imepac.commons.entities.administrativo")
@EnableJpaRepositories(basePackages = "br.edu.imepac.commons.repositories.administrativo")
```

Depois (tudo local):
```java
@SpringBootApplication(scanBasePackages = {
    "br.edu.imepac.administrativo",
    "br.edu.imepac.commons.config"
})
// @EntityScan e @EnableJpaRepositories removidos — Spring Boot detecta automaticamente dentro do pacote base
```

**Mover testes:** `commons/test/services/<dominio>/*` → `<microsservico>/test/<feature>/`

**Esforço:** 1 dia | **Risco:** médio (mecânico mas trabalhoso — IntelliJ refactor ajuda)  
**Testes:** `mvn test` após cada serviço migrado. Newman após PR pronta.

---

## Fase 1 — OpenFeign (dividir com Dev B depois)

**Branch:** `feat/openfeign`  
**Depende de:** Fase 0 mergeada

Substituições:
- `agendamento/clients/AdministrativoClient` RestTemplate → `@FeignClient`
- `atendimento/clients/AgendamentoClient` RestTemplate → `@FeignClient`
- `administrativo/clients/AgendamentoClient` RestTemplate → `@FeignClient`

`FeignErrorDecoder` usa exceptions da Fase 0A.

---

## Fase 2 — API Gateway (dividir com Dev B depois)

**Branch:** `feat/api-gateway`  
**Módulo novo:** `gateway/` (Spring Cloud Gateway, WebFlux — não importa `commons`)

Rotas:
- `/api/admin/**` → `administrativo:8081`
- `/api/agendamentos/**` → `agendamento:8082`
- `/api/atendimentos/**` → `atendimento:8083`

---

## Fase 3 — Keycloak (dividir com Dev B depois)

**Branch:** `feat/keycloak`  
**Depende de:** Fase 2 mergeada

Realm `clinica`, roles `ADMIN/ATENDENTE/MEDICO/GESTOR`, Resource Server em cada microsserviço.

---

## Critérios de "Fase 0 pronta"

- [ ] `commons` tem 0 entities, 0 repositories, 0 services de negócio
- [ ] 3 exceptions tipadas em `commons.exceptions`
- [ ] Cada microsserviço com package-by-feature
- [ ] `@EntityScan`/`@EnableJpaRepositories` removidos das Applications (Spring detecta local)
- [ ] `mvn test` verde em todos os módulos
- [ ] Newman verde (comportamento HTTP idêntico)

---

## Plano B (se apertar prazo)

Ordem de descarte das fases seguintes:
1. Keycloak (Fase 3) — mostra arquitetura sem auth
2. Gateway (Fase 2) — chama serviços direto
3. Feign (Fase 1) — mantém RestTemplate atual

**Nunca cortar Fase 0** — é a fundação que dá clareza ao código.
