# Backlog — itens fora do escopo desta entrega (C3 + C4)

Pontos identificados durante a varredura que merecem virar trabalho proprio, mas nao foram resolvidos nesta sessao para manter o foco.

## Confiabilidade do outbox

- **Coordenacao entre instancias do scheduler.** Hoje o `OutboxScheduler` e seguro para uma instancia (o `@Scheduled` do Spring nao reentra em si mesmo). Em multi-instancia, duas instancias podem ler o mesmo evento. Solucao: pessimistic lock com `SELECT ... FOR UPDATE SKIP LOCKED` (MySQL 8+) na query do scheduler.
- **Transacao por evento em vez de por lote.** O `processarPendentes()` esta em uma unica `@Transactional` para o batch. Um lote grande ou um evento lento mantem a transacao aberta por mais tempo. Avaliar processar cada evento em sua propria transacao (e.g., metodo separado anotado `@Transactional` chamado via self-injection ou via uma classe auxiliar).
- **Backoff exponencial no retry.** O retry hoje e imediato (a cada poll). Adicionar `proxima_tentativa_em` na entidade e respeitar para nao martelar um servico que esta caindo.

## Postman / collections

- **Collection de regressao `atendimento-notificacao` precisa de delay.** Com o fluxo agora assincrono, a assercao "consulta virou REALIZADA" depende do scheduler ter rodado. Rodar com `newman ... --delay-request 12000`, ou converter o `GET /v1/consultas/{id}` em um pre-request assincrono que espera ate ver o status. End-to-end e validado via script bash em `/tmp/c4_e2e.sh` e `/tmp/c4_falha.sh` durante a entrega.
- **Caso de retry no Postman.** Adicionar request que simula o agendamento offline e valida que o evento fica em FALHA com tentativas > 0, e em PROCESSADO apos o servico voltar — hoje validado por script bash.

## Resiliencia da comunicacao

- **Sem circuit breaker.** Os `RestTemplate` clients tem timeout mas nao tem circuit breaker. Em uma onda de falhas, o atendimento bloqueia threads tentando o agendamento. Avaliar Resilience4j.

## Seguranca

- **BCrypt nas senhas.** `MedicoEntity.senha` e `AtendenteEntity.senha` em plaintext (sabido — fora do escopo academico). Adotar quando seguranca virar requisito.

## Decisao registrada (NAO virou backlog — feita de proposito)

- **Outbox sem Flyway.** O projeto usa `ddl-auto=update` para todo o schema. Introduzir Flyway so para a tabela `outbox_event` criaria um hibrido inconsistente. A tabela e criada pelo Hibernate como as demais. Migrar todo o schema para Flyway seria um trabalho proprio, nao um efeito colateral do C4.
