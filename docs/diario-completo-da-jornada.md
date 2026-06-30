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

