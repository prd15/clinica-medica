# Diário Completo da Jornada — Sistema de Clínica Médica

Este é o relato detalhado de toda a minha caminhada dentro do projeto, do primeiro
dia em que abri o repositório até o estado atual. Escrevi como um diário mesmo:
em ordem, com o que fiz, por que fiz, o que deu errado no caminho e como resolvi.
Serve pra eu lembrar das decisões e pra quem chegar depois entender não só *o que*
existe no código, mas *como* chegou ali.

Projeto: sistema de gestão de clínica médica em microsserviços.
Stack central: Java 17, Spring Boot 3.3.5, Spring Cloud, MySQL, Keycloak, Docker.

---

## Padrões que mantive o tempo todo

Antes de qualquer coisa, alguns padrões que segui em todo o projeto:

- **Conventional Commits.** `tipo(escopo): descrição`, em português, no imperativo,
  pra manter o histórico legível e fácil de revisar.
- **Nada de senha no código.** Segredos sempre por variável de ambiente
  (`${VAR}`), nunca chumbados no `application.properties`.
- **A anatomia do Convênio em tudo.** Cada funcionalidade nova segue as mesmas
  camadas do módulo de referência: Entity → Repository → Service (com testes) →
  Controller + DTOs.

Esses padrões aparecem em absolutamente tudo abaixo, então não vou repetir a
cada passo.

---

## Capítulo 0 — O primeiro contato

Quando abri o repositório pela primeira vez, o esqueleto do sistema já existia: um
projeto Maven multi-módulo com a separação que viraria a espinha dorsal de tudo.

- **`commons`** — biblioteca compartilhada (não é serviço): entidades-base,
  exceções, handler global, configurações comuns.
- **`administrativo`** (porta 8081) — cadastros: convênios, pacientes, médicos,
  especialidades, atendentes, relatórios.
- **`agendamento`** (porta 8082) — consultas e seus status.
- **`atendimento`** (porta 8083) — prontuário, anotações, exames.

A "regra de ouro" do projeto já estava posta: o módulo **Convênio** era o padrão
de referência. Toda funcionalidade nova deveria seguir a mesma anatomia em camadas:

```
Entity  →  Repository  →  Service (com testes)  →  Controller
                                                    ├─ Request DTO (@NotBlank)
                                                    └─ Response DTO
```

E duas regras de arquitetura que eu respeitaria sempre:

1. **Nunca** `@ManyToOne` entre entidades de bancos diferentes — referência é por
   `Long id`. Cada serviço é dono do seu banco e ninguém lê o banco alheio.
2. Comunicação entre serviços **só** por HTTP REST.

Esse foi o terreno. A partir daqui, é a minha caminhada.

---

## Capítulo 1 — Code review e as primeiras melhorias

Meu primeiro trabalho de verdade não foi escrever feature nova — foi *olhar com
seriedade* pro que existia e apertar os parafusos. Saiu daí uma rodada de
melhorias que não eram over-engineering, coisas concretas:

- **`show-sql` desligado por padrão** nos três serviços. Em dev, dá pra ligar por
  variável de ambiente (`SPRING_JPA_SHOW_SQL=true`), mas o padrão é silencioso —
  log limpo.
- **DRY no `AdministrativoClient`** — extraí um helper que estava repetido.
- **`@Transactional(readOnly = true)`** nos métodos de leitura. Sinaliza intenção
  e deixa o provider otimizar (sem flush desnecessário).

Aprendi/reforcei aqui o raciocínio de **logging com SLF4J + Logback**: por que
`log.warn` em vez de `System.out`, por que níveis importam, e como isso depois
escalaria pro projeto inteiro com Logback. Foi a semente do que viraria a
observabilidade lá na frente.

Fechei essa rodada documentando e com o ciclo de review encerrado.

---

## Capítulo 2 — O fluxo atendimento → agendamento: 3 bugs e 3 melhorias

