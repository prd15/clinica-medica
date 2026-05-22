# Swagger review

Revisao dos contratos Swagger/OpenAPI dos tres microsservicos para apoiar a entrega da Pessoa 6.

## Resultado por servico

| Servico | Dependencia SpringDoc | SwaggerConfig | Controllers anotados | URL local |
|---|---|---|---|---|
| administrativo | Sim | Sim | Sim | `http://localhost:8081/swagger-ui/index.html` |
| agendamento | Sim | Sim | Sim | `http://localhost:8082/swagger-ui/index.html` |
| atendimento | Sim | Sim | Sim | `http://localhost:8083/swagger-ui/index.html` |

## Tabela de revisao

| Arquivo | @Tag | @Operation | @ApiResponse | @Schema nos campos | Status |
|---|---|---|---|---|---|
| AtendenteController | Sim | Sim | Sim | N/A | OK |
| ConvenioController | Sim | Sim | Sim | N/A | OK |
| EspecialidadeController | Sim | Sim | Sim | N/A | OK |
| MedicoController | Sim | Sim | Sim | N/A | OK |
| PacienteController | Sim | Sim | Sim | N/A | OK |
| RelatorioController | Sim | Sim | Sim | N/A | OK |
| ConsultaController | Sim | Sim | Sim | N/A | OK |
| AtendimentoController | Sim | Sim | Sim | N/A | OK |
| GlobalExceptionHandler | N/A | N/A | N/A | N/A | Fora do Swagger de endpoints |
| AlterarStatusRequest | N/A | N/A | N/A | Sim | OK |
| AtendenteRequest | N/A | N/A | N/A | Sim | OK |
| AtendenteResponse | N/A | N/A | N/A | Sim | OK |
| ConsultaDiariaRelatorioResponse | N/A | N/A | N/A | Sim | OK |
| ConvenioRequest | N/A | N/A | N/A | Sim | OK |
| ConvenioResponse | N/A | N/A | N/A | Sim | OK |
| ErrorResponse | N/A | N/A | N/A | Sim | OK |
| EspecialidadeRequest | N/A | N/A | N/A | Sim | OK |
| EspecialidadeResponse | N/A | N/A | N/A | Sim | OK |
| MedicoRequest | N/A | N/A | N/A | Sim | OK |
| MedicoResponse | N/A | N/A | N/A | Sim | OK |
| PacienteRequest | N/A | N/A | N/A | Sim | OK |
| PacienteResponse | N/A | N/A | N/A | Sim | OK |
| PacientesPorConvenioRelatorioResponse | N/A | N/A | N/A | Sim | OK |
| ConsultaRequest | N/A | N/A | N/A | Sim | OK |
| ConsultaResponse | N/A | N/A | N/A | Sim | OK |
| ReagendarRequest | N/A | N/A | N/A | Sim | OK |
| AnotacaoRequest | N/A | N/A | N/A | Sim | OK |
| AnotacaoResponse | N/A | N/A | N/A | Sim | OK |
| AtendimentoRequest | N/A | N/A | N/A | Sim | OK |
| AtendimentoResponse | N/A | N/A | N/A | Sim | OK |
| ExameRequest | N/A | N/A | N/A | Sim | OK |
| ExameResponse | N/A | N/A | N/A | Sim | OK |
| HistoricoResponse | N/A | N/A | N/A | Sim | OK |
| ProntuarioResponse | N/A | N/A | N/A | Sim | OK |

## Correcoes realizadas

- Adicionados `@ApiResponse` de `400` em endpoints com `PathVariable`, `RequestParam` ou payload validavel que ainda nao documentavam entrada invalida.
- Adicionado `@ApiResponse` de `404` no agendamento de consulta para recursos externos inexistentes.
- Adicionado `example` ao campo `prontuarioId` de `AtendimentoResponse`.
- Confirmado que `MedicoResponse` e `AtendenteResponse` nao expõem campo de senha.

## Pontos verificados

- Os tres modulos possuem `springdoc-openapi-starter-webmvc-ui`.
- Os tres modulos possuem bean `OpenAPI` com titulo, versao e descricao.
- Os controllers principais usam `@Tag`, `@Operation` e `@ApiResponse`.
- DTOs de request e response possuem `@Schema(description)` na classe e `@Schema(description, example)` nos campos.
- Todas as APIs mantem o prefixo versionado `/v1/`.

## Observacoes

- O endpoint `GET /v1/relatorios/consultas-diarias` esta oculto no Swagger do administrativo porque depende de integracao futura com o agendamento.
- Para acesso dentro do Kubernetes, use `kubectl port-forward` para o Service desejado e abra `/swagger-ui/index.html`.
- Para versionamento formal de contrato, a proxima melhoria recomendada e gerar e salvar os arquivos OpenAPI JSON em `docs/openapi/`.
