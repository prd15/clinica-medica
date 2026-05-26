# Bugs para Correcao

Arquivo destinado ao agente responsavel por corrigir os problemas encontrados na rodada de validacao.

## Contexto

Projeto: `clinica-medica`

Ambiente em que os bugs foram reproduzidos:

- Aplicacao rodando via `docker-compose up -d --build`.
- Servicos:
  - `administrativo`: `http://localhost:8081`
  - `agendamento`: `http://localhost:8082`
  - `atendimento`: `http://localhost:8083`
- Health checks dos tres servicos estavam `UP`.
- Testes Maven e colecoes Newman passaram; os bugs abaixo sao pontos de robustez/contrato encontrados fora do caminho feliz principal.

## BUG-001 - URL malformada em consultas retorna 500

Severidade: Media

Modulo: `agendamento`

Endpoint:

```http
PATCH /v1/consultas//realizar
```

### Como reproduzir

Com o servico `agendamento` rodando:

```bash
curl.exe -i -X PATCH http://localhost:8082/v1/consultas//realizar
```

### Resultado atual

Retorna `500 Internal Server Error`.

Resposta observada:

```json
{
  "timestamp": "2026-05-26T18:42:50.134245291",
  "status": 500,
  "error": "Internal Server Error",
  "message": "Erro interno do servidor"
}
```

Log observado no container `agendamento`:

```text
NoResourceFoundException: No static resource v1/consultas/realizar.
```

### Resultado esperado

Uma URL sem ID valido nao deve virar erro interno.

Retorno esperado:

- `400 Bad Request`, se a aplicacao tratar como parametro ausente/invalido.
- ou `404 Not Found`, se a aplicacao tratar como rota inexistente.

O importante e nao retornar `500`.

### Hipotese tecnica

O Spring esta tratando `/v1/consultas//realizar` como tentativa de resolver recurso estatico ou rota inexistente. A excecao `NoResourceFoundException` cai no handler generico do `GlobalExceptionHandler` e vira `500`.

Arquivos provaveis:

- `agendamento/src/main/java/br/edu/imepac/agendamento/controllers/GlobalExceptionHandler.java`
- `agendamento/src/main/java/br/edu/imepac/agendamento/controllers/ConsultaController.java`

### Sugestao de correcao

Adicionar tratamento explicito para `NoResourceFoundException` e/ou excecao equivalente de rota inexistente no `GlobalExceptionHandler`, retornando `404`.

Exemplo conceitual:

```java
@ExceptionHandler(NoResourceFoundException.class)
public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex) {
    return buildError(HttpStatus.NOT_FOUND, "Rota nao encontrada");
}
```

Tambem verificar se `NoHandlerFoundException` precisa ser tratado dependendo da configuracao do Spring.

### Criterio de aceite

Comando:

```bash
curl.exe -i -X PATCH http://localhost:8082/v1/consultas//realizar
```

Deve retornar `400` ou `404`, nunca `500`.

Nao deve quebrar:

- `PATCH /v1/consultas/{id}/realizar`
- `PATCH /v1/consultas/{id}/confirmar`
- `PATCH /v1/consultas/{id}/reagendar`
- colecao `docs/consulta-collection.json`

## BUG-002 - CNPJ longo em convenio chega ao banco e retorna 409 generico

Severidade: Baixa

Modulo: `administrativo`

Endpoint:

```http
POST /v1/convenios
```

### Como reproduzir

Com o servico `administrativo` rodando, enviar payload com `cnpj` maior que o tamanho aceito pela coluna:

```bash
curl.exe -i -X POST http://localhost:8081/v1/convenios ^
  -H "Content-Type: application/json" ^
  -d "{\"nome\":\"Convenio CNPJ Longo QA\",\"descricao\":\"Teste tamanho\",\"cnpj\":\"CNPJ-LONGO-DEMAIS-202605261542\",\"telefone\":\"34999990000\",\"ativo\":true}"
```

### Resultado atual

A entrada chega ate o banco e gera erro de truncamento.

Resposta observada em reproducao com payload equivalente:

```json
{
  "timestamp": "2026-05-26T18:43:02.730585833",
  "status": 409,
  "error": "Conflict",
  "message": "Violacao de integridade: registro duplicado ou referencia invalida"
}
```

Log observado no container `administrativo`:

```text
SQL Error: 1406, SQLState: 22001
Data truncation: Data too long for column 'cnpj' at row 1
```

### Resultado esperado

Payload invalido deve ser barrado na camada de validacao antes de chegar ao banco.

Retorno esperado:

- `400 Bad Request`
- Mensagem clara indicando que `cnpj` tem tamanho/formato invalido.

### Hipotese tecnica

`ConvenioRequest` valida `cnpj` apenas como obrigatorio, mas nao valida tamanho nem formato. O erro e capturado depois como violacao de integridade, gerando `409` generico.

Arquivos provaveis:

- `administrativo/src/main/java/br/edu/imepac/administrativo/dtos/ConvenioRequest.java`
- `administrativo/src/main/java/br/edu/imepac/administrativo/convenio/ConvenioEntity.java`
- `administrativo/src/main/java/br/edu/imepac/administrativo/controllers/GlobalExceptionHandler.java`
- `administrativo/src/test/java/br/edu/imepac/administrativo/convenio/ConvenioServiceTest.java`

### Sugestao de correcao

Adicionar validacao no DTO de entrada.

Exemplo conceitual:

```java
@NotBlank(message = "CNPJ e obrigatorio")
@Size(max = 18, message = "CNPJ deve ter no maximo 18 caracteres")
@Pattern(
    regexp = "\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}|\\d{14}",
    message = "CNPJ invalido"
)
private String cnpj;
```

Confirmar primeiro o tamanho real da coluna e o formato aceito pelo dominio. Se o projeto ja aceita CNPJ livre, pelo menos aplicar `@Size(max = <tamanho_da_coluna>)`.

### Criterio de aceite

Payload com `cnpj` longo demais deve retornar `400`.

Payloads validos devem continuar funcionando:

- CNPJ formatado: `12.345.678/0001-90`
- CNPJ numerico: `12345678000190`, se esse formato for aceito pelo dominio.

Nao deve quebrar:

- `docs/convenio-collection.json`
- `docs/paciente-collection.json`
- `docs/consulta-collection.json`
- `docs/atendimento-collection.json`
- `docs/relatorios-collection.json`

## Validacao recomendada apos correcao

Executar:

```bash
C:\Users\pedro\Tools\apache-maven-3.9.16\bin\mvn.cmd clean test
docker-compose up -d --build
npx -y newman run docs/convenio-collection.json -e docs/local.postman_environment.json --reporters cli,json --reporter-json-export target/newman/convenio.json
npx -y newman run docs/consulta-collection.json -e docs/local.postman_environment.json --reporters cli,json --reporter-json-export target/newman/consulta.json
```

Testes manuais obrigatorios:

```bash
curl.exe -i -X PATCH http://localhost:8082/v1/consultas//realizar
curl.exe -i -X PATCH http://localhost:8082/v1/consultas/abc/realizar
```

E reenviar um `POST /v1/convenios` com `cnpj` longo demais para confirmar `400`.

