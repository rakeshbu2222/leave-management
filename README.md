# Leave Management System (Java Full Stack)

A small, real-world style project: employees apply for leave, a manager approves or rejects it,
and the leave balance is updated.

**Tech stack** (the same one most Indian service/product companies use):

| Layer     | Technology                                           |
|-----------|------------------------------------------------------|
| Backend   | Java 17, Spring Boot 3, Spring Data JPA, Hibernate   |
| Database  | H2 (in-memory, zero setup) → MySQL later             |
| Frontend  | React 18 + Vite                                      |
| Testing   | JUnit 5, Mockito, MockMvc, Spring Boot Test          |
| API docs  | Swagger (springdoc-openapi)                          |
| Build     | Maven (backend), npm (frontend)                      |

---

## 1. Install these once

- **JDK 17 or 21** – check with `java -version`
- **Maven 3.9+** – check with `mvn -v` (IntelliJ also has Maven built in)
- **Node.js 18+** – check with `node -v`
- **IntelliJ IDEA Community** (backend) and **VS Code** (frontend)
- **Postman** (API testing)
- **Git** – check with `git --version`

## 2. Run the backend

```bash
cd backend
mvn test              # FIRST: run all tests. You should see "Tests run: 17, Failures: 0"
mvn spring-boot:run   # starts the API on http://localhost:8080
```

Or in IntelliJ: *File → Open → backend folder* → wait for Maven to load → run `LeaveManagementApplication.java`.

Open these once it is running:

- Swagger UI: http://localhost:8080/swagger-ui.html  ← try every API here
- H2 database console: http://localhost:8080/h2-console
  (JDBC URL: `jdbc:h2:mem:leavedb`, user `sa`, empty password) → run `SELECT * FROM EMPLOYEES;`

3 sample employees are inserted on startup (`DataLoader.java`).

## 3. Run the frontend

```bash
cd frontend
npm install
npm run dev           # opens on http://localhost:5173
```

## 4. APIs

| Method | URL                             | What it does                          |
|--------|---------------------------------|---------------------------------------|
| POST   | `/api/employees`                | Create employee                       |
| GET    | `/api/employees`                | List employees                        |
| GET    | `/api/employees/{id}`           | Get one employee                      |
| POST   | `/api/leaves`                   | Apply for leave                       |
| GET    | `/api/leaves?employeeId=1`      | List leaves (filter is optional)      |
| PUT    | `/api/leaves/{id}/approve`      | Approve leave (deducts balance)       |
| PUT    | `/api/leaves/{id}/reject`       | Reject leave                          |

Ready-made requests are in `api-requests.http` (open in IntelliJ and click ▶, or copy into Postman).

## 5. Switch to MySQL (do this in Sprint 2)

```sql
CREATE DATABASE leavedb;
```
Update the password in `backend/src/main/resources/application-mysql.properties`, then:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

## 6. Learning path – read in this order

1. `docs/01-PROJECT-WALKTHROUGH.md` – how a request flows through the code, layer by layer
2. `docs/02-HOW-WORK-HAPPENS.md` – Jira, standups, Git branches, PRs, code review, deployment
3. `docs/03-SPRINT-BACKLOG.md` – your tickets. Pick them up one by one like a real developer.