Esse foi o primeiro mergulho num fluxo de negócio de ponta a ponta. A ideia: quando
um atendimento acontece, o agendamento precisa saber que a consulta foi realizada.
Mexendo nisso, achei e consertei **três bugs**:

1. **Endpoint `/realizar` no agendamento** — o caminho pra marcar uma consulta como
   realizada não estava redondo.
2. **Unicidade de `consultaId` no atendimento** — dava pra registrar atendimento
   duplicado pra mesma consulta. Fechei com restrição de unicidade.
3. **Validar a consulta antes de registrar o atendimento** — não dava pra atender
   uma consulta que não existe. Passei a validar antes.

E **três melhorias**:

- `minha-agenda` do médico passou a incluir consultas **CONFIRMADA** (não só
  agendadas).
- Listagem com **filtros combinados**.
- Avaliação de **double-booking** (dois agendamentos no mesmo horário pro mesmo
  médico) — que depois viraria uma trava no próprio banco.

Validei tudo com Postman e build verde antes de fechar. Esse capítulo me deixou
íntimo do agendamento e do atendimento, o que seria essencial pro Outbox.

---

## Capítulo 3 — Caça ao código morto

Pedi e fiz uma varredura por código morto. Achei e removi o que não tinha mais uso
(por exemplo, um `AtendenteService.findByUsuario` órfão). Regra que segui à risca:
**não matar nada que indicasse teste** — testes ficam intocados até pedido
explícito. Testei que a remoção não afetou nada e fechei com commits de `chore`.

---

## Capítulo 4 — C3: cada banco com as suas tabelas

Aqui veio o primeiro problema *arquitetural* sério. O `@EntityScan` estava amplo
demais: o Hibernate enxergava as entidades de todos os domínios e **criava todas as
tabelas em todos os bancos**. O banco do agendamento tinha tabela de convênio, o de
atendimento tinha tabela de paciente — uma bagunça que feria o princípio de "cada
serviço é dono do seu banco".

**O que fiz (C3):**

- Movi entidades, repositórios e services pra subpacotes por domínio
  (`commons.<tipo>.<dominio>`).
- Estreitei o `@EntityScan`, o `@EnableJpaRepositories` e o `scanBasePackages` de
  cada serviço pra enxergar **só** o seu domínio.

**Os perrengues técnicos** (e foram vários):

- Escrevi um script pra mover as classes em lote e ele **falhou no zsh** — o `for c
  in $3` não fazia word-splitting como eu esperava. Reescrevi em bash.
- O bash 3.2 do macOS **não tem** `declare -A` (array associativo) nem nameref.
  Tive que reescrever com variáveis simples + `eval`.
- O `set -e` deixou um **refactor parcial** no meio do caminho (umas classes
  movidas, outras não). Limpei com `git reset --hard` + `git clean -fd` e refiz do
  zero, com calma.
- Mover os services quebrou testes que dependiam de **acesso de mesmo pacote**.
  Movi os testes pra espelhar os subpacotes novos.
- Um detalhe que quase me pegou: `mvn | tail` retorna o exit do `tail`, **não** do
  Maven. Quase declarei verde um build que tinha falhado. Passei a checar o
  resultado de verdade.

**Validação:** `SHOW TABLES` em cada banco confirmou — cada um só com as tabelas do
seu domínio. Build e boot limpos. Fechei C3 com PR.

---

## Capítulo 5 — C4: o padrão Outbox (nada se perde)

Esse é, tecnicamente, o capítulo do qual mais me orgulho. O problema: quando o
atendimento avisa o agendamento que a consulta foi realizada, **e se a chamada
falhar?** A consulta foi atendida mas o agendamento nunca soube. Informação perdida.

A solução é o **padrão Outbox (caixa de saída transacional)**:

- No mesmo instante (e na **mesma transação**) em que o atendimento é salvo, gravo
  um registro de "aviso a entregar" numa tabela `outbox_event`. Ou os dois
  acontecem, ou nenhum — atomicidade garantida pelo banco.
