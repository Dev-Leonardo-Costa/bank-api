# 🏦 Bank API

API REST para simulação de operações bancárias, desenvolvida com **Java 21 e Spring Boot**, com foco em regras de negócio, segurança, consistência transacional, testes automatizados, documentação, containerização e CI/CD.

O projeto simula funcionalidades encontradas em sistemas financeiros, incluindo gerenciamento de clientes e contas, transferências via PIX, limites diários, extrato bancário, comprovantes e agendamento de PIX.

> 💡 O objetivo deste projeto vai além de implementar endpoints CRUD. A proposta é trabalhar conceitos encontrados em aplicações backend reais, como concorrência, segurança, transações financeiras, migrations, processamento agendado, testes de integração, documentação de API, containerização e entrega contínua.

---

## 🚀 Funcionalidades

### 👤 Clientes

- Cadastro de clientes
- Validação de CPF
- Validação de e-mail
- Controle de clientes ativos
- Tratamento centralizado de exceções
- Persistência utilizando Spring Data JPA

### 💳 Contas Bancárias

- Criação de contas
- Geração de número da conta
- Associação entre cliente e conta
- Controle de saldo
- Controle de status da conta
- Consulta de informações da conta

### ⚡ PIX

- Cadastro e gerenciamento de chaves PIX
- Transferência utilizando chave PIX
- Normalização da chave PIX
- Validação da conta de origem
- Validação da conta de destino
- Validação de saldo
- Validação de conta ativa
- Bloqueio de PIX para a própria conta
- Limite diário de PIX
- Consulta do limite diário
- Alteração do limite diário
- Controle de concorrência durante transferências
- Persistência das transações realizadas

### 📅 PIX Agendado

- Agendamento de PIX para data futura
- Consulta dos PIX agendados
- Validação da conta de origem
- Validação da chave PIX de destino
- Controle de propriedade da conta
- Processamento automático com Spring Scheduler
- Tratamento de sucesso e falha durante o processamento

Estados do agendamento:

```text
SCHEDULED
PROCESSING
COMPLETED
FAILED
CANCELED
```

O PIX agendado é persistido inicialmente como `SCHEDULED` e posteriormente processado automaticamente pelo scheduler.

### 📊 Extrato Bancário

- Consulta paginada de movimentações
- Ordenação das movimentações
- Identificação de crédito e débito
- PIX enviado
- PIX recebido
- Transferência enviada
- Transferência recebida
- Depósitos
- Saques
- Identificação da contraparte da movimentação

### 🧾 Comprovantes

- Consulta de comprovante por transação
- Identificação do pagador
- Identificação do recebedor
- Conta e agência
- Valor da operação
- Tipo da transação
- Status da transação
- Data da operação
- Controle de acesso ao comprovante

### 🔐 Segurança

- Spring Security
- Autenticação utilizando JWT
- API stateless
- Autorização baseada no usuário autenticado
- Validação de propriedade das contas
- Proteção das operações bancárias
- Controle de acesso aos comprovantes

---

## 🛠️ Tecnologias

| Tecnologia | Utilização |
|---|---|
| Java 21 | Linguagem principal |
| Spring Boot 3.5 | Framework backend |
| Spring Web | Construção da API REST |
| Spring Data JPA | Persistência |
| Hibernate | ORM |
| Spring Security | Segurança |
| JWT | Autenticação stateless |
| PostgreSQL 17 | Banco de dados |
| Flyway | Versionamento do banco |
| MapStruct | Mapeamento Entity/DTO |
| Bean Validation | Validação das requisições |
| Swagger UI | Interface interativa de documentação e testes |
| OpenAPI | Especificação da API REST |
| Docker | Containerização da aplicação |
| Docker Compose | Orquestração local |
| Testcontainers | Testes de integração com PostgreSQL |
| JUnit 5 | Testes automatizados |
| Mockito | Testes unitários |
| AssertJ | Assertions nos testes |
| JaCoCo | Cobertura de testes |
| GitHub Actions | CI/CD |
| GitHub Container Registry | Registry das imagens Docker |
| Render | Deploy da aplicação e PostgreSQL |
| Maven | Build e gerenciamento de dependências |
| Lombok | Redução de código boilerplate |

