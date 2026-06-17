# Task Manager API

> **Trabalho T2 — Engenharia de Software: Arquitetura e Padrões**  
> UNISINOS · Prof. Guilherme Silva de Lacerda

---

## 📋 Índice

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

## 🎯 Visão Geral

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

## 🏗️ Arquitetura

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

**Possíveis Respostas de Erro:**

| Status | Camada | Causa |
|--------|--------|-------|
| `400 Bad Request` | Controller | Payload inválido (@Valid falhou) |
| `401 Unauthorized` | JwtFilter | Token ausente, inválido ou expirado |
| `403 Forbidden` | Validator | Usuário não é membro do board |
| `404 Not Found` | Service | Recurso não encontrado no banco |
| `409 Conflict` | Service | Email duplicado ou membro já existe |


---

## 🗄️ Modelagem de Dados

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

## 🔌 API Endpoints

> **Base URL:** `http://localhost:8080/api/v1`  
> **Documentação Interativa:** `http://localhost:8080/swagger-ui.html`

### Autenticação

Todos os endpoints (exceto registro e login) requerem:
```
Authorization: Bearer <JWT_TOKEN>
```

#### **POST /auth/login** (público)
Autentica usuário e retorna token JWT.

**Request:**
```json
{
  "email": "user@example.com",
  "password": "senha123"
}
```

**Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### **POST /auth/logout**
Invalida token (client-side).

**Response:** `204 No Content`

---

### Usuários

#### **POST /users** (público)
Registra novo usuário.

**Request:**
```json
{
  "name": "João Silva",
  "email": "joao@example.com",
  "password": "senha123"
}
```

**Validações:**
- `name`: obrigatório, não vazio
- `email`: obrigatório, formato válido, único
- `password`: obrigatório, mínimo 6 caracteres

**Response:** `201 Created`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "João Silva",
  "email": "joao@example.com",
  "createdAt": "2026-06-16T12:00:00",
  "updatedAt": "2026-06-16T12:00:00"
}
```

#### **GET /users/{id}**
Busca usuário por ID. **Requer ownership** (usuário só pode acessar próprio perfil).

**Response:** `200 OK` | `403 Forbidden` | `404 Not Found`

#### **PUT /users/{id}**
Atualiza nome do usuário. **Requer ownership**.

**Request:**
```json
{
  "name": "João Silva Santos"
}
```

**Response:** `200 OK` | `403 Forbidden` | `404 Not Found`

#### **DELETE /users/{id}**
Soft delete do usuário. **Requer ownership**.

**Response:** `204 No Content` | `403 Forbidden` | `404 Not Found`

---

### Board Members

#### **POST /boards/{id}/members**
Adiciona membro ao board (por email). **Requer role OWNER ou ADMIN**.

**Request:**
```json
{
  "email": "novo@member.com"
}
```

**Response:** `201 Created` | `403 Forbidden` | `404 Not Found` | `409 Conflict`

**Validações:**
- Usuário com email deve existir
- Não pode adicionar membro duplicado
- Apenas OWNER ou ADMIN pode adicionar

#### **DELETE /boards/{id}/members/{userId}**
Remove membro do board. **Requer role OWNER ou ADMIN**.

**Response:** `204 No Content` | `403 Forbidden` | `404 Not Found`

---

### Tasks

#### **POST /tasks**
Cria nova task. **Requer ser membro do board**.

**Request:**
```json
{
  "title": "Implementar autenticação",
  "description": "Adicionar JWT auth",
  "status": "PENDING",
  "priority": "HIGH",
  "dueDate": "2026-06-30",
  "boardId": "board-uuid-here",
  "assignedUserId": "user-uuid-here"
}
```

**Campos opcionais:** `description`, `status` (default: PENDING), `dueDate`, `assignedUserId`

**Response:** `201 Created` | `400 Bad Request` | `403 Forbidden`

#### **GET /tasks?boardId={id}&status={status}&assignedUserId={id}&search={text}**
Lista tasks com filtros opcionais. **Implementado com JPA Specifications**.

**Query Parameters (todos opcionais):**
- `boardId` (UUID) - filtra por board
- `status` (PENDING|IN_PROGRESS|DONE|CANCELLED) - filtra por status
- `assignedUserId` (UUID) - filtra por responsável
- `search` (string) - busca em title e description (case-insensitive)

**Exemplo:**
```
GET /tasks?boardId=550e8400-e29b-41d4-a716-446655440000&status=PENDING&search=bug
```

**Response:** `200 OK`
```json
[
  {
    "id": "task-uuid",
    "title": "Fix bug login",
    "description": "...",
    "status": "PENDING",
    "priority": "HIGH",
    "dueDate": "2026-06-30",
    "boardId": "board-uuid",
    "assignedUserId": "user-uuid",
    "createdAt": "2026-06-16T10:00:00",
    "updatedAt": "2026-06-16T10:00:00"
  }
]
```

#### **PATCH /tasks/{id}**
Atualização parcial de task. **Requer ser membro do board**.

**Request:** (todos os campos são opcionais)
```json
{
  "title": "Novo título",
  "description": "Nova descrição",
  "status": "IN_PROGRESS",
  "priority": "MEDIUM",
  "dueDate": "2026-07-15",
  "assignedUserId": "outro-user-uuid"
}
```

**Response:** `200 OK` | `403 Forbidden` | `404 Not Found`

#### **DELETE /tasks/{id}**
Deleta task. **Requer ser membro do board**.

**Response:** `204 No Content` | `403 Forbidden` | `404 Not Found`

---

### Task Comments

#### **POST /tasks/{taskId}/comments**
Adiciona comentário à task. **Requer ser membro do board**.

**Request:**
```json
{
  "text": "Precisamos revisar isso antes do deploy"
}
```

**Response:** `201 Created`
```json
{
  "id": "comment-uuid",
  "text": "Precisamos revisar isso antes do deploy",
  "authorId": "user-uuid",
  "authorName": "João Silva",
  "createdAt": "2026-06-16T14:30:00"
}
```

#### **GET /tasks/{taskId}/comments**
Lista todos os comentários da task. Ordenado por `createdAt ASC`.

**Response:** `200 OK`

#### **DELETE /comments/{commentId}**
Deleta comentário. **Apenas o autor pode deletar**.

**Response:** `204 No Content` | `403 Forbidden` | `404 Not Found`


---

## 🚀 Como Rodar

### Pré-requisitos

- **Java 21** ou superior
- **Maven 3.9+**
- **Docker** e **Docker Compose**

### 1. Clone o repositório

```bash
git clone <repository-url>
cd taskmanager
```

### 2. Suba o banco de dados PostgreSQL

```bash
docker compose up -d
```

Isso cria um container PostgreSQL 16 com:
- **Database:** `taskmanager`
- **User:** `taskuser`
- **Password:** `taskpass`
- **Port:** `5432`

Verifique se está rodando:
```bash
docker compose ps
```

### 3. Configure o JAVA_HOME (se necessário)

```bash
# Para macOS ARM (M1/M2/M3)
export JAVA_HOME=/path/to/sapmachine-jdk-21.0.4.jdk/Contents/Home

