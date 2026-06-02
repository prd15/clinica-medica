# Roadmap SOLID — Clinica Medica

Documento de plano para o grupo. Define o que vamos refatorar para aplicar SOLID,
em que ordem, com referências aos arquivos reais do projeto. Não é manifesto
teórico, é roteiro de trabalho.

---

## 1. Postura

SOLID é ferramenta de design, não nota de prova. A regra é aplicar onde a dor
for real (regra nova quebra três arquivos, classe inflou pra 400 linhas, teste de
um caso quebra outro). Inventar abstração só pra ter abstração reproduz exatamente
o problema que SOLID tenta resolver: indireção demais e ninguém sabe onde a regra
mora.

Postura assumida neste roadmap: pragmática. Não vamos reescrever Convenio nem
Paciente — eles já estão bem segmentados na arquitetura atual
(`commons/entities/<dominio>` + `commons/repositories/<dominio>` +
`commons/services/<dominio>`). Vamos atacar dois pontos onde o crescimento
previsto vai doer e deixar o resto reativo.

---

## 2. Estado atual: o que o projeto já entrega bem

Antes do plano, vale reconhecer o que não precisa mexer:

- A separação por domínio em `commons/` já isola responsabilidades em nível de
  pacote. Cada serviço tem um motivo de mudar (SRP em escala arquitetural).
- O Outbox já delega: `OutboxScheduler` drena, `OutboxEventProcessor` processa um
  evento, `OutboxEventRepository` consulta. Três classes, três motivos de mudar.
- Os Feign clients são pequenos (`AgendamentoConfirmacaoClient` no atendimento,
  `AdministrativoClient` no agendamento). Nada de cliente único com 30 métodos
  (ISP na prática).
- Spring praticamente força DIP via `@Autowired` em interfaces
  (`ConvenioRepository`, `OutboxEventRepository`).

A intuição "o projeto está SOLID" não é só sorte. A arquitetura de microsserviços
+ commons + repositórios JPA já empurra metade do trabalho.

---

## 3. Onde dói (ou vai doer) e precisa de plano

Dois pontos:

1. O `ServiceTokenProvider` é classe concreta acoplada ao Keycloak e existe três
   vezes (uma cópia em cada microsserviço). Trocar provedor de autenticação =
   reescrever três classes.
2. O `OutboxEventProcessor` hoje processa um único `eventType`. Quando o segundo
   tipo aparecer (cancelamento, reagendamento, lembrete 24h), o reflexo errado
   é meter `switch` dentro do processor.

Os dois são acionáveis agora, custo baixo, ganho garantido.

---

## 4. Fase 1 — Refatorações que valem agora

### 4.1 ServiceTokenProvider vira abstração (DIP)

**Dor.** A classe `ServiceTokenProvider` mora em três lugares:

- `atendimento/src/main/java/br/edu/imepac/atendimento/integration/agendamento/ServiceTokenProvider.java`
- `agendamento/src/main/java/br/edu/imepac/agendamento/integration/administrativo/ServiceTokenProvider.java`
- `administrativo/src/main/java/br/edu/imepac/administrativo/integration/agendamento/ServiceTokenProvider.java`

Cada cópia sabe da URL do Keycloak, do `client_id` e do `client_secret`. No dia
em que migrar pra Auth0, Okta ou um provedor próprio, são três rewrites em três
módulos, sem teste de regressão de provedor.

**Estado atual do pacote de integração (mesmo padrão nos três módulos):**

```
integration/<servico-alvo>/
├── *Client.java                    (interface Feign)
├── *FeignConfig.java               (RequestInterceptor que injeta o token)
└── ServiceTokenProvider.java       (classe concreta acoplada ao Keycloak)
```

As três cópias são quase idênticas. Mudam só `client_id`, escopo do token, e
qual serviço cada uma chama. Toda a lógica de fetch + cache + refresh é igual.

**Passos exatos pra refatorar (por módulo):**

1. **Renomear** a classe atual para `KeycloakServiceTokenProvider`. No IntelliJ,
   `Shift+F6` em cima do nome — todos os usos e imports são atualizados.
2. **Criar interface** no mesmo pacote, com a assinatura pública atual:
   ```java
   public interface ServiceTokenProvider {
       String obterToken();
   }
   ```
