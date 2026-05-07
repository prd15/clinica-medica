# /status-projeto

Mostra o estado atual de implementacao do projeto.

## O que fazer
1. Leia o `CLAUDE.md` para entender o projeto
2. Varra todos os arquivos `.java` nos modulos
3. Varra o `docs/CHANGELOG_AGENTES.md` se existir
4. Monte um relatorio com:

```markdown
# Status do Projeto — Clinica Medica

## commons
| Modulo | Entity | Repository | Service | Testes |
|--------|--------|------------|---------|--------|
| Convenio | done | done | done | done |
| Paciente | ? | ? | ? | ? |
...

## administrativo (porta 8081)
| Modulo | Controller | DTOs | Rotas |
|--------|------------|------|-------|
| Convenio | done | done | GET,POST,PUT,DELETE |
...

## agendamento (porta 8082)
...

## atendimento (porta 8083)
...

## Proxima task recomendada
[sugira o proximo passo logico]
```
