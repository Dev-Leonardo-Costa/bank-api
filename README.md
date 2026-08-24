# 🏦 Bank API

API REST para simulação de operações bancárias, desenvolvida com **Java e Spring Boot**, com foco em regras de negócio, segurança, consistência transacional, testes automatizados e boas práticas de desenvolvimento backend.

O projeto simula funcionalidades encontradas em sistemas financeiros, incluindo gerenciamento de clientes e contas, transferências via PIX, limites diários, extrato bancário, comprovantes e agendamento de PIX.

> 💡 O objetivo deste projeto vai além de implementar endpoints CRUD. A proposta é trabalhar conceitos encontrados em aplicações backend reais, como concorrência, segurança, transações financeiras, migrations, processamento agendado e testes de integração.

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
- Estados do agendamento:

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
- Proteção de operações bancárias
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
| Docker | Ambiente de desenvolvimento |
| Testcontainers | Testes com PostgreSQL real |
| JUnit 5 | Testes automatizados |
| Mockito | Testes unitários |
| Maven | Build e dependências |
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

Estrutura simplificada do projeto:

```text
src/main/java/com/leonardo/bank_api
│
├── account
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── mapper
│   ├── repository
│   └── service
│
├── customer
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── mapper
│   ├── repository
│   └── service
│
├── pix
│   ├── controller
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
│   ├── dto
│   ├── entity
│   ├── mapper
│   ├── repository
│   └── service
│
├── security
│
├── common
│
└── BankApiApplication.java
```

---

## ⚡ Fluxo de uma transferência PIX

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
Conta 10 → Conta 5

Primeiro lock:
Conta 5

Segundo lock:
Conta 10
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
            ↓
Limite diário da conta
```

Caso o valor ultrapasse o limite configurado, a transferência não é realizada.

---

## 📅 Processamento de PIX Agendado

O projeto possui suporte para agendamento de PIX.

Ao criar um agendamento:

```text
POST /pix/schedules
```

o PIX ainda não é executado.

Ele é armazenado inicialmente com:

```text
SCHEDULED
```

O processamento automático utiliza o **Spring Scheduler**.

```text
PixScheduleProcessor
        │
        ▼
Busca:
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

A execução interna reutiliza as regras financeiras da transferência PIX, como:

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

Exemplo:

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

## 🧪 Testes Automatizados

O projeto possui testes unitários e testes de integração.

### Testes Unitários

Os testes unitários utilizam:

```text
JUnit 5
Mockito
AssertJ
```

Eles validam as regras de negócio de maneira isolada.

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
- usuário sem acesso à transação.

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

## 🗄️ Versionamento do Banco

O projeto utiliza **Flyway** para gerenciamento das alterações do banco.

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

Isso permite que o banco seja criado e atualizado de maneira reproduzível e versionada.

---

## 🐳 Docker

O PostgreSQL pode ser executado utilizando Docker Compose.

Exemplo:

```yaml
services:

  postgres:
    image: postgres:17
    container_name: bank-postgres

    environment:
      POSTGRES_DB: bank
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres

    ports:
      - "5432:5432"

    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

Para iniciar:

```bash
docker compose up -d
```

Para verificar:

```bash
docker ps
```

Para parar:

```bash
docker compose down
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

### 1. Clone o projeto

```bash
git clone <URL_DO_REPOSITORIO>
```

### 2. Entre na pasta

```bash
cd bank-api
```

### 3. Suba o PostgreSQL

```bash
docker compose up -d
```

### 4. Configure as variáveis de ambiente

Configure as propriedades necessárias para execução da aplicação, principalmente o segredo utilizado pelo JWT.

Exemplo:

```text
SECURITY_JWT_SECRET=<sua-chave-secreta>
```

> ⚠️ Nunca versione secrets, senhas ou tokens reais no GitHub.

### 5. Execute a aplicação

```bash
mvn spring-boot:run
```

Ou gere o `.jar`:

```bash
mvn clean package
```

Depois:

```bash
java -jar target/bank-api-*.jar
```

---

## 🔐 Autenticação

Os endpoints protegidos utilizam JWT.

Após autenticar, envie o token no header:

```http
Authorization: Bearer <token>
```

O usuário autenticado é utilizado para validar acesso às contas e operações bancárias.

---

## 📡 Exemplos de Endpoints

### PIX

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

> Os endpoints podem evoluir conforme novas funcionalidades forem adicionadas ao projeto.

---

## 🧠 Decisões Técnicas

Algumas decisões importantes adotadas durante o desenvolvimento:

### DTOs

As entidades JPA não são expostas diretamente pela API.

```text
Request DTO
    ↓
Service
    ↓
Entity
    ↓
Mapper
    ↓
Response DTO
```

### MapStruct

O MapStruct é utilizado para reduzir mapeamentos manuais entre entidades e DTOs.

### Flyway

Alterações no banco são versionadas e reproduzíveis.

### Lock em operações financeiras

Transferências concorrentes exigem controle para evitar inconsistência de saldo.

### Testcontainers

Os testes de integração utilizam PostgreSQL real em container em vez de depender exclusivamente de banco em memória.

### Scheduler separado da regra financeira

O scheduler é responsável por encontrar operações que precisam ser processadas.

A regra financeira continua concentrada no serviço PIX, evitando duplicação de lógica.

---

## 📈 Próximas Evoluções

O projeto continua em desenvolvimento.

Backlog atual:

- [ ] Cancelamento de PIX agendado
- [ ] Testes unitários do PIX Scheduler
- [ ] Testes de integração do agendamento
- [ ] Idempotência em operações PIX
- [ ] Auditoria de transações
- [ ] Filtros avançados no extrato
- [ ] Métricas e observabilidade
- [ ] Redis
- [ ] Pipeline CI/CD
- [ ] Containerização completa da aplicação
- [ ] Deploy em ambiente cloud

---

## 🎯 Objetivo

A **Bank API** foi criada para aplicar conceitos utilizados no desenvolvimento de sistemas backend modernos e explorar desafios que vão além de CRUD.

Entre os principais conceitos trabalhados estão:

```text
✓ API REST
✓ Arquitetura em camadas
✓ Segurança com JWT
✓ Autorização
✓ PostgreSQL
✓ JPA / Hibernate
✓ Migrations
✓ MapStruct
✓ Transações financeiras
✓ Concorrência
✓ Lock pessimista
✓ Limite diário
✓ Processamento agendado
✓ Testes unitários
✓ Testes de integração
✓ Testcontainers
✓ Docker
```

---

## 👨‍💻 Autor

**Leonardo Costa**

Desenvolvedor Java / Full Stack

Projeto desenvolvido com foco em evolução técnica, boas práticas de engenharia de software e demonstração prática de conhecimentos em desenvolvimento backend.

---

⭐ Se este projeto foi útil ou chamou sua atenção, considere deixar uma **Star** no repositório.