3. **Marcar a classe renomeada** como implementação ativa do Keycloak:
   ```java
   @Component
   @ConditionalOnProperty(
           name = "auth.provider",
           havingValue = "keycloak",
           matchIfMissing = true)
   class KeycloakServiceTokenProvider implements ServiceTokenProvider {
       // corpo atual da classe sem alteração
   }
   ```
4. **Atualizar o `RequestInterceptor`** (no `*FeignConfig`) pra depender da
   interface, não da classe concreta:
   ```java
   private final ServiceTokenProvider tokenProvider; // antes: KeycloakServiceTokenProvider
   ```
   Spring resolve sozinho pela única implementação ativa.
5. **Rodar `mvn test`** no módulo. Os testes do client devem continuar verdes
   (Mockito mocka interface igual a classe concreta).
6. **Documentar** a nova flag `auth.provider` no `.env.example`.

**Ordem entre módulos.**

Recomendado fazer um módulo de cada vez, na ordem `atendimento → agendamento →
administrativo`. Cada módulo é um commit atômico:

- `refactor(integration): extrai ServiceTokenProvider como interface no atendimento`
- `refactor(integration): extrai ServiceTokenProvider como interface no agendamento`
- `refactor(integration): extrai ServiceTokenProvider como interface no administrativo`
- `docs(env-example): documenta auth.provider`

**Esforço.** ~1h por módulo, ~3h somando, mais revisão de PR.

**Risco.** Baixo. Refatoração puramente estrutural, coberta pelos testes que já
existem.

**Ganho no dia D.** Trocar de provedor passa a ser "escreve
`Auth0ServiceTokenProvider`, ajusta `auth.provider=auth0` no `.env`, deploy".
Zero modificação no Feign interceptor, zero modificação nos services.

### 4.2 Outbox com handlers por eventType (OCP)

**Dor.** O `OutboxEventProcessor`
(`atendimento/src/main/java/br/edu/imepac/atendimento/outbox/`) hoje trata só
`CONFIRMACAO_REALIZACAO`. O campo `eventType` no `OutboxEvent` existe pra suportar
vários tipos, mas a forma errada de implementar o próximo é abrir o processor e
meter `switch (evento.getEventType())`. Cada tipo novo reabre o mesmo arquivo,
cada teste do processor cresce, cada bug fixado num caso arrisca quebrar os
outros.

**Plano.**

```java
public interface OutboxEventHandler {
    String eventType();
    void handle(OutboxEvent evento);
}

@Component
class ConfirmacaoRealizacaoHandler implements OutboxEventHandler {
    private final AgendamentoConfirmacaoClient client;

    @Override
    public String eventType() {
        return "CONFIRMACAO_REALIZACAO";
    }

    @Override
    public void handle(OutboxEvent evento) {
        // toda a lógica atual do processor que chama o Feign client
    }
}
```

O processor fica fino, só roteando:

```java
@Component
public class OutboxEventProcessor {

    private final Map<String, OutboxEventHandler> handlersPorTipo;
    private final OutboxEventRepository repository;

    public OutboxEventProcessor(List<OutboxEventHandler> handlers,
                                OutboxEventRepository repository) {
        this.handlersPorTipo = handlers.stream()
                .collect(Collectors.toUnmodifiableMap(
                        OutboxEventHandler::eventType, h -> h));
        this.repository = repository;
    }

    public void processar(OutboxEvent evento) {
        OutboxEventHandler handler = handlersPorTipo.get(evento.getEventType());
        if (handler == null) {
            // ajustar nomes aos métodos reais do OutboxEvent
            evento.descartar("eventType nao registrado: " + evento.getEventType());
            repository.save(evento);
            return;
        }
        try {
            handler.handle(evento);
            evento.marcarProcessado();
        } catch (Exception e) {
            evento.registrarFalha(e.getMessage());
        }
        repository.save(evento);
    }
}
```

Os nomes `descartar`, `marcarProcessado` e `registrarFalha` são ilustrativos.
Ajustar ao contrato real do `OutboxEvent` (que já tem o `OutboxEvent.pendente(...)`
como factory).

**Testes a atualizar/criar:**

- `OutboxSchedulerTest`: continua igual, só verifica delegação.
- `OutboxEventProcessorTest`: testa o roteamento (handler conhecido, handler
  ausente, handler que lança exceção).
