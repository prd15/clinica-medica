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

