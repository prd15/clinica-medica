# Plano de Refatoracao Arquitetural

Este documento resume as mudancas arquiteturais recomendadas para evoluir o projeto `clinica-medica` a partir da base atual, mantendo o que ja funciona e melhorando o isolamento entre microsservicos.

## 1. Quebrar o `commons`

Hoje o `commons` concentra entidades, repositories e services de varios dominios. Isso ajuda a reduzir duplicacao, mas cria acoplamento forte entre os microsservicos. Na pratica, os servicos parecem separados em runtime, mas compartilham modelo de dominio em tempo de compilacao.

A proposta e transformar o `commons` em uma biblioteca tecnica, sem conhecimento das regras da clinica.

Deve ficar no `commons`:

- `ApiResponse<T>`, se o projeto decidir padronizar envelopes de resposta.
- Excecoes tecnicas ou genericas, como `BusinessException`, `EntityNotFoundException` e `IntegrationException`.
- Um `GlobalExceptionHandler` comum, se os tres servicos puderem compartilhar o mesmo padrao de erro.
- Configuracoes tecnicas reutilizaveis, como `ModelMapperConfig`.
- Utilitarios pequenos e independentes de dominio.

Deve sair do `commons`:

- Entidades JPA.
- Repositories.
- Services de negocio.
- Enums especificos de cada dominio.
- Qualquer regra que pertença a administrativo, agendamento ou atendimento.

Beneficio principal: cada microsservico passa a ser dono real do seu modelo e pode evoluir sem carregar classes de outros dominios.

## 2. Mover dominio para cada microsservico

Depois de reduzir o `commons`, cada modulo deve receber suas entidades, repositories e services.

Estrutura sugerida:

```text
administrativo/
  paciente/
    PacienteEntity
    PacienteRepository
    PacienteService
    PacienteController
    dto/
  medico/
  convenio/
  especialidade/
  atendente/

agendamento/
  consulta/
    ConsultaEntity
    ConsultaRepository
    ConsultaService
    ConsultaController
    dto/
  clients/

atendimento/
  atendimento/
  prontuario/
  anotacao/
  exame/
  outbox/
  clients/
```

Essa organizacao usa package-by-feature. Ela tende a escalar melhor que separar tudo por camada global, porque cada caso de uso fica agrupado em um lugar.

Regras importantes:

- `administrativo` e dono de pacientes, medicos, convenios, especialidades e atendentes.
- `agendamento` e dono das consultas e da agenda.
- `atendimento` e dono do atendimento clinico, prontuario, anotacoes, exames e outbox.
- Referencias entre servicos devem continuar por `Long id`, sem relacionamento JPA entre bancos diferentes.

## 3. Adicionar excecoes tipadas e handler comum

O projeto atual usa bastante `IllegalArgumentException`, `IllegalStateException` e `NoSuchElementException`. Funciona, mas essas excecoes sao genericas e espalham semantica de HTTP pelos handlers.

Criar excecoes tipadas deixa as regras mais claras.

Exemplos:

```java
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String entity, Object id) {
        super(entity + " nao encontrado com id: " + id);
    }
}

public class IntegrationException extends RuntimeException {
    public IntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

Mapeamento sugerido:

- `EntityNotFoundException` -> `404 Not Found`
- `BusinessException` -> `409 Conflict` ou `422 Unprocessable Entity`
- `IntegrationException` -> `503 Service Unavailable` ou `502 Bad Gateway`
- `MethodArgumentNotValidException` -> `400 Bad Request`
- `DataIntegrityViolationException` -> `409 Conflict`
- excecao inesperada -> `500 Internal Server Error`

Se os tres servicos compartilharem exatamente o mesmo padrao de erro, o `GlobalExceptionHandler` pode ficar no `commons` via auto-configuration. Se cada servico precisar de casos especificos, o `commons` pode fornecer apenas as excecoes e um helper/base, mantendo handlers concretos nos modulos.

## 4. Trocar `RestTemplate` por OpenFeign

O `RestTemplate` atual funciona, mas gera boilerplate: montagem manual de URL, tratamento manual de `RestClientException`, parse manual de respostas e repeticao entre clients.

Com OpenFeign, o contrato HTTP fica declarativo.

Exemplo no `agendamento`:

```java
@FeignClient(
    name = "administrativo",
    url = "${administrativo.url}",
    configuration = AdministrativoFeignConfig.class
)
public interface AdministrativoClient {

    @GetMapping("/v1/pacientes/{id}")
    PacienteRefDTO buscarPaciente(@PathVariable Long id);

