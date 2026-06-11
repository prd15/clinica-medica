# Deploy no Kubernetes

Manifests para rodar a clínica em um cluster local (kind, minikube ou Docker Desktop).
Arquitetura igual à do `docker-compose.yml`: 3 microsserviços + 3 MySQL + Keycloak + gateway.

## Pré-requisitos

As imagens não estão em registry — é preciso buildar e carregar no cluster:

```bash
docker build -f administrativo/Dockerfile -t clinica/administrativo:latest .
docker build -f agendamento/Dockerfile  -t clinica/agendamento:latest .
docker build -f atendimento/Dockerfile  -t clinica/atendimento:latest .
docker build -f gateway/Dockerfile      -t clinica/gateway:latest .

# kind:
kind load docker-image clinica/administrativo:latest clinica/agendamento:latest clinica/atendimento:latest clinica/gateway:latest
# minikube:
# minikube image load <imagem>   (para cada uma)
```

## Deploy

```bash
# 1. Namespace
kubectl apply -f k8s/namespace.yaml

# 2. Secrets — copie o exemplo e troque os valores (NÃO commite o arquivo real)
kubectl apply -f k8s/secrets.example.yaml

# 3. Realm do Keycloak (montado em /opt/keycloak/data/import)
kubectl create configmap keycloak-realm --from-file=keycloak/realm-clinica.json -n clinica

# 4. Tudo o mais
kubectl apply -R -f k8s/
```

O secret `clinica-secrets` precisa das chaves:

| Chave | Uso |
|---|---|
| `db-password` | senha root dos MySQL e `DB_PASS` dos serviços |
| `keycloak-service-secret` | client secret do `clinica-service` (client_credentials); substituído no realm no import e usado pelos serviços |

## Verificação

```bash
kubectl get pods -n clinica          # tudo deve ficar Running/Ready (~2 min)
kubectl port-forward -n clinica svc/gateway 8080:8080
curl http://localhost:8080/api/admin/v1/convenios   # 401 sem token = cadeia OK
```

Com ingress-nginx instalado, as APIs respondem em `clinica.local` (gateway) e o
Keycloak em `keycloak.clinica.local` (adicione ambos no hosts apontando pro IP do ingress).

## Observações

- O issuer dos tokens é `http://keycloak:8180/realms/clinica` (`KC_HOSTNAME=keycloak`);
  os serviços validam esse valor via `KEYCLOAK_ISSUER_URI`.
- Keycloak roda em `start-dev` com admin/admin — configuração de desenvolvimento,
  não usar em produção.
