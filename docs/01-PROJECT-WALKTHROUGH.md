# 01 – Project Walkthrough

> Imagine it's your first day. Your lead says: "Clone the repo, run it, and understand the leave flow
> by end of day." This document is that onboarding.

## 1. The business requirement (what the client asked for)

> **Epic: LMS-1 – Leave Management System**
> HR wants to stop tracking leave in Excel. Employees should apply for leave online.
> Managers should approve or reject. Every employee gets 20 days of leave per year.
> Approved leave must be deducted from the balance automatically.

**Users:** Employee (applies), Manager (approves/rejects), HR (adds employees).

**Business rules (from the requirement):**
1. End date cannot be before start date.
2. Start date cannot be in the past.
3. Cannot apply for more days than the remaining balance.
4. Only a PENDING leave can be approved or rejected.
5. On approval, the days are deducted from the balance.
6. Email must be unique per employee.

## 2. Folder structure (and why)

```
backend/src/main/java/com/company/leave/
├── LeaveManagementApplication.java   ← starts the app
├── controller/   ← HTTP layer: URLs, request/response, @Valid
├── service/      ← BUSINESS LOGIC: rules, calculations, @Transactional
├── repository/   ← DB layer: Spring Data JPA interfaces
├── entity/       ← Java classes mapped to DB tables
├── dto/          ← objects the API sends/receives (never expose entities)
├── exception/    ← custom exceptions + GlobalExceptionHandler
└── config/       ← CORS, sample data
```

This is called **layered architecture**: `Controller → Service → Repository → Database`.
Each layer has ONE job. If the DB changes, only the repository changes. If a rule changes, only the service changes.

## 3. Follow one request end-to-end (MOST IMPORTANT SECTION)

Request: **Approve leave #7** → `PUT /api/leaves/7/approve`

```
Browser/Postman
   │  PUT /api/leaves/7/approve
   ▼
LeaveController.approve(7)            ← matches URL via @PutMapping("/{id}/approve")
   │  calls
   ▼
LeaveService.approve(7)               ← @Transactional starts a DB transaction
   │  1. leaveRepository.findById(7)   → SELECT ... FROM leave_requests WHERE id=7
   │  2. not found?  throw ResourceNotFoundException
   │  3. not PENDING? throw BusinessException
   │  4. enough balance? else throw BusinessException
   │  5. employee.balance -= days; leave.status = APPROVED
   │  6. method ends → transaction COMMITS → Hibernate runs 2 UPDATE statements
   ▼
LeaveResponse (DTO) → converted to JSON by Jackson → 200 OK
```

If any exception is thrown in the service → the transaction ROLLS BACK (nothing saved)
→ `GlobalExceptionHandler` turns the exception into a clean JSON error with 400/404.

**Do this now:** put a breakpoint in `LeaveService.approve`, run in Debug mode, call the API from Swagger,
and step through line by line. Watch the SQL printed in the console. This one exercise teaches more than 10 videos.

## 4. Each layer explained

### Entity (`Employee`, `LeaveRequest`)
- **What:** a Java class = a DB table. Each object = one row.
- **Key annotations:** `@Entity`, `@Id`, `@GeneratedValue`, `@Column`, `@ManyToOne`, `@Enumerated(STRING)`, `@PrePersist`.
- **Interview point:** "I used `@ManyToOne(fetch = LAZY)` from LeaveRequest to Employee so we don't load the employee unless needed, and `EnumType.STRING` so reordering the enum never corrupts data."

### Repository (`EmployeeRepository`, `LeaveRequestRepository`)
- **What:** interfaces extending `JpaRepository`. Spring generates the implementation.
- **Derived queries:** `existsByEmail`, `findByEmployeeIdOrderByCreatedAtDesc` — SQL is built from the method name.
- **Interview point:** "For simple queries I use derived methods; for complex ones I use `@Query` with JPQL or native SQL."

### DTO (`EmployeeRequest`, `LeaveApplyRequest`, `*Response`, `ErrorResponse`)
- **Why not return the entity?**
  1. Security – client can't set `id` or `leaveBalance`.
  2. API stays stable when the DB table changes.
  3. Avoids lazy-loading / infinite JSON loop problems.
- **Interview point:** "Entities stay inside the service layer. Controllers only work with DTOs. I map using a static `from()` method; in bigger projects we used MapStruct."

### Service (`EmployeeService`, `LeaveService`)
- **What:** all business rules. The only layer with `@Transactional`.
- **Constructor injection** instead of `@Autowired` on fields → dependencies are `final`, easy to mock in tests.
- **Dirty checking:** in `approve()` we never call `save()`. Inside a transaction, Hibernate tracks loaded entities and saves changes automatically on commit.
- **Interview point:** "I keep `@Transactional` on service methods, because one business operation like approve updates two tables and both must succeed or both roll back."

### Controller (`EmployeeController`, `LeaveController`)
- **What:** maps URLs to methods; `@Valid` triggers validation; returns proper status codes (201 for create).
- **Rule:** zero business logic here. If you see an `if` about business rules in a controller, it belongs in the service.

### Validation
- Annotations on DTOs: `@NotBlank`, `@Email`, `@NotNull`, `@FutureOrPresent`, `@Size`.
- Rules that need the DB or compare two fields (balance, end ≥ start) are checked in the **service**.
- **Interview point:** "Field-level validation with Bean Validation on the DTO; business validation in the service; both return the same error JSON format."

### Exception handling (`GlobalExceptionHandler`)
- `@RestControllerAdvice` catches exceptions from all controllers.
- `ResourceNotFoundException → 404`, `BusinessException → 400`, validation → 400 with field details, anything else → 500 with a safe message (full stack trace only in logs).

### Logging
- SLF4J `log.info` for important business events (created, approved), `log.warn` for expected failures, `log.error` with stack trace for unexpected ones.
- Use `{}` placeholders: `log.info("Leave approved id={}", id)` — never string concatenation.

## 5. Tests (in `backend/src/test`)

| Test class                 | Type         | What is mocked         | Speed  |
|----------------------------|--------------|------------------------|--------|
| `LeaveServiceTest`         | Unit         | Repository, EmployeeService | ms |
| `EmployeeServiceTest`      | Unit         | Repository             | ms     |
| `LeaveControllerTest`      | Web slice (`@WebMvcTest`) | Service   | ~1s    |
| `LeaveFlowIntegrationTest` | Integration (`@SpringBootTest`) | Nothing (real H2 DB) | few s |

**What to mock and why:** in a unit test, mock everything *outside* the class you are testing,
so a failing test points at exactly one class. Never mock the class under test. Don't mock DTOs/entities – just create them.

**Test naming:** `method_condition_expectedResult` e.g. `approve_alreadyApproved_throwsBusinessException`.

## 6. Frontend (React)

```
frontend/src/
├── api.js                     ← ALL backend calls in one place
├── App.jsx                    ← holds state (employees, leaves, message), loads data
└── components/
    ├── EmployeeSection.jsx    ← add + list employees
    ├── LeaveForm.jsx          ← apply leave
    └── LeaveTable.jsx         ← list leaves + approve/reject buttons
```

Flow: component calls `api.applyLeave()` → `fetch` to Spring Boot → on success reload lists; on error show `message` from the backend's `ErrorResponse`.