    @GetMapping("/v1/medicos/{id}")
    MedicoRefDTO buscarMedico(@PathVariable Long id);

    @GetMapping("/v1/convenios/{id}")
    ConvenioRefDTO buscarConvenio(@PathVariable Long id);
}
```

Tambem vale criar um `ErrorDecoder` para traduzir erros HTTP em excecoes do projeto:

```java
public class FeignErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()) {
            case 404 -> new EntityNotFoundException("Recurso remoto", methodKey);
            case 409, 422 -> new BusinessException("Regra recusada pelo servico remoto");
            case 502, 503, 504 -> new IntegrationException("Servico remoto indisponivel", null);
            default -> new IntegrationException("Erro remoto HTTP " + response.status(), null);
        };
    }
}
```

Recomendacao: manter DTOs de referencia dentro do servico consumidor, por exemplo `agendamento/clients/dto/PacienteRefDTO`. Isso evita acoplar contratos de dominio no `commons`.

## 5. Adicionar API Gateway

O gateway deve ser a entrada unica externa da aplicacao. Em vez de o cliente chamar diretamente `8081`, `8082` e `8083`, ele chama apenas o gateway.

Fluxo alvo:

```text
cliente
  -> gateway
      -> administrativo
      -> agendamento
      -> atendimento
```

Responsabilidades do gateway:

- Roteamento por path.
- CORS centralizado.
- Validacao inicial de autenticacao.
- Possivel rate limit no futuro.
- Ponto unico para expor a API ao frontend.

Exemplo de rotas:

```text
/api/admin/**        -> administrativo
/api/agendamentos/** -> agendamento
/api/atendimentos/** -> atendimento
```

Com Spring Cloud Gateway, ele sera WebFlux. Por isso, e melhor nao importar o `commons` se ele depender de Spring MVC. Caso precise de resposta padronizada no gateway, crie uma resposta local simples.

O gateway nao substitui completamente a seguranca nos microsservicos. O ideal e os servicos tambem validarem o token, para defesa em profundidade.

## 6. Adicionar Keycloak

Para login e autorizacao, Keycloak e uma escolha melhor que implementar JWT proprio. Ele resolve autenticacao como produto pronto e reduz codigo sensivel dentro da aplicacao.

Keycloak ficaria responsavel por:

- Login.
- Cadastro/gestao de usuarios.
- Senhas e hash.
- Emissao de access token e refresh token.
- Roles e grupos.
- Fluxo OAuth2/OpenID Connect.

Os microsservicos e o gateway viram Resource Servers: eles validam o JWT emitido pelo Keycloak.

Dependencia principal nos microsservicos:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

Configuracao base:

```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/clinica
```

Roles sugeridas:

- `ADMIN`: gerencia cadastros, usuarios e configuracoes.
- `ATENDENTE` ou `RECEPCIONISTA`: cria e gerencia consultas.
- `MEDICO`: acessa agenda propria e registra atendimento.
- `GESTOR`: acessa relatorios.
- `PACIENTE`: consulta dados proprios, se esse perfil entrar no escopo.

Exemplo de autorizacao:

```java
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/v1/medicos")
public ResponseEntity<MedicoResponse> criar(...) {
    ...
}
```

Cuidados:

- Definir um realm especifico, por exemplo `clinica`.
- Definir clients separados para frontend/gateway, se houver frontend.
- Padronizar o claim de roles para o Spring Security entender.
- Evitar que endpoints internos fiquem publicos sem necessidade.

## Ordem recomendada de implementacao

Evite fazer tudo ao mesmo tempo. A ordem mais segura e:

1. Quebrar o `commons`.
2. Mover entidades, repositories e services para os microsservicos donos.
3. Adicionar excecoes tipadas e padronizar handlers.
4. Trocar `RestTemplate` por OpenFeign.
5. Adicionar gateway.
6. Adicionar Keycloak e proteger endpoints por perfil.

Se Flyway entrar no escopo junto, ele deve vir depois da reorganizacao do dominio e antes da seguranca:

```text
commons tecnico
-> dominio por servico
-> Flyway por servico
-> OpenFeign
-> Gateway
-> Keycloak
```

## Resultado esperado

Ao final, o projeto fica mais coerente com microsservicos:

- Cada servico tem seu proprio dominio e banco.
- O `commons` deixa de acoplar regras de negocio.
- A comunicacao entre servicos fica declarativa com Feign.
- O gateway concentra a entrada externa.
- Keycloak centraliza autenticacao e autorizacao.
- O codigo fica mais facil de explicar, manter e evoluir.
