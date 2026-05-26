# Melhorias Arquiteturais - Fase 0

Escopo para o agente implementar: melhorias estruturais ainda dentro da Fase 0.
Nao implementar OpenFeign, Gateway, Keycloak, JWT, ApiResponse global ou mudancas de contrato HTTP nesta rodada.

Base de comparacao analisada: `C:\Users\pedro\Desktop\clinica-medica-api`.

## Objetivo

Deixar o projeto mais alinhado ao conceito de microsservicos e mais organizado por contexto de negocio, mantendo comportamento HTTP atual e reduzindo acoplamento do `commons`.

## Prioridade 1 - Completar package-by-feature

Estado atual:

- Entidades, repositories e services ja foram movidos para os microsservicos donos.
- Controllers e DTOs ainda estao em pacotes globais como `controllers/` e `dtos/`.

Problema:

Isso deixa cada feature dividida entre varios pacotes. Exemplo: `PacienteService` fica em `paciente/`, mas `PacienteController`, `PacienteRequest` e `PacienteResponse` ficam fora da feature.

Alvo:

Cada feature deve concentrar controller, service, repository, entity e DTOs:

```text
administrativo/
  paciente/
    PacienteController.java
    PacienteService.java
    PacienteRepository.java
    PacienteEntity.java
    dto/
      PacienteRequest.java
      PacienteResponse.java

  convenio/
    ConvenioController.java
    ConvenioService.java
    ConvenioRepository.java
    ConvenioEntity.java
    dto/
      ConvenioRequest.java
      ConvenioResponse.java

  medico/
  especialidade/
  atendente/
  relatorio/
    RelatorioController.java
    dto/
      ConsultaDiariaRelatorioResponse.java
      PacientesPorConvenioRelatorioResponse.java
```

Para `agendamento`:

```text
agendamento/
  consulta/
    ConsultaController.java
    ConsultaService.java
    ConsultaRepository.java
    ConsultaEntity.java
    StatusConsulta.java
    dto/
      ConsultaRequest.java
      ConsultaResponse.java
      ReagendarRequest.java
      ContagemConsultasResponse.java
```

Para `atendimento`:

```text
atendimento/
  atendimento/
    AtendimentoController.java
    AtendimentoService.java
    AtendimentoRepository.java
    AtendimentoEntity.java
    StatusAtendimento.java
    dto/
      AtendimentoRequest.java
      AtendimentoResponse.java
      HistoricoResponse.java

  prontuario/
    ProntuarioEntity.java
    ProntuarioRepository.java
    dto/
      ProntuarioResponse.java

  anotacao/
    AnotacaoEntity.java
    AnotacaoRepository.java
    dto/
      AnotacaoRequest.java
      AnotacaoResponse.java

  exame/
    SolicitacaoExameEntity.java
    SolicitacaoExameRepository.java
    dto/
      ExameRequest.java
      ExameResponse.java
```

Observacoes:

- Mover arquivos com refactor/rename para preservar historico quando possivel.
- Atualizar imports em controllers, services, tests e Swagger configs se necessario.
- Manter rotas HTTP iguais.
- Manter nomes de classes iguais.
- Nao alterar payloads.
- `ErrorResponse` pode continuar por modulo por enquanto, ate a prioridade 3.

Validacao:

```bash
mvn clean test
```

## Prioridade 2 - Introduzir auto-configuracao no commons

Estado atual:

As Applications dos microsservicos incluem `br.edu.imepac.commons.config` no `scanBasePackages` para carregar `ModelMapperConfig`.

Problema:

Uma biblioteca tecnica nao deveria depender de scan manual em cada microsservico consumidor.

Alvo:

Criar auto-configuracao Spring Boot 3 no `commons`:

```text
commons/
  src/main/java/br/edu/imepac/commons/config/
    CommonsAutoConfiguration.java
    ModelMapperConfig.java

  src/main/resources/META-INF/spring/
    org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

`CommonsAutoConfiguration` deve registrar/importar os beans tecnicos do commons, inicialmente `ModelMapperConfig`.

Exemplo de direcao:

```java
package br.edu.imepac.commons.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(ModelMapperConfig.class)
public class CommonsAutoConfiguration {
}
```

Arquivo:

```text
br.edu.imepac.commons.config.CommonsAutoConfiguration
```

Depois disso, as Applications podem ficar assim:

```java
@SpringBootApplication
@EnableJpaAuditing
public class AdministrativoApplication {
}
```

Para `atendimento`, manter tambem `@EnableScheduling`.

Cuidados:

- Nao usar `scanBasePackages = "br.edu.imepac"` como atalho.
- Nao colocar dominio no commons.
- Confirmar que `ModelMapper` ainda sobe em todos os modulos.

Validacao:

```bash
mvn clean test
```

## Prioridade 3 - Unificar GlobalExceptionHandler no commons

Estado atual:

Existem handlers praticamente duplicados em:

```text
administrativo/controllers/GlobalExceptionHandler.java
agendamento/controllers/GlobalExceptionHandler.java
atendimento/controllers/GlobalExceptionHandler.java
```

Problema:

Duplicacao aumenta chance de divergencia entre microsservicos e enfraquece o papel do `commons` como biblioteca tecnica.

Alvo:

Mover o tratamento comum de exceptions para o `commons`, via auto-configuracao.

Estrutura sugerida:

```text
commons/
  exceptions/
    BusinessException.java
    EntityNotFoundException.java
    IntegrationException.java
    ServicoIndisponivelException.java
  exceptions/handler/
    GlobalExceptionHandler.java
  dtos/
    ErrorResponse.java
```

O handler comum deve cobrir:

- `EntityNotFoundException` -> 404
- `BusinessException` -> 409
- `IntegrationException` -> 503
- `ServicoIndisponivelException` -> 503
- `MethodArgumentNotValidException` -> 400
- `IllegalArgumentException` -> 400, pois foi decidido manter esse uso no `ConsultaController`
- `IllegalStateException` -> 409
- `NoSuchElementException` -> 404
- `DateTimeParseException` -> 400
- `DataIntegrityViolationException` -> 409
- `HttpMessageNotReadableException` -> 400
- `MethodArgumentTypeMismatchException` -> 400
- `MissingServletRequestParameterException` -> 400
- `ConstraintViolationException` -> 400
- `HttpRequestMethodNotSupportedException` -> 405
- fallback `Exception` -> 500

Atendimento tem caso especifico:

```java
IncorrectResultSizeDataAccessException -> 409
```

Opcoes:

1. Incluir esse caso no handler comum, se nao prejudicar os outros modulos.
2. Manter um handler local pequeno apenas em `atendimento` para esse caso especifico.

Recomendacao:

Preferir opcao 1 se compilar sem dependencias extras e sem mudar comportamento.

Cuidados:

- Manter formato atual de erro se possivel para nao quebrar collections/Postman.
- Nao introduzir `ApiResponse<T>` global agora.
- Remover handlers locais so depois de confirmar que o handler comum esta sendo carregado via auto-config.
- Evitar conflito de dois `@RestControllerAdvice` tratando as mesmas exceptions.

Validacao:

```bash
mvn clean test
```

Depois, subir os servicos e testar alguns erros HTTP manualmente ou via Newman:

- ID invalido em path -> 400
- entidade inexistente -> 404
- regra de negocio -> 409
- servico externo indisponivel -> 503

## Fora do escopo desta rodada

- OpenFeign.
- Gateway.
- Keycloak.
- JWT proprio.
- `ApiResponse<T>` global.
- Alterar status HTTP atuais.
- Alterar payloads de requests/responses.
- Atualizar README/documentacao geral.