- `ConfirmacaoRealizacaoHandlerTest`: testa a integração com o Feign client
  mockado.

**Esforço.** ~3-4h.

**Risco.** Baixo. A cobertura existente do scheduler protege a maior parte do
caminho.

**Ganho no dia D.** Quando `CANCELAMENTO` ou `REAGENDAMENTO` aparecer, é uma
classe nova, um teste novo, zero alteração no processor ou no scheduler.

---

## 5. Fase 2 — Reativo, faz quando a dor aparecer

Não dá pra cravar quando isso vai acontecer. Os gatilhos:

### 5.1 Validators de service (OCP + SRP)

**Gatilho.** Método de service com 4+ `if` validando regras de negócio, ou a
mesma validação repetida em dois services diferentes.

```java
public interface Validator<T> {
    void validar(T request);
}

@Component
class CrmObrigatorioValidator implements Validator<MedicoRequest> {
    @Override
    public void validar(MedicoRequest req) {
        if (req.crm() == null || req.crm().isBlank()) {
            throw new ValidacaoException("CRM e obrigatorio");
        }
    }
}
```

O service injeta `List<Validator<MedicoRequest>> validators` e roda em sequência.
Validator novo = classe nova, zero alteração no service.

**Quando NÃO fazer.** Service com duas validações triviais. `if` é mais legível e
mais fácil de debugar.

### 5.2 Quebra de service gordo (SRP + ISP)

**Gatilho.** Service com 12+ métodos públicos onde controllers diferentes usam
subconjuntos sem sobreposição.

Padrão sugerido (CQRS-lite):

- `ConsultaCommandService` — `criar`, `confirmar`, `cancelar`, `realizar`
- `ConsultaQueryService` — `listar`, `buscarPorId`, `agendaDoMedico`
- `ConsultaMaintenanceService` — `purgarAntigas`, recálculos batch

Não fazer preventivamente. Hoje o `ConsultaService` tem 6-8 métodos, não dói. No
dia em que passar dos 12, conversar antes de mergear.

### 5.3 Strategy para regras de negócio com variação (OCP)

**Gatilho.** Regra varia por contexto e não cai bem em `if`.

Casos prováveis no domínio:

- Cálculo de valor de consulta por convênio.
- Política de cancelamento por tipo (eletiva vs. urgência).
- Política de retry diferente por tipo de evento do outbox.

Mesmo padrão do `OutboxEventHandler`: interface + implementações + roteamento por
chave.

---

## 6. Fase 3 — Higiene contínua

Sem entregável, é cultura.

### 6.1 Três perguntas no template de PR

Adicionar no PR description:

1. Alguma classe nova com mais de uma responsabilidade clara? Por quê?
2. Algum `if/else` por tipo que poderia ser polimorfismo?
3. Alguma dependência concreta onde uma interface valeria? (Aceitável responder
   "não vale agora", desde que documentado.)

Não é gate de CI. É espelho. Força o autor a pensar antes da revisão.

### 6.2 Sinais de alerta em revisão

Sem virar burocracia automatizada, atenção em revisão para:

- Service com mais de 200 linhas — autor justifica.
- Classe com 5+ dependências injetadas — cheiro de SRP violado.
- Método com mais de 50 linhas — quebrar antes de mergear.

---

## 7. Anti-padrões a evitar neste roadmap

Algumas armadilhas que parecem SOLID mas não são:

A primeira, e a mais comum, é **interface só pra ter interface**.
`IConvenioService` + `ConvenioServiceImpl` quando só existe uma implementação e
nunca vai existir outra. Não traz testabilidade nova (Mockito mocka classes
concretas sem problema), só polui o pacote. Se tem mais de uma implementação
real, ou se a injeção é em código fora do seu controle, aí vale.

Segunda: **hierarquia precoce**. Criar `BaseUsuarioEntity` pra três entidades com
dois campos em comum. Composição via `@Embeddable` resolve melhor, evita o
problema de LSP no futuro e não força ninguém a herdar comportamento que não
quer.

Terceira: **factory pra tudo**. Em CRUD normal, `new ConvenioRequest()` no
controller não precisa virar `ConvenioRequestFactory`. Factory faz sentido quando
a criação tem regra (gerar UUID, popular auditoria automática) que se repete em
vários lugares.

