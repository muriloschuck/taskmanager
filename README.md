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

---

## Arquitetura

### Arquitetura em Camadas

```mermaid
block-beta
  columns 1
 
  space:1
  CLIENT["🌐 Cliente HTTP (Postman, cURL, Frontend)"]
  space:1
 
  blockArrowId1<["⬇️&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;⬇️"]>(down)
 
  space:1
  SECURITY["🔒 Security Layer<br/><br/>JwtAuthenticationFilter<br/>SecurityConfig"]
  space:1
 
  blockArrowId2<["⬇️&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;⬇️"]>(down)
 
  space:1
  block:CONTROLLERS
    columns 1
    CTITLE["📡 Controllers (@RestController)"]
    space:1
    CTL1["AuthController · UserController"]
    CTL2["BoardController · TaskController · TaskCommentController"]
  end
  space:1
 
  blockArrowId3<["⬇️&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;⬇️"]>(down)
 
  space:1
  block:SERVICES
    columns 1
    STITLE["⚙️ Services (@Service)"]
    space:1
    SVC1["UserService · BoardService · TaskService · TaskCommentService"]
  end
  space:1
 
  blockArrowId4<["⬇️&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;⬇️"]>(down)
 
  space:1
  block:REPOSITORIES
    columns 1
    RTITLE["💾 Repositories (JpaRepository)"]
    space:1
    REPO1["UserRepository · BoardRepository · BoardMemberRepository"]
    REPO2["TaskRepository · TaskCommentRepository"]
  end
  space:1
 
  blockArrowId5<["⬇️&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;⬇️"]>(down)
 
  space:1
  block:MODELS
    columns 1
    MTITLE["📦 Entities (@Entity)"]
    space:1
    MDL1["User · Board · BoardMember · Task · TaskComment"]
  end
  space:1
 
  blockArrowId6<["⬇️&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;⬇️"]>(down)
 
  space:1
  DB[("🗄️ PostgreSQL 16<br/><br/>Database: taskmanager")]
  space:1
 
  style CLIENT     fill:#E3F2FD,stroke:#1976D2,stroke-width:3px,color:#0D47A1
  style SECURITY   fill:#FFF3E0,stroke:#F57C00,stroke-width:3px,color:#E65100
  style CONTROLLERS fill:#F3E5F5,stroke:#7B1FA2,stroke-width:3px,color:#4A148C
  style CTITLE     fill:#E1BEE7,stroke:#7B1FA2,stroke-width:2px,color:#4A148C
  style CTL1       fill:#F3E5F5,stroke:#BA68C8,stroke-width:2px,color:#4A148C
  style CTL2       fill:#F3E5F5,stroke:#BA68C8,stroke-width:2px,color:#4A148C
  style SERVICES   fill:#E8F5E9,stroke:#388E3C,stroke-width:3px,color:#1B5E20
  style STITLE     fill:#C8E6C9,stroke:#388E3C,stroke-width:2px,color:#1B5E20
  style SVC1       fill:#E8F5E9,stroke:#66BB6A,stroke-width:2px,color:#1B5E20
  style REPOSITORIES fill:#E1F5FE,stroke:#0288D1,stroke-width:3px,color:#01579B
  style RTITLE     fill:#B3E5FC,stroke:#0288D1,stroke-width:2px,color:#01579B
  style REPO1      fill:#E1F5FE,stroke:#4FC3F7,stroke-width:2px,color:#01579B
  style REPO2      fill:#E1F5FE,stroke:#4FC3F7,stroke-width:2px,color:#01579B
  style MODELS     fill:#FFF9C4,stroke:#F9A825,stroke-width:3px,color:#F57F17
  style MTITLE     fill:#FFF59D,stroke:#F9A825,stroke-width:2px,color:#F57F17
  style MDL1       fill:#FFF9C4,stroke:#FFD54F,stroke-width:2px,color:#F57F17
  style DB         fill:#37474F,stroke:#607D8B,stroke-width:4px,color:#ECEFF1
```

### Fluxo de Usuário - Exemplo Completo

**Cenário:** Usuário faz login, cria uma task e faz logout.

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

```
users (UUID id, email UK, password_hash, name, deleted, timestamps)
  │
  ├──< boards (UUID id, name, description, owner_id FK → users)
  │     │
  │     ├──< board_members (UUID id, board_id FK, user_id FK, role ENUM, joined_at)
  │     │                    UK(board_id, user_id)
  │     │
  │     └──< tasks (UUID id, title, description, status ENUM, priority ENUM,
  │               due_date, board_id FK, assigned_user_id FK → users, timestamps)
  │           │
  │           └──< task_comments (UUID id, task_id FK, user_id FK, content, created_at)
  │
  └──< (relationships: owner, board member, task assignee, comment author)
```

### Tabelas e Relacionamentos

#### **users**
- **PK:** `id` (UUID)
- **UK:** `email`
- **Campos:** `password_hash`, `name`, `deleted` (boolean), `created_at`, `updated_at`
- **Soft Delete:** Campo `deleted = true` ao invés de remover fisicamente

#### **boards**
- **PK:** `id` (UUID)
- **FK:** `owner_id` → `users.id`
- **Campos:** `name`, `description`, `created_at`, `updated_at`

#### **board_members** (Junction Table)
- **PK:** `id` (UUID)
- **FK:** `board_id` → `boards.id` (CASCADE DELETE)
- **FK:** `user_id` → `users.id`
- **UK:** `(board_id, user_id)` - impede duplicatas
- **Campos:** `role` (ENUM: OWNER, ADMIN, MEMBER), `joined_at`

#### **tasks**
- **PK:** `id` (UUID)
- **FK:** `board_id` → `boards.id` (CASCADE DELETE)
- **FK:** `assigned_user_id` → `users.id` (nullable)
- **Campos:** `title`, `description`, `status` (ENUM), `priority` (ENUM), `due_date`, `created_at`, `updated_at`

**Enums:**
- `TaskStatus`: PENDING, IN_PROGRESS, DONE, CANCELLED
- `TaskPriority`: LOW, MEDIUM, HIGH

#### **task_comments**
- **PK:** `id` (UUID)
- **FK:** `task_id` → `tasks.id` (CASCADE DELETE)
- **FK:** `user_id` → `users.id`
- **Campos:** `content` (text), `created_at`

### Índices (V6 migration)

Para otimização de queries frequentes:
- `idx_tasks_board_id` em `tasks.board_id`
- `idx_tasks_assigned_user_id` em `tasks.assigned_user_id`
- `idx_tasks_status` em `tasks.status`
- `idx_tasks_priority` em `tasks.priority`
- `idx_bm_board_id` em `board_members.board_id`
- `idx_bm_user_id` em `board_members.user_id`
- `idx_comments_task_id` em `task_comments.task_id`

---

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

a. Instale o [Bruno](https://www.usebruno.com/) (cliente API open-source)
b. Abra o Bruno e clique em "Open Collection"
c. Navegue até `docs/collection/` no projeto
d. A collection será carregada com todos os endpoints configurados

### Parar tudo

```bash
docker compose down          # Para containers
docker compose down -v       # Para e remove volumes (apaga dados)
```

---
