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

## Day 6 — LM-104: Get Leave by ID + Update Employee

### Yesterday

* Continued LM-102 employee phone number implementation.
* Added and verified `phoneNumber` changes across the employee DTO, entity and application flow.
* Addressed LM-102 review comments.
* Verified the LM-102 branch is pushed to the remote repository.

### Today

* Verify and complete the LM-102 merge into `main` before starting LM-104.
* Create branch `feature/LM-104-get-leave-update-employee`.
* Implement `GET /api/leaves/{id}`:

  * Return `200` when the leave exists.
  * Return `404` when the leave does not exist.
  * Refactor the existing leave lookup into reusable `findLeave()`.
  * Use `@Transactional(readOnly = true)` to handle the lazy-loaded employee safely.
* Implement `PUT /api/employees/{id}`:

  * Update name, email, department and phone number.
  * Reuse existing validation.
  * Normalize email by trimming spaces and converting it to lowercase.
  * Prevent duplicate email while allowing an employee to keep their own email.
  * Return `404` for an unknown employee ID.
  * Preserve `id` and `leaveBalance`.
* Add unit and integration tests.
* Run `mvn clean test` and verify all **37 tests pass**.
* Perform Swagger and MySQL manual verification.
* Add the required PUT-without-phoneNumber experiment result to the PR notes.
* Push the LM-104 branch and open a PR without merging.

### Blockers

* LM-102 currently needs to be merged into `main` before LM-104 can be branched from the updated `main`.
* No other blocker currently identified.

### Expected Outcome

* `GET /api/leaves/{id}` implemented and tested.
* `PUT /api/employees/{id}` implemented and tested.
* Existing approve/reject behavior remains unchanged.
* All **37 tests** pass.
* Swagger and MySQL verification completed.
* LM-104 branch pushed and PR opened for review without merging.