- Um **scheduler** roda em segundo plano, lê os eventos pendentes e tenta entregar.
  Se falhar, marca como FALHA e **tenta de novo** depois (retry configurável).

**O que construí (C4):**

- `OutboxEvent` + `OutboxStatus` (PENDENTE / PROCESSADO / FALHA / DESCARTADO) +
  repositório.
- O `registrar()` do atendimento enfileira o evento na mesma transação.
- `OutboxScheduler` com retry configurável.
- Mais tarde isso ganharia housekeeping (limpar eventos terminais antigos) e lock
  pessimista com SKIP LOCKED pra rodar em múltiplas instâncias sem entrega dupla.

**Validação end-to-end** (provada com scripts bash):

- *Caminho feliz:* POST atendimento → consulta vira REALIZADA em ~7s →
  `outbox_event` em PROCESSADO.
- *Falha + retry:* derrubei o agendamento de propósito → o evento foi pra FALHA
  com tentativas=1 → religuei o agendamento → na próxima volta do scheduler virou
  PROCESSADO. **O evento não se perdeu.** Esse era o ponto.

Registrei no `BACKLOG.md` o que ficou de fora de propósito (backoff exponencial,
circuit breaker, etc.) e documentei tudo no diário.

---

## Capítulo 6 — A descoberta: Keycloak já estava pronto

Num dado momento fui verificar se a segurança/Keycloak já tinha sido implementada —
e descobri que **sim**, estava tudo na branch `development`: o realm `clinica`, o
OAuth2 Resource Server nos serviços, os papéis ADMIN/ATENDENTE/MEDICO/SERVICE, o
`client_credentials` pra comunicação serviço-a-serviço. O time tinha avançado em
paralelo com Gateway (Spring Cloud Gateway) e OpenFeign (substituindo RestTemplate).

Aqui a história deixou de ser "o meu canto do código" e virou "entrar no fluxo do
time". Decidimos trabalhar a partir do `development`, que já era a versão
arquiteturalmente mais avançada.

**Trazer tudo pra rodar localmente:**

- Atualizei meu checkout pro `development` e recarreguei o IntelliJ.
- Subi a stack completa no Docker — 8 containers (3 bancos + Keycloak + 4 serviços).
- Fiz um **smoke test** ponta a ponta provando a segurança: 401 sem token, 200 com
  token de admin, 403 atendente tentando criar médico, 201 admin criando convênio.
  A defesa em profundidade funcionava — até o acesso direto ao microsserviço
  (sem passar pelo gateway) validava o JWT.

**Perrengues recorrentes da infra** (que me acompanhariam até o fim):

- O **OrbStack** (daemon do Docker) caía sozinho com frequência. Religar com
  `open -a OrbStack` virou reflexo.
- A **rede da faculdade** fazia interceptação de TLS e quebrava o `gh` (GitHub
  CLI). Resolvi reautenticando com token clássico quando precisei.

---

## Capítulo 7 — SOLID: do conceito ao roadmap

O pedido seguinte foi diferente: olhar o projeto com seriedade e avaliar se valia
aplicar um **design pattern bem definido** — e SOLID. Expliquei os cinco princípios
no contexto real do projeto (não na teoria), apontando onde o código já acertava e
onde doía:

- **SRP** — a separação por domínio já era SRP em escala de arquitetura.
- **OCP** — o `OutboxEventProcessor` tratava um único tipo de evento; o segundo tipo
  ia pedir um `switch`. Candidato número 1.
- **LSP / ISP** — pouco aplicáveis no estado atual; não forcei.
- **DIP** — o `ServiceTokenProvider` era classe concreta acoplada ao Keycloak,
  repetida nos três serviços. Candidato número 2.

Disso saiu o **Roadmap SOLID**: um plano pragmático, em fases, com a postura de
"aplicar onde a dor é real, não inventar abstração por abstração". Documentei tudo
num cofre Obsidian separado (`clinica-medica-solid-roadmap`) com o roadmap, o guia
de execução passo a passo, o plano de rollback e uma pasta de evidências.