---

## 🏗️ Arquitetura

O projeto utiliza uma arquitetura em camadas, mantendo responsabilidades separadas entre os componentes da aplicação.

```text
Client
  │
  ▼
Controller
  │
  ▼
Service
  │
  ├────► Mapper
  │
  ▼
Repository
  │
  ▼
PostgreSQL
```

Estrutura simplificada:

```text
src/main/java/com/leonardo/bank_api
│
├── account
│   ├── controller
│   ├── controllerdocs
│   ├── dto
│   ├── entity
│   ├── mapper
│   ├── repository
│   └── service
│
├── customer
│   ├── controller
│   ├── controllerdocs
│   ├── dto
│   ├── entity
│   ├── mapper
│   ├── repository
│   └── service
│
├── pix
│   ├── controller
│   ├── controllerdocs
│   ├── dto
│   ├── entity
│   ├── enums
│   ├── mapper
│   ├── repository
│   ├── scheduler
│   └── service
│
├── transaction
│   ├── controller
│   ├── controllerdocs
│   ├── dto
│   ├── entity
│   ├── mapper
│   ├── repository
│   └── service
│
├── security
├── config
├── common
└── BankApiApplication.java
```

---

## ⚡ Fluxo de uma Transferência PIX

Uma transferência PIX passa por diversas validações antes da movimentação financeira.

```text
Cliente autenticado
        │
        ▼
Requisição PIX
        │
        ▼
Normalização da chave
        │
        ▼
Busca da chave PIX
        │
        ▼
Identificação da conta destino
        │
        ▼
Lock das contas envolvidas
        │
        ▼
Validação de propriedade
        │
        ▼
Validação das contas
        │
        ▼
Validação de saldo
        │
        ▼
Validação do limite diário
        │
        ▼
Débito da conta origem
        │
        ▼
Crédito da conta destino
        │
        ▼
Persistência da transação
        │
        ▼
TransactionResponse
```

---

## 🔒 Concorrência nas Transferências

Operações financeiras exigem atenção especial à consistência dos dados.

Durante uma transferência PIX, as contas envolvidas são carregadas com lock para evitar problemas causados por operações concorrentes.

As contas são acessadas seguindo uma ordem determinística baseada em seus identificadores.

Exemplo:

```text
Transferência:
Conta 10 → Conta 5

Ordem dos locks:

1º → Conta 5
2º → Conta 10
```

Essa estratégia ajuda a reduzir problemas de concorrência e o risco de deadlocks durante transferências simultâneas.

---

## 💰 Limite Diário de PIX

Cada conta possui um limite diário para operações PIX.

Antes da transferência, a aplicação verifica:

```text
Valor já transferido no dia
            +
Valor da nova transferência
            │
            ▼
     Limite diário
```

Caso o valor ultrapasse o limite configurado, a transferência não é realizada.

---

## 📅 Processamento de PIX Agendado

O projeto possui suporte para agendamento de PIX.

Ao criar um agendamento:

```http
POST /pix/schedules
```

o PIX ainda não é executado.

Ele é armazenado inicialmente como:

```text
SCHEDULED
```

O processamento automático utiliza o **Spring Scheduler**.

```text
PixScheduleProcessor
        │
        ▼
Busca agendamentos:
status = SCHEDULED
scheduledAt <= agora
        │
        ▼
    PROCESSING
        │
        ▼
Execução interna do PIX
        │
        ├───────────────┐
        │               │
        ▼               ▼
   COMPLETED          FAILED
```

A execução interna reutiliza as regras financeiras da transferência PIX, incluindo:

- validação de saldo;
- validação das contas;
- limite diário;
- controle de concorrência;
- atualização dos saldos;
- persistência da transação.

O processamento agendado não depende de uma requisição HTTP ou de um JWT ativo.

---

## 📊 Extrato de Transações

O extrato permite visualizar as movimentações realizadas em uma conta.