# Ou use o Java padrão do sistema (certifique-se que é Java 21)
java -version
```

### 4. Execute a aplicação

```bash
./mvnw spring-boot:run
```

**Na primeira execução:**
- Liquibase aplicará automaticamente as 6 migrations (V1 a V6)
- Todas as tabelas e índices serão criados
- Aplicação estará pronta para uso

### 5. Acesse

| URL | Descrição |
|-----|-----------|
| `http://localhost:8080` | API Base |
| `http://localhost:8080/swagger-ui.html` | Documentação interativa (Swagger UI) |
| `http://localhost:8080/v3/api-docs` | OpenAPI JSON spec |

### 6. Verifique o banco de dados

```bash
docker exec -it taskmanager-db psql -U taskuser -d taskmanager

# Dentro do psql:
\dt                          # Lista todas as tabelas
\d users                     # Describe da tabela users
SELECT * FROM databasechangelog;  # Vê migrations aplicadas
```

### Parar tudo

```bash
docker compose down          # Para o container
docker compose down -v       # Para e remove volumes (APAGA DADOS)
```

---

## 🔐 Autenticação

### Fluxo JWT

1. **Registro:** `POST /api/v1/users` (sem autenticação)
2. **Login:** `POST /api/v1/auth/login` → retorna JWT token
3. **Uso:** Adicione o token no header de todas as requisições:
   ```
   Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ```
4. **Expiração:** Token válido por 24 horas (configurável em `application.properties`)

### Configuração JWT

Em `application.properties`:

```properties
jwt.secret=${JWT_SECRET:defaultDevSecretThatShouldBeOverridden1234567890SecretKeyLongEnough}
```

**⚠️ IMPORTANTE:** Em produção, defina a variável de ambiente `JWT_SECRET` com uma chave segura!

```bash
export JWT_SECRET="sua-chave-super-secreta-com-no-minimo-256-bits"
```

---

## 📖 Exemplos de Uso

### Fluxo Completo: Criar Board → Adicionar Membro → Criar Task → Comentar

#### 1. Registrar dois usuários

```bash
# Usuário 1 (owner do board)
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice",
    "email": "alice@example.com",
    "password": "senha123"
  }'

# Usuário 2 (será adicionado como member)
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Bob",
    "email": "bob@example.com",
    "password": "senha123"
  }'
```

#### 2. Alice faz login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@example.com",
    "password": "senha123"
  }'

# Response: { "token": "eyJ..." }
# Salvar token como TOKEN_ALICE
```

#### 3. Alice cria um board (precisa implementar endpoint ainda)

**Nota:** Este endpoint ainda não existe. Você precisaria criar `BoardController` com `POST /boards`.

#### 4. Alice adiciona Bob ao board

```bash
curl -X POST http://localhost:8080/api/v1/boards/{BOARD_ID}/members \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN_ALICE" \
  -d '{
    "email": "bob@example.com"
  }'
