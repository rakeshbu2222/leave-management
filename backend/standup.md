# Sprint 1 Standup

## Day 1

### Yesterday

* Completed initial project setup.
* Verified the existing Spring Boot application.
* Ran the existing automated test suite.

### Today

* Verify MySQL 8 setup.
* Create the `leavedb` database.
* Set up Flyway database migrations.
* Create the initial database migration.
* Configure the application to use MySQL.

### Blockers

* None.

---

## Day 4 — LM-107

### Yesterday

* Completed LM-105: Employee email case-insensitive validation.
* Merged LM-105 changes into `main`.
* Created the LM-107 branch from the updated `main`.

### Today

* Reproduced the LM-107 invalid ID issue.
* Verified that invalid numeric path/query parameters were incorrectly returning HTTP 500.
* Added regression tests for invalid employee/leave IDs and unsupported HTTP methods.
* Updated `GlobalExceptionHandler` to handle `MethodArgumentTypeMismatchException` and return HTTP 400.
* Updated `GlobalExceptionHandler` to handle `HttpRequestMethodNotSupportedException` and return HTTP 405.
* Verified that invalid IDs do not reach the service layer.
* Ran the complete automated test suite: **23 tests passed, 0 failures, 0 errors**.
* Manually verified the LM-107 scenarios through Swagger.
* Confirmed that the invalid ID and unsupported HTTP method scenarios now return the expected HTTP status codes.

### Blockers

* None.

---

## Day 5 — LM-102

### Yesterday
- Merged the LM-105/LM-107 cleanup PR.

### Today
- Added V2 Flyway migration for the optional `phone_number` column.
- Added phoneNumber to Employee, EmployeeRequest (@Pattern 10 digits), EmployeeResponse, service and UI.
- 4 existing unit tests failed to compile (record constructor changed); fixed by passing null.
- Added 4 new tests; 27 tests passing.
- Verified V2 on MySQL (flyway_schema_history shows versions 1 and 2).

### Blockers
- None.
