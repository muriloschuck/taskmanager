# Task Manager API

> **Trabalho T2 — Engenharia de Software: Arquitetura e Padrões**  
> UNISINOS · Prof. Guilherme Silva de Lacerda

---

## Índice

1. [Visão Geral](#-visão-geral)
2. [Stack Tecnológica](#-stack-tecnológica)
3. [Arquitetura](#-arquitetura)
4. [Modelagem de Dados](#-modelagem-de-dados)
5. [API Endpoints](#-api-endpoints)
6. [Como Rodar](#-como-rodar)
7. [Autenticação](#-autenticação)
8. [Exemplos de Uso](#-exemplos-de-uso)
9. [Estrutura do Projeto](#-estrutura-do-projeto)
10. [Decisões Técnicas](#-decisões-técnicas)

---

## Visão Geral

API RESTful para um **sistema de gestão de tarefas colaborativas**, permitindo:

- Criar e gerenciar usuários com autenticação usando JWTs
- Adicionar membros a boards (com controle de permissões)
- Criar e gerenciar tarefas com status, prioridade e atribuição
- Comentar em tarefas
- Filtros avançados de tarefas (status, prioridade, busca por texto)

---

## Stack

| Componente | Tecnologia
|------------|-----------
| **Linguagem** | Java 21 |
| **Framework** | Spring Boot |
| **Banco de Dados** | PostgreSQL 16 Alpine |
| **Migrations** | Liquibase |
| **Documentação API** | SpringDoc OpenAPI 3 |
| **Build** | Maven 3.9 |
| **Infra** | Docker Compose |
| **Utilities** | Lombok | 1.18.46 |
| **Testes** | JUnit 5 + Mockito |

---

## Arquitetura

### Arquitetura em Camadas

```mermaid
flowchart TD
    Client["Cliente HTTP"]

    subgraph Security["Security Layer"]
        JWT["JwtAuthenticationFilter"]
        SC["SecurityConfig"]
    end

    subgraph Controllers["Controllers"]
        AC["AuthController"]
        UC["UserController"]
        BC["BoardController"]
        TC["TaskController"]
        TCC["TaskCommentController"]
    end

    subgraph Services["Services"]
        US["UserService"]
        BS["BoardService"]
        TS["TaskService"]
        TCS["TaskCommentService"]
    end

    subgraph Repositories["Repositories"]
        UR["UserRepository"]
        BR["BoardRepository"]
        BMR["BoardMemberRepository"]
        TR["TaskRepository"]
        TCR["TaskCommentRepository"]
    end

    subgraph Entities["Entities"]
        E["User · Board · BoardMember<br/>Task · TaskComment"]
    end

    DB[("PostgreSQL 16")]

    Client --> Security
    Security --> Controllers
    Controllers --> Services
    Services --> Repositories
    Repositories --> Entities
    Entities --> DB

    classDef clientStyle fill:#E3F2FD,stroke:#1976D2,stroke-width:2px,color:#0D47A1
    classDef securityStyle fill:#FFF3E0,stroke:#F57C00,stroke-width:2px,color:#E65100
    classDef controllerStyle fill:#F3E5F5,stroke:#7B1FA2,stroke-width:2px,color:#4A148C
    classDef serviceStyle fill:#E8F5E9,stroke:#388E3C,stroke-width:2px,color:#1B5E20
    classDef repoStyle fill:#E1F5FE,stroke:#0288D1,stroke-width:2px,color:#01579B
    classDef entityStyle fill:#FFF9C4,stroke:#F9A825,stroke-width:2px,color:#F57F17
    classDef dbStyle fill:#37474F,stroke:#607D8B,stroke-width:3px,color:#ECEFF1

    class Client clientStyle
    class JWT,SC securityStyle
    class AC,UC,BC,TC,TCC controllerStyle
    class US,BS,TS,TCS serviceStyle
    class UR,BR,BMR,TR,TCR repoStyle
    class E entityStyle
    class DB dbStyle
```

### Fluxo

```mermaid
sequenceDiagram
    actor User as Usuário
    participant API as Task Manager API
 
    Note over User,API: 1. AUTENTICAÇÃO
    User->>API: POST /api/v1/auth/login<br/>{email: "user@example.com", password: "senha123"}
    API-->>User: 200 OK<br/>{token: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."}
    
    Note over User: Armazena JWT token
    
    Note over User,API: 2. CRIAR TASK
    User->>API: POST /api/v1/tasks<br/>Authorization: Bearer {token}<br/>{title: "Implementar login", boardId: "uuid", priority: "HIGH"}
    API-->>User: 201 Created<br/>{id: "uuid", title: "Implementar login", status: "PENDING", ...}
    
    Note over User,API: 3. LISTAR TASKS DO BOARD
    User->>API: GET /api/v1/tasks?boardId={uuid}<br/>Authorization: Bearer {token}
    API-->>User: 200 OK<br/>[{task1}, {task2}, {task3}]
    
    Note over User,API: 4. ATUALIZAR STATUS DA TASK
    User->>API: PATCH /api/v1/tasks/{id}<br/>Authorization: Bearer {token}<br/>{status: "IN_PROGRESS"}
    API-->>User: 200 OK<br/>{id: "uuid", status: "IN_PROGRESS", updatedAt: "2026-06-16T..."}
    
    Note over User,API: 5. ADICIONAR COMENTÁRIO
    User->>API: POST /api/v1/tasks/{id}/comments<br/>Authorization: Bearer {token}<br/>{text: "Iniciando implementação"}
    API-->>User: 201 Created<br/>{id: "uuid", text: "Iniciando...", authorName: "João"}
    
    Note over User,API: 6. LOGOUT
    User->>API: POST /api/v1/auth/logout<br/>Authorization: Bearer {token}
    API-->>User: 204 No Content
    
    Note over User: Remove token (client-side)
```

---

## Modelagem de Dados

### Diagrama ER

```mermaid
erDiagram
    USERS ||--o{ BOARDS : "owns"
    USERS ||--o{ BOARD_MEMBERS : "participates"
    USERS ||--o{ TASKS : "assigned_to"
    USERS ||--o{ TASK_COMMENTS : "authors"
    BOARDS ||--o{ BOARD_MEMBERS : "has"
    BOARDS ||--o{ TASKS : "contains"
    TASKS ||--o{ TASK_COMMENTS : "receives"

    USERS {
        UUID id PK
        string email UK
        string password_hash
        string name
        boolean deleted
        timestamp created_at
        timestamp updated_at
    }

    BOARDS {
        UUID id PK
        string name
        string description
        UUID owner_id FK
        timestamp created_at
        timestamp updated_at
    }

    BOARD_MEMBERS {
        UUID id PK
        UUID board_id FK
        UUID user_id FK
        enum role "OWNER, ADMIN, MEMBER"
        timestamp joined_at
    }

    TASKS {
        UUID id PK
        string title
        string description
        enum status "PENDING, IN_PROGRESS, DONE, CANCELLED"
        enum priority "LOW, MEDIUM, HIGH"
        timestamp due_date
        UUID board_id FK
        UUID assigned_user_id FK
        timestamp created_at
        timestamp updated_at
    }

    TASK_COMMENTS {
        UUID id PK
        UUID task_id FK
        UUID user_id FK
        text content
        timestamp created_at
    }
```

## Como Rodar

### Pré-requisitos

- **Java 21** ou superior
- **Maven 3.9+**
- **Docker**

### 1. Clone o repositório

```bash
git clone <repository-url>
cd taskmanager
```

### 2. Suba o banco de dados PostgreSQL

```bash
docker compose up -d
```

Verifique se está rodando:
```bash
docker compose ps
```

### 3. Execute a aplicação

```bash
./mvnw spring-boot:run
```

### 4. Acesse

| URL | Descrição |
|-----|-----------|
| `http://localhost:8080` | API Base |
| `http://localhost:8080/swagger-ui.html` | Swagger UI |
| `http://localhost:8080/v3/api-docs` | OpenAPI JSON spec |

### 5. Verifique o banco de dados

```bash
docker exec -it taskmanager-db psql -U taskuser -d taskmanager

# Dentro do psql:
\dt                          # Lista todas as tabelas
\d users                     # Describe da tabela users
SELECT * FROM databasechangelog;  # Vê migrations aplicadas
```

### 6. Teste a API com Bruno

Use a collection do Bruno em `docs/collection/` para testar todos os endpoints.

- a. Instale o [Bruno](https://www.usebruno.com/) (cliente API open-source)
- b. Abra o Bruno e clique em "Open Collection"
- c. Navegue até `docs/collection/` no projeto
- d. A collection será carregada com todos os endpoints configurados

### Parar tudo

```bash
docker compose down          # Para containers
docker compose down -v       # Para e remove volumes (apaga dados)
```

---
