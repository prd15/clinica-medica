# Teste local do Ingress

## O que e um Ingress Controller

O `Ingress` define regras HTTP/HTTPS de entrada para os servicos do cluster. O Ingress Controller e o componente que executa essas regras na pratica, atuando como proxy reverso dentro do Kubernetes.

Neste projeto, os manifests usam o NGINX Ingress Controller. Sem um controller instalado, o recurso `Ingress` pode existir no cluster, mas as rotas externas nao funcionam.

## Por que usar Ingress

Usamos Ingress para centralizar a exposicao externa dos tres microsservicos em uma unica entrada HTTP, com roteamento por host:

| Host local | Service interno | Porta |
|---|---|---:|
| `administrativo.clinica.local` | `administrativo` | `8081` |
| `agendamento.clinica.local` | `agendamento` | `8082` |
| `atendimento.clinica.local` | `atendimento` | `8083` |

Isso evita criar um `LoadBalancer` separado para cada microsservico. Em ambientes cloud, cada `LoadBalancer` costuma provisionar recurso externo proprio e pode aumentar custo e complexidade. Com Ingress, um unico ponto de entrada distribui as requisicoes para os Services internos.

## Como testar localmente

1. Instale/habilite um NGINX Ingress Controller no cluster local.

   Minikube:

   ```bash
   minikube addons enable ingress
   ```

   Kind ou outros clusters locais geralmente exigem instalar o controller via manifest ou Helm.

2. Aplique os manifests:

   ```bash
   kubectl apply -f k8s/namespace.yaml
   kubectl apply -f k8s/secrets.yaml
   kubectl apply -f k8s/administrativo/
   kubectl apply -f k8s/agendamento/
   kubectl apply -f k8s/atendimento/
   kubectl apply -f k8s/ingress.yaml
   ```

3. Descubra o IP de entrada.

   Minikube:

   ```bash
   minikube ip
   ```

   Em Docker Desktop, normalmente use `127.0.0.1`.

4. Adicione os hosts locais apontando para o IP do Ingress.

   Linux/macOS, arquivo `/etc/hosts`:

   ```text
   127.0.0.1 administrativo.clinica.local
   127.0.0.1 agendamento.clinica.local
   127.0.0.1 atendimento.clinica.local
   ```

   Windows, arquivo `C:\Windows\System32\drivers\etc\hosts`:

   ```text
   127.0.0.1 administrativo.clinica.local
   127.0.0.1 agendamento.clinica.local
   127.0.0.1 atendimento.clinica.local
   ```

   Troque `127.0.0.1` pelo IP retornado pelo seu cluster, se necessario.

5. Teste no navegador:

   ```text
   http://administrativo.clinica.local/swagger-ui/index.html
   http://agendamento.clinica.local/swagger-ui/index.html
   http://atendimento.clinica.local/swagger-ui/index.html
   ```