Quarta: **evento pra tudo**. O outbox existe pra desacoplar microsserviços.
Dentro do mesmo microsserviço, chamada direta de service é mais simples e mais
debugável. Se alguém propor "vou disparar um evento e o outro service do mesmo
módulo escuta", segura.

---

## 8. Checklist de PR para refactor SOLID

Para cada PR de refatoração:

- [ ] `mvn clean install` passa na raiz
- [ ] Testes existentes continuam verdes
- [ ] Cobertura do código novo igual ou maior que a do código removido
- [ ] Postman collections do domínio afetado rodam sem regressão
- [ ] Swagger UI continua subindo e respondendo
- [ ] Commits atômicos no padrão `tipo(escopo): descricao`
  - ex: `refactor(integration): extrai ServiceTokenProvider para interface`
- [ ] Diário de desenvolvimento no Obsidian atualizado
- [ ] PR description responde as três perguntas da seção 6.1

---

## 9. Divisão sugerida no grupo

Pra evitar conflito de merge, as tarefas da fase 1 podem ir em paralelo:

| Tarefa                          | Dono     | Branch                                   | Módulos                                          |
|---------------------------------|----------|------------------------------------------|--------------------------------------------------|
| 4.1 ServiceTokenProvider (DIP)  | 1 pessoa | `refactor/dip-service-token-provider`    | administrativo + agendamento + atendimento       |
| 4.2 Outbox handlers (OCP)       | 1 pessoa | `refactor/ocp-outbox-handlers`           | atendimento                                      |

Como 4.1 toca os três microsserviços e 4.2 toca só atendimento, dá pra mergear
na ordem: primeiro 4.2 (escopo menor, risco menor), depois 4.1 (mais arquivos,
mais revisão).

Tarefas da fase 2 são reativas. Quando aparecer o gatilho, quem está
implementando a feature traz o refactor no mesmo PR ou em PR antecessor.

---

## 10. Métricas de sucesso

Como saber daqui a três sprints se valeu:

- Quando o segundo tipo de evento de outbox entrar, foi 1 classe nova sem mexer
  no processor? Marcou ponto.
- Quando alguém perguntar "como troco de Keycloak pra X?", a resposta foi
  "escreve uma classe que implementa `ServiceTokenProvider`"? Marcou ponto.
- Se daqui a três meses ninguém usou a interface `ServiceTokenProvider` pra
  plugar outro provedor e a `KeycloakServiceTokenProvider` continua sendo a
  única implementação, a interface ainda assim valeu pelo desacoplamento ou foi
  cerimônia? Conversa honesta no retrô. Não tem vergonha em reverter.

---

## 11. Ordem de execução resumida

1. Grupo lê este documento, sinaliza concordância ou contraproposta.
2. Define donos das tarefas 4.1 e 4.2 na próxima daily.
3. Cria as duas branches em paralelo.
4. Mergeia 4.2 primeiro (menor escopo).
5. Mergeia 4.1 depois (toca três módulos).
6. Atualiza template de PR com as três perguntas da seção 6.1.
7. Fase 2 fica documentada como gatilho-reativo. Não tem deadline.

---

## 12. Referências

- Robert C. Martin, *Clean Architecture*, capítulos 7 a 11. Cobre SOLID com
  exemplos.
- Robert C. Martin, *Design Principles and Design Patterns* (2000). PDF aberto.
  Trinta páginas, suficiente como leitura de grupo.
- Eric Evans, *Domain-Driven Design*, parte II. Pra quando o grupo quiser ir
  além de SOLID em direção a modelagem rica de domínio.

---

## 13. Glossário rápido

- **SRP** (Single Responsibility): uma classe, um motivo de mudar.
- **OCP** (Open/Closed): aberto pra extensão, fechado pra modificação.
- **LSP** (Liskov Substitution): subtipo substitui o tipo base sem quebrar
  contrato.
- **ISP** (Interface Segregation): clientes não dependem de método que não usam.
- **DIP** (Dependency Inversion): depende de abstração, não de implementação
  concreta.
- **YAGNI** (You Aren't Gonna Need It): não cria abstração antes de precisar.
- **CQRS** (Command Query Responsibility Segregation): separa quem lê de quem
  escreve.

---

*Documento vivo. Revisar ao fim de cada fase. Não é tablet de pedra.*