Exemplo de PIX enviado:

```json
{
  "transactionId": 10,
  "type": "PIX",
  "direction": "DEBIT",
  "amount": 150.00,
  "description": "PIX enviado",
  "counterparty": "Maria Silva",
  "createdAt": "2026-08-21T17:40:00"
}
```

Para quem recebe a mesma operação:

```json
{
  "transactionId": 10,
  "type": "PIX",
  "direction": "CREDIT",
  "amount": 150.00,
  "description": "PIX recebido",
  "counterparty": "Leonardo Costa",
  "createdAt": "2026-08-21T17:40:00"
}
```

O extrato possui paginação para evitar o carregamento de grandes volumes de transações de uma única vez.

---

## 🧾 Comprovante de Transação

As transações possuem comprovantes individuais.

Exemplo:

```json
{
  "transactionId": 25,
  "type": "PIX",
  "status": "COMPLETED",
  "amount": 250.00,
  "payerName": "Cliente Origem",
  "payerAgency": "0001",
  "payerAccount": "00000020",
  "receiverName": "Cliente Destino",
  "receiverAgency": "0001",
  "receiverAccount": "00000021",
  "createdAt": "2026-08-21T19:00:00"
}
```

A aplicação valida se o usuário autenticado participou da transação antes de permitir acesso ao comprovante.

---

## 📚 Documentação da API

A Bank API possui documentação interativa utilizando **Swagger UI e OpenAPI**.

A documentação foi organizada para evitar excesso de anotações diretamente nos controllers.

```text
Controller
    │
    └── Responsável pelos endpoints HTTP

ControllerDocs
    │
    ├── @Tag
    ├── @Operation
    ├── @ApiResponse
    └── @SecurityRequirement

DTO
    │
    └── @Schema
         ├── descrição
         ├── exemplos
         └── formato dos campos

OpenApiConfig
    │
    ├── informações da API
    └── autenticação JWT
```

Essa abordagem mantém os Controllers mais enxutos e concentra a documentação OpenAPI em contratos específicos.

### 🔐 Autenticação pelo Swagger

O Swagger está integrado à autenticação JWT.

Após realizar o login e obter um token, é possível utilizar o botão **Authorize** para autenticar as requisições realizadas pela própria interface.

```text
Login
  │
  ▼
JWT
  │
  ▼
Authorize
  │
  ▼
Endpoints protegidos 🔒
```

### 🧪 Swagger UI

Em desenvolvimento:

```text
http://localhost:8081/swagger-ui/index.html
```

Especificação OpenAPI:

```text
http://localhost:8081/v3/api-docs
```

A interface permite visualizar e testar operações relacionadas a:

- autenticação;
- clientes;
- contas;
- chaves PIX;
- transferências PIX;
- limite diário;
- PIX agendado;
- extrato;
- comprovantes.

Os DTOs utilizam `@Schema` para exibir exemplos e descrições diretamente na documentação.

---

## 🧪 Testes Automatizados

O projeto possui testes unitários e testes de integração para validar regras de negócio e integração com a infraestrutura.

### Testes Unitários

Os testes unitários utilizam:

```text
JUnit 5
Mockito
AssertJ
```

Entre os cenários testados estão:

- transferência PIX realizada com sucesso;
- saldo insuficiente;
- contas inválidas;
- limite diário de PIX;
- consulta de limite;
- alteração de limite;
- validações de segurança;
- geração de comprovante;
- transação inexistente;
- usuário sem acesso à transação;
- criação de PIX agendado;
- listagem de agendamentos;
- processamento do PIX agendado;
- falha durante execução de PIX agendado.

### Testes de Integração

Os testes de integração utilizam **Testcontainers**.

Durante os testes, uma instância real do PostgreSQL é criada automaticamente.

```text
JUnit
  │
  ▼
Spring Boot
  │
  ▼
Hibernate / JPA
  │
  ▼
PostgreSQL
(Testcontainers)
```

Isso permite validar o comportamento da aplicação utilizando um banco semelhante ao ambiente real.

Para executar todos os testes:

```bash
mvn test
```

---

## 📈 Cobertura de Testes com JaCoCo

O projeto utiliza **JaCoCo** para acompanhar a cobertura dos testes automatizados.

A cobertura é utilizada como indicador para identificar partes da aplicação que ainda precisam de cenários de teste.

Execute:

```bash
mvn clean test
```

O relatório HTML é gerado em:

```text
target/site/jacoco/index.html
```

O objetivo não é apenas atingir um percentual de cobertura, mas utilizar a métrica como apoio para evolução da qualidade do código.

---

## 🗄️ Versionamento do Banco

O projeto utiliza **Flyway** para gerenciamento das alterações do banco de dados.

As migrations ficam em:

```text
src/main/resources/db/migration
```

Cada alteração estrutural é criada como uma nova migration.

Exemplo:

```text
V1__create_customers_table.sql
V2__create_accounts_table.sql
...
V8__create_pix_schedules_table.sql
```

Isso permite que o banco seja criado e atualizado de maneira reproduzível e versionada nos diferentes ambientes.

---

## 🌎 Configuração por Ambiente

A aplicação possui configurações separadas por ambiente:

```text
application.yml
│
├── application-dev.yml
├── application-test.yml
└── application-prod.yml
```

### Desenvolvimento

```text
SPRING_PROFILES_ACTIVE=dev
```

Utilizado durante o desenvolvimento local.

### Testes

```text
SPRING_PROFILES_ACTIVE=test
```

Utilizado pelos testes automatizados e testes de integração.

### Produção

```text
SPRING_PROFILES_ACTIVE=prod
```

Utilizado na execução containerizada e no ambiente de produção.

Credenciais, secrets e configurações específicas de infraestrutura são externalizadas através de variáveis de ambiente.

> ⚠️ Secrets, senhas e tokens reais não devem ser versionados no repositório.

---

## 🐳 Docker

A aplicação é totalmente containerizada utilizando **Docker**.

O Dockerfile utiliza **Multi-stage Build**, separando o ambiente responsável pela compilação do ambiente utilizado para executar a aplicação.

```text
Código fonte
     │
     ▼
Maven + Java 21
     │
     ▼
Build
     │
     ▼
JAR
     │
     ▼
Java 21 Runtime
     │
     ▼
Bank API
```

Essa abordagem mantém o processo de build separado da imagem utilizada em runtime.

### Docker Compose

No ambiente local, a API e o PostgreSQL podem ser executados através do Docker Compose:

```text
Docker Compose
│
├── bank-api
│   └── Spring Boot / Java 21
│
└── bank-postgres
    └── PostgreSQL 17
```

O PostgreSQL possui `healthcheck`, permitindo que a aplicação aguarde o banco estar disponível antes da inicialização.

Para construir e iniciar:

```bash
docker compose up -d --build
```

Verificar os containers:

```bash
docker ps
```

Acompanhar os logs:

```bash
docker logs -f bank-api
```

Parar os containers:

```bash
docker compose down
```

Para remover também os volumes locais:

```bash
docker compose down -v
```

---

## 🔄 CI/CD

O projeto possui uma esteira de **Integração Contínua e Entrega Contínua utilizando GitHub Actions**.

O objetivo é automatizar a validação do código, testes, build e publicação da imagem da aplicação.

```text
Developer
    │
    ▼
Git Push
    │
    ▼
GitHub
    │
    ▼
GitHub Actions
    │
    ├──── CI ─────────────────────┐
    │                             │
    │                         Testes
    │                             │
    │                      ├── JUnit
    │                      ├── Mockito
    │                      ├── Testcontainers
    │                      └── JaCoCo
    │                             │
    │                             ▼
    │                           Build
    │                             │
    └───────────────┬─────────────┘
                    │
                    ▼
              Docker Build
                    │
                    ▼
        GitHub Container Registry
                    │
                    ▼
                 Deploy
                    │
                    ▼
                 Render
```

### Continuous Integration

Durante o CI são executados:

