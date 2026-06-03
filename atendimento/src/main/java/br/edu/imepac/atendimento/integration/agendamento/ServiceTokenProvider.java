package br.edu.imepac.atendimento.integration.agendamento;

// Contrato pra fornecer token de servico ao Feign interceptor. Implementacao
// concreta hoje e' KeycloakServiceTokenProvider (client_credentials no Keycloak),
// selecionada via @ConditionalOnProperty(auth.provider=keycloak, matchIfMissing=true).
//
// Trocar de provedor (Auth0, Okta, proprio) = criar nova classe que implemente
// essa interface + ajustar a flag. Zero modificacao no FeignConfig ou em
// qualquer caller.
//
// Visibilidade package-private intencional: a interface so faz sentido dentro
// deste pacote de integracao. Outros pacotes nao injetam token de servico
// diretamente.
interface ServiceTokenProvider {

    String getToken();
}