```

#### 5. Alice cria uma task

```bash
curl -X POST http://localhost:8080/api/v1/tasks \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN_ALICE" \
  -d '{
    "title": "Implementar login social",
    "description": "Adicionar Google OAuth",
    "priority": "HIGH",
    "boardId": "{BOARD_ID}",
    "assignedUserId": "{BOB_USER_ID}"
  }'

# Response: { "id": "task-uuid", ... }
```

#### 6. Bob comenta na task

```bash
# Bob faz login primeiro
TOKEN_BOB=$(curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"bob@example.com","password":"senha123"}' | jq -r '.token')

# Bob adiciona comentário
curl -X POST http://localhost:8080/api/v1/tasks/{TASK_ID}/comments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN_BOB" \
  -d '{
    "text": "Vou começar hoje mesmo!"
  }'
```

#### 7. Alice atualiza status da task

```bash
curl -X PATCH http://localhost:8080/api/v1/tasks/{TASK_ID} \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN_ALICE" \
  -d '{
    "status": "IN_PROGRESS"
  }'
```

#### 8. Listar tasks do board

```bash
curl -X GET "http://localhost:8080/api/v1/tasks?boardId={BOARD_ID}&status=IN_PROGRESS" \
  -H "Authorization: Bearer $TOKEN_ALICE"
```


---

## Estrutura do Projeto

```
taskmanager/
├── src/main/java/com/unisinos/taskmanager/
│   ├── controller/              # REST Controllers
│   │   ├── AuthController.java          # Login/Logout
│   │   ├── UserController.java          # User CRUD
│   │   ├── BoardController.java         # Board member management
│   │   ├── TaskController.java          # Task CRUD + filters
│   │   └── TaskCommentController.java   # Comments
│   │
│   ├── service/                 # Business Logic
│   │   ├── UserService.java
│   │   ├── BoardService.java
│   │   ├── TaskService.java
│   │   └── TaskCommentService.java
│   │
│   ├── repository/              # JPA Repositories
│   │   ├── UserRepository.java
│   │   ├── BoardRepository.java
│   │   ├── BoardMemberRepository.java
│   │   ├── TaskRepository.java
│   │   └── TaskCommentRepository.java
│   │
│   ├── model/                   # JPA Entities
│   │   ├── User.java
│   │   ├── Board.java
│   │   ├── BoardMember.java
│   │   ├── Task.java
│   │   ├── TaskComment.java
│   │   └── enums/               # Enumerations
│   │       ├── BoardRole.java       # OWNER, ADMIN, MEMBER
│   │       ├── TaskStatus.java      # PENDING, IN_PROGRESS, DONE, CANCELLED
│   │       └── TaskPriority.java    # LOW, MEDIUM, HIGH
│   │
│   ├── dto/                     # Data Transfer Objects
│   │   ├── LoginRequestDTO.java
│   │   ├── AuthResponseDTO.java
│   │   ├── UserRegisterDTO.java
│   │   ├── UserUpdateDTO.java
│   │   ├── UserResponseDTO.java
│   │   ├── AddMemberDTO.java
│   │   ├── TaskCreateDTO.java
│   │   ├── TaskUpdateDTO.java
│   │   ├── TaskResponseDTO.java
│   │   ├── TaskCommentCreateDTO.java
│   │   └── TaskCommentResponseDTO.java
│   │
│   ├── security/                # JWT & Spring Security
│   │   ├── SecurityConfig.java      # Security configuration
│   │   ├── JwtService.java          # JWT token generation/validation
│   │   └── JwtAuthenticationFilter.java  # Filter to validate JWT
│   │
│   ├── util/                    # Utilities
│   │   └── SecurityUtils.java       # Get authenticated user ID
│   │
│   ├── exception/               # Custom Exceptions
│   │   ├── UserNotFoundException.java
│   │   ├── DuplicateEmailException.java
│   │   └── UnauthorizedException.java
│   │
│   └── TaskmanagerApplication.java  # Main Spring Boot class
│
├── src/main/resources/
│   ├── application.properties   # Main configuration
│   └── db/changelog/            # Liquibase migrations
│       ├── db.changelog-main.yaml           # Master changelog
│       └── migrations/
│           ├── V1__create_users.yaml
│           ├── V2__create_boards.yaml
│           ├── V3__create_board_members.yaml
│           ├── V4__create_tasks.yaml
│           ├── V5__create_task_comments.yaml
│           └── V6__create_indexes.yaml
│
├── src/test/java/               # Tests
│   └── com/unisinos/taskmanager/
│       └── TaskmanagerApplicationTests.java
│
├── docker-compose.yaml          # PostgreSQL container
├── pom.xml                      # Maven dependencies
└── README.md                    # Este arquivo
```

---
