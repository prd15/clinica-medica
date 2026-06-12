## Resumo

<!-- 1-3 frases sobre o que muda e por que. -->

## Test plan

- [ ] `mvn clean install` no root verde
- [ ] Testes do(s) modulo(s) afetado(s) verdes
- [ ] Postman collection do dominio roda sem regressao
- [ ] Smoke test via Docker stack ok (containers respondem, fluxo principal funciona)
- [ ] Swagger UI sobe sem erro nos endpoints afetados

## Perguntas SOLID

Responder antes de submeter. Aceitavel responder "nao se aplica" desde que justificado em uma frase.

1. **SRP** — Alguma classe nova com mais de uma responsabilidade clara? Por que?
2. **OCP** — Algum `if/else` ou `switch` por tipo que poderia ser polimorfismo?
3. **DIP** — Alguma dependencia concreta onde uma interface valeria? (Aceitavel: "nao vale agora porque so existe uma implementacao e nenhuma outra esta no horizonte".)

## Referencias

- Roadmap SOLID: [`docs/ROADMAP_SOLID.md`](../docs/ROADMAP_SOLID.md)
- Issue/Trello/Discord relacionado:
- Tag de rollback (se for refactor estrutural):