Antes de mexer em qualquer código, criei uma **tag de rollback**
(`snapshot-pre-solid-fase1`) no origin — um ponto seguro pra voltar com um comando
se algo quebrasse.

Detalhe importante: auditei o roadmap **contra o código real** antes de executar e
achei várias premissas erradas (nomes de método ilustrativos que não existiam,
`OutboxEventProcessorTest` que eu achava que ia criar mas já existia, o adapter
`AgendamentoClient` que eu não tinha considerado). Corrigi o roadmap antes de
começar. Isso evitou executar em cima de suposição.

---

## Capítulo 8 — Fase 1 SOLID, Tarefa B: OCP no Outbox

Primeira execução. Branch `refactor/ocp-outbox-handlers`, escopo só no atendimento.
A ideia: o processor parar de decidir o que fazer com `if (eventType == X)` e passar
a **rotear por handler**.

Fiz em commits atômicos, cada um com teste verde antes do próximo:

1. `feat(outbox): cria interface OutboxEventHandler` — interface package-private
   (alinhada com o processor, que já era package-private de propósito).
2. `feat(outbox): cria ConfirmacaoRealizacaoHandler` — a lógica do único tipo atual,
   dependendo do adapter `AgendamentoClient` (que já traduz 404/409 em
   `EventoPermanenteException`).
3. `refactor(outbox): OutboxEventProcessor roteia via Map<eventType, Handler>` —
   monta um mapa dos handlers no construtor e roteia. Tipo desconhecido vira
   `EventoPermanenteException` → DESCARTADO. **Preservei** o try/catch que separa
   erro permanente (descarta sem retry) de transitório (incrementa tentativas).
4. Ajustei o `OutboxEventProcessorTest` (que já existia) pra mockar o handler.
5. Criei o `ConfirmacaoRealizacaoHandlerTest`.

Um commit do plano (ajustar o `OutboxSchedulerTest`) eu **pulei conscientemente** —
o teste já estava alinhado com a arquitetura nova, não precisava mexer. Anotei a
decisão em vez de inventar commit cosmético.

**Provei o OCP de verdade na stack:** inseri um evento fake apontando pra uma
consulta inexistente (id 999) → o scheduler pegou → o handler chamou o agendamento →
404 → virou `EventoPermanenteException` → DESCARTADO, tentativas=0. O log do
processor confirmou. O caminho feliz também: evento real → PROCESSADO em ~10s.

40 testes verdes no atendimento. PR #36. Mergeado.

---

## Capítulo 9 — Fase 1 SOLID, Tarefa A: DIP no token provider

Branch `refactor/dip-service-token-provider`, escopo nos três serviços. O
`ServiceTokenProvider` (classe concreta, acoplada ao Keycloak, repetida 3x) virou
uma **interface**, com `KeycloakServiceTokenProvider` como implementação,
selecionada por `@ConditionalOnProperty(auth.provider=keycloak, matchIfMissing=true)`.

Fiz 3 commits por módulo (10 no total, com o de docs), sempre o mesmo padrão:

1. Renomeia a classe concreta pra `KeycloakServiceTokenProvider`.
2. Cria a interface + a classe passa a implementá-la + `@ConditionalOnProperty`.
3. O `FeignConfig` passa a depender da interface, não da classe.

Três commits por módulo foi de propósito: permite **revert cirúrgico** se um módulo
específico der problema. Documentei a flag `auth.provider` no `.env.example`.

Decisão de divergência do plano: o método ficou `getToken()` (o nome real que já
existia), não `obterToken()` (que o roadmap usava como ilustrativo). Zero churn em
teste.

**Validação:** rebuild dos 3 serviços, zero `NoSuchBeanDefinitionException` nos logs
(o Spring resolveu a interface pra implementação certa), e regressão de Postman:
**96 requests, 153 asserts, 0 falhas** em 7 collections. PR #37. Mergeado.

---

