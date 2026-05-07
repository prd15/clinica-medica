# /implementar-modulo

Implementa um modulo completo seguindo o padrao do projeto.

## Como usar
/implementar-modulo [NomeDoModulo] [microsservico]

Exemplos:
- /implementar-modulo Paciente administrativo
- /implementar-modulo Consulta agendamento
- /implementar-modulo Atendimento atendimento

## O que este comando faz
1. Usa @agent-backend-dev para implementar Entity + Repository + Service + Testes + Controller + DTOs
2. Usa @agent-code-reviewer para revisar o codigo
3. Usa @agent-doc-writer para documentar no Obsidian
4. Apresenta resumo do que foi feito

## Fluxo de execucao

### Passo 1 — Planejamento (AGUARDA SUA APROVACAO)
Antes de escrever qualquer codigo, mostre:
- Arquivos que serao criados (com caminho completo)
- Campos da entidade
- Rotas que serao expostas
- Dependencias necessarias

Pergunte: "Posso prosseguir com esta implementacao? (s/n)"
SO AVANCE apos confirmacao explicita.

### Passo 2 — Implementacao
Use @agent-backend-dev para criar todos os arquivos.

### Passo 3 — Revisao
Use @agent-code-reviewer para revisar o que foi criado.
Se houver problemas ALTO, corrija antes de continuar.

### Passo 4 — Documentacao
Use @agent-doc-writer para documentar no Obsidian.

### Passo 5 — Resumo final
Mostre:
- Arquivos criados
- Rotas expostas
- Status da revisao
- Documentacao criada
- Proxima task sugerida
