# MS_HR 🚀

Sistema de gestão de RH desenvolvido com arquitetura de microsserviços enterprise, utilizando Spring Boot 3.4.5, JWT, Docker e Spring Cloud.

\---

## 📋 Sobre o Projeto

O HR é um sistema backend de RH construído com microsserviços independentes que se comunicam entre si via API Gateway, com autenticação JWT, descoberta de serviços via Eureka e configurações centralizadas no GitHub.

O projeto foi desenvolvido com foco em boas práticas de mercado: DTOs, tratamento global de exceções, Lombok, Circuit Breaker e containerização completa com Docker.

\---

## 🏗️ Arquitetura

```
Cliente (Insomnia/Frontend)
           ↓
hr-api-gateway (:8765) → valida JWT + roteia
           ↓
┌─────────────────────────────────────────┐
│  hr-oauth   →feign→   hr-user          │
│  hr-worker             hr-payroll      │
└─────────────────────────────────────────┘
           ↓
hr-eureka-server (:8761) → Service Discovery
hr-config-server (:8888) → Configs → GitHub
PostgreSQL (Docker)
```

\---

## 🛠️ Tecnologias

|Tecnologia|Versão|Uso|
|-|-|-|
|Java|21 (LTS)|Linguagem principal|
|Spring Boot|3.4.5|Framework principal|
|Spring Cloud|2024.0.1|Microsserviços|
|Spring Security|6.x|Autenticação e autorização|
|JWT (jjwt)|0.12.5|Tokens de acesso|
|Spring Cloud Gateway|-|API Gateway reativo|
|Netflix Eureka|-|Service Discovery|
|Spring Cloud Config|-|Configurações centralizadas|
|OpenFeign|-|Comunicação entre serviços|
|Resilience4j|-|Circuit Breaker|
|Lombok|1.18.46|Redução de boilerplate|
|PostgreSQL|16|Banco de dados produção|
|H2|-|Banco de dados testes|
|Docker|-|Containerização|
|Docker Compose|-|Orquestração de containers|

\---

## 📦 Microsserviços

|Serviço|Porta|Descrição|
|-|-|-|
|`hr-api-gateway`|8765|Gateway central — roteia e valida JWT|
|`hr-eureka-server`|8761|Servidor de descoberta de serviços|
|`hr-config-server`|8888|Configurações centralizadas via GitHub|
|`hr-oauth`|dinâmica|Autenticação e geração de tokens JWT|
|`hr-user`|dinâmica|Gerenciamento de usuários e roles|
|`hr-worker`|dinâmica|CRUD de trabalhadores|
|`hr-payroll`|dinâmica|Cálculo de folha de pagamento|

\---

## 🔐 Segurança

O sistema utiliza autenticação JWT com controle de acesso por roles (RBAC):

|Role|Acesso|
|-|-|
|`ROLE\_OPERATOR`|`GET /workers/\*\*`|
|`ROLE\_ADMIN`|`/workers/\*\*`, `/payments/\*\*`, `/users/\*\*`|

\---

## ✨ Boas Práticas Implementadas

* **DTOs** — separação entre entidades JPA e objetos de transferência
* **Exceções globais** — `@ControllerAdvice` com `StandardError` padronizado
* **Lombok** — código limpo sem boilerplate
* **Circuit Breaker** — fallback automático com Resilience4j
* **Healthcheck** — Docker Compose aguarda serviços ficarem saudáveis
* **Variáveis de ambiente** — tokens e senhas nunca expostos no código
* **Perfis** — `test` (H2) e `dev` (PostgreSQL)

\---

## 🐳 Como Rodar com Docker

### Pré-requisitos

* Docker Desktop instalado e rodando
* Git instalado

### 1\. Clone o repositório

```bash
git clone https://github.com/Viniciuss27/MS\_Teste.git
cd MS\_Teste
```

### 2\. Configure as variáveis de ambiente

Crie um arquivo `.env` na raiz:

```
GIT\_PASSWORD=seu\_token\_github
```

### 3\. Suba todos os containers

```bash
docker-compose up -d
```

### 4\. Verifique os serviços

```bash
docker ps
```

Acesse o Eureka Dashboard:

```
http://localhost:8761
```

\---

## 🧪 Testando a API

### 1\. Gerar token JWT

```
POST http://localhost:8765/oauth/token

Headers:
  client-id:     myappname123
  client-secret: myappsecret123

Params:
  email:    nina@gmail.com
  password: 123456
```

### 2\. Acessar recursos protegidos

```
# ROLE\_OPERATOR ou ROLE\_ADMIN
GET http://localhost:8765/workers
Authorization: Bearer {token}

# ROLE\_ADMIN
GET http://localhost:8765/users/1
Authorization: Bearer {token}

# ROLE\_ADMIN
GET http://localhost:8765/payments/1/days/5
Authorization: Bearer {token}
```

\---

## 🗂️ Estrutura do Projeto

```
HRMicro/
├── Config\_Server/
├── Eureka/
├── Gateway/
├── Oauth/
├── Payroll/
├── User/
├── Worker/
├── docker-compose.yml
├── .env            (não versionado)
└── .gitignore
```

\---

## ⚙️ Configurações Centralizadas

As configurações ficam no repositório:
🔗 [MS\_Config](https://github.com/Viniciuss27/MS_Config)

```
application.yml         → configs globais (JWT, OAuth client)
hr-worker-test.yml      → hr-worker perfil test (H2)
hr-worker-dev.yml       → hr-worker perfil dev (PostgreSQL)
hr-user-test.yml        → hr-user perfil test (H2)
hr-user-dev.yml         → hr-user perfil dev (PostgreSQL)
```

\---

## 📊 Fluxo de Autenticação

```
1. Cliente envia email + senha + client credentials
2. hr-oauth valida o client (clientId + clientSecret)
3. hr-oauth busca o usuário no hr-user via Feign
4. hr-oauth valida a senha com BCrypt
5. hr-oauth gera e retorna o token JWT
6. Cliente usa o token nas próximas requisições
7. hr-api-gateway valida o token e verifica as roles
8. Requisição roteada para o serviço correto
```

\---

👨‍💻 Autor

Desenvolvido por **Vinícius** 🚀

[!\[GitHub](https://img.shields.io/badge/GitHub-Viniciuss27-black?logo=github)](https://github.com/Viniciuss27)
[!\[LinkedIn](https://img.shields.io/badge/LinkedIn-viniciuss27-blue?logo=linkedin)](https://linkedin.com/in/viniciuss27)

\---

## 📄 Licença

Este projeto está sob a licença MIT.