- checkout do código;
- configuração do Java 21;
- cache das dependências Maven;
- testes unitários;
- testes de integração;
- Testcontainers com PostgreSQL;
- geração da cobertura com JaCoCo;
- build da aplicação.

Falhas nessas etapas interrompem o pipeline.

### Continuous Delivery

Após a validação do código, uma nova imagem Docker é construída.

A imagem é publicada no **GitHub Container Registry (GHCR)**.

São utilizadas tags como:

```text
latest
SHA do commit
```

O SHA permite relacionar uma determinada imagem Docker ao commit que originou aquele artefato, melhorando a rastreabilidade das versões.

---

## ☁️ Deploy

A aplicação possui deploy em ambiente cloud utilizando **Render**.

A Bank API executa de forma containerizada e utiliza PostgreSQL configurado para o ambiente de produção.

```text
Internet
   │
   ▼
Render
   │
   ├── Bank API
   │      │
   │      ▼
   │ Spring Boot
   │
   └────► PostgreSQL
```

O ambiente de produção utiliza:

```text
SPRING_PROFILES_ACTIVE=prod
```

As configurações de banco de dados e segurança são fornecidas através de variáveis de ambiente.

O Flyway executa as migrations necessárias durante a inicialização da aplicação.

### 🌐 API em Produção

A aplicação está disponível em ambiente cloud através do Render.

> URL pública da API: `ADICIONE_AQUI_SUA_URL_DO_RENDER`

### 📚 Swagger em Produção

Depois de inserir a URL pública:

```text
https://SUA-URL/swagger-ui/index.html
```

OpenAPI JSON:

```text
https://SUA-URL/v3/api-docs
```

---

## ▶️ Como Executar

### Pré-requisitos

Tenha instalado:

```text
Java 21+
Maven
Docker
Docker Compose
Git
```

### Opção 1 — Executar com Docker

Clone o projeto:

```bash
git clone https://github.com/Dev-Leonardo-Costa/bank-api.git
```

Entre na pasta:

```bash
cd bank-api
```

Configure as variáveis de ambiente necessárias.

Depois:

```bash
docker compose up -d --build
```

### Opção 2 — Executar pelo Maven

Configure:

```text
SPRING_PROFILES_ACTIVE=dev
SECURITY_JWT_SECRET=<sua-chave-secreta>
```

Certifique-se de que o PostgreSQL está disponível.

Depois:

```bash
mvn spring-boot:run
```

Também é possível gerar o `.jar`:

```bash
mvn clean package
```

e executar:

```bash
java -jar target/bank-api-*.jar
```

---

## 🔐 Autenticação

Os endpoints protegidos utilizam JWT.

Após autenticar, envie o token:

```http
Authorization: Bearer <token>
```

O usuário autenticado é utilizado para validar acesso às contas e operações bancárias.

No Swagger, o token também pode ser configurado através do botão:

```text
Authorize
```

---

## 📡 Exemplos de Endpoints

### Login

```http
POST /auth/login
```

### Transferência PIX

```http
POST /pix/transfer
```

### Agendar PIX

```http
POST /pix/schedules
```

### Listar PIX Agendados

```http
GET /pix/schedules
```

### Consultar Extrato

```http
GET /transactions/accounts/{accountId}/statement?page=0&size=10
```

### Consultar Comprovante

```http
GET /transactions/{transactionId}/receipt
```

> Consulte o Swagger para visualizar requests, responses, exemplos e códigos HTTP documentados.

---

## 🧠 Decisões Técnicas

### DTOs

As entidades JPA não são expostas diretamente pela API.

```text
Request DTO
    │
    ▼
Service
    │
    ▼
Entity
    │
    ▼
Mapper
    │
    ▼
Response DTO
```

### MapStruct

O MapStruct é utilizado para reduzir mapeamentos manuais entre entidades e DTOs e manter essa responsabilidade separada da camada de serviço.

### Documentação desacoplada dos Controllers

As anotações OpenAPI específicas dos endpoints foram concentradas em interfaces `ControllerDocs`.

```text
Controller
    implements
        ↓
ControllerDocs
```

