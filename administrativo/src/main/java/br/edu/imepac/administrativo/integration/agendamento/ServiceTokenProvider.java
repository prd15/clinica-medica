package br.edu.imepac.administrativo.integration.agendamento;

// Contrato pra fornecer token de servico ao Feign interceptor que chama o
// agendamento. Implementacao concreta hoje e' KeycloakServiceTokenProvider
// (client_credentials no Keycloak), selecionada via @ConditionalOnProperty
// (auth.provider=keycloak, matchIfMissing=true).
//
// Trocar de provedor (Auth0, Okta, proprio) = criar nova classe que implemente
// essa interface + ajustar a flag. Zero modificacao no FeignConfig ou em
// qualquer caller.
//
// Visibilidade package-private intencional.
interface ServiceTokenProvider {

    String getToken();
}