Isso reduz o excesso de anotações nos controllers e mantém a documentação organizada.

### Flyway

Alterações estruturais do banco são versionadas através de migrations, permitindo reproduzir a estrutura nos diferentes ambientes.

### Lock em Operações Financeiras

Transferências concorrentes exigem controle para evitar inconsistências de saldo.

As contas envolvidas são bloqueadas seguindo uma ordem determinística, reduzindo o risco de deadlocks.

### Testcontainers

Os testes de integração utilizam PostgreSQL real em container em vez de depender exclusivamente de banco em memória.

### Scheduler separado da regra financeira

O scheduler é responsável por localizar os PIX que precisam ser processados.

A regra financeira permanece concentrada no serviço responsável pelo PIX, evitando duplicação de lógica.

### Configuração externalizada

Configurações específicas de ambiente e informações sensíveis são fornecidas através de variáveis de ambiente.

Isso permite executar o mesmo artefato em diferentes ambientes sem alterar o código da aplicação.

### Imagem Docker versionada

As imagens publicadas no GHCR utilizam o SHA do commit, permitindo rastrear qual código originou cada versão da aplicação.

---

## 📈 Evolução do Projeto

### ✅ Implementado

- [x] Cadastro de clientes
- [x] Contas bancárias
- [x] Autenticação JWT
- [x] Transferências
- [x] PIX
- [x] Chaves PIX
- [x] Limite diário de PIX
- [x] Controle de concorrência
- [x] Lock pessimista
- [x] Extrato bancário
- [x] Comprovantes
- [x] PIX agendado
- [x] Processamento com Spring Scheduler
- [x] Testes unitários
- [x] Testes do processamento agendado
- [x] Testes de integração
- [x] Testcontainers
- [x] JaCoCo
- [x] Flyway
- [x] Swagger UI
- [x] OpenAPI
- [x] Autenticação JWT integrada ao Swagger
- [x] Exemplos nos DTOs com `@Schema`
- [x] Documentação separada com `ControllerDocs`
- [x] Docker Multi-stage Build
- [x] Docker Compose
- [x] Profiles dev, test e prod
- [x] Pipeline CI
- [x] Pipeline CD
- [x] Publicação de imagem no GHCR
- [x] Deploy em ambiente cloud

### 🔜 Backlog

- [ ] Idempotência nas operações PIX
- [ ] Cancelamento de PIX agendado
- [ ] Auditoria de transações
- [ ] Spring Boot Actuator
- [ ] Métricas e observabilidade
- [ ] Redis
- [ ] Cache
- [ ] Rate limiting
- [ ] Filtros avançados no extrato

---

## 🎯 Objetivo

A **Bank API** foi criada para aplicar conceitos utilizados no desenvolvimento de sistemas backend modernos e explorar desafios que vão além de operações CRUD.

Entre os principais conceitos trabalhados estão:

```text
✓ API REST
✓ Arquitetura em camadas
✓ Java 21
✓ Spring Boot
✓ Segurança com JWT
✓ Autorização
✓ PostgreSQL
✓ JPA / Hibernate
✓ Flyway
✓ MapStruct
✓ Swagger / OpenAPI
✓ Transações financeiras
✓ Concorrência
✓ Lock pessimista
✓ Limite diário
✓ Processamento agendado
✓ Testes unitários
✓ Testes de integração
✓ Testcontainers
✓ JaCoCo
✓ Docker
✓ Docker Compose
✓ Configuração por ambiente
✓ GitHub Actions
✓ CI/CD
✓ GHCR
✓ Deploy em cloud
```

A proposta é continuar evoluindo o projeto gradualmente, adicionando novos desafios de arquitetura, segurança, performance, observabilidade e confiabilidade.

---

## 👨‍💻 Autor

**Leonardo Costa**

Desenvolvedor Java / Full Stack

Projeto desenvolvido com foco em evolução técnica, boas práticas de engenharia de software e aplicação prática de conceitos utilizados no desenvolvimento backend.

---

⭐ Se este projeto foi útil ou chamou sua atenção, considere deixar uma **Star** no repositório.
