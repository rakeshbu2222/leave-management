# 03 – Sprint Backlog (your Jira board)

Rules of the game:
- Do tickets **in order**. Each one follows the 10-step flow in `02-HOW-WORK-HAPPENS.md`.
- One branch + one PR per ticket, even though you work alone. This builds the habit.
- Every ticket needs tests. No test = PR rejected.
- **Hints show *where* to look, not the code.** Try for at least 1 hour before asking for help.
- When done, send the diff (or the changed classes) to Claude and say "review my PR for LMS-xx".
  You'll get review comments the way a tech lead writes them.

Story points: 1 = few hours, 2 = half day, 3 = one day, 5 = 2–3 days, 8 = most of a week.

---

## Sprint 0 – Onboarding (week 1)

### LMS-01 · Task · 2 pts · Set up and run the project
- Run `mvn test` (17 tests pass), start backend + frontend, try every request in `api-requests.http`.
- Push the project to your own GitHub repo (`main` branch).
- **Done when:** you can explain `PUT /api/leaves/{id}/approve` end-to-end without looking at the code.

### LMS-02 · Task · 1 pt · Debug the approve flow
- Put breakpoints in `LeaveController.approve` and `LeaveService.approve`, run in Debug mode, step through.
- Watch the SQL in the console. Note: how many SELECTs? how many UPDATEs? When do the UPDATEs run – at the line you changed, or when the method ends? Why?

---

## Sprint 1 – Small enhancements (week 2–3)

### LMS-11 · Story · 2 pts · Add phone number to employee
**As** HR, **I want** to store an employee's phone number.
**Acceptance criteria**
- `phoneNumber` is optional, but if given it must be exactly 10 digits.
- Returned in all employee APIs and shown in the UI table.
- Existing employees without a phone still work.
**Hint:** Entity → Request DTO (`@Pattern`) → Response DTO → Service (constructor) → UI. Which existing tests break? Why?

### LMS-12 · Story · 3 pts · Cancel a leave
**As** an employee, **I want** to cancel my leave.
**Acceptance criteria**
- New API: `PUT /api/leaves/{id}/cancel` → status `CANCELLED`.
- PENDING or APPROVED leave can be cancelled. If it was APPROVED, the days go back to the balance.
- Cannot cancel a REJECTED or already CANCELLED leave → 400.
- Cannot cancel a leave whose start date has already passed → 400.
- "Cancel" button in the UI.
**Hint:** `LeaveStatus`, `LeaveService`, `LeaveController`, `LeaveTable.jsx`. Look at why `findPendingLeave` can't be reused as-is.

### LMS-13 · Story · 1 pt · Get one leave by id
- `GET /api/leaves/{id}` → 200 with leave, 404 if not found. Controller test for both.

### LMS-14 · Bug · 2 pts · Duplicate employees with different email case
**Reported by QA:** "I created `ravi@company.com`, then `Ravi@Company.com` was also accepted. Now there are 2 Ravis."
**Expected:** Email is case-insensitive and stored in lowercase. Leading/trailing spaces removed.
**Steps:** reproduce in Postman first → find the root cause → fix → write a **regression test** that fails before your fix and passes after.

---

## Sprint 2 – Database & querying (week 4–5)

### LMS-21 · Task · 3 pts · Move to MySQL with Flyway migrations
- Install MySQL, run with the `mysql` profile.
- Add Flyway. Create `V1__create_tables.sql` with both tables. Change `ddl-auto` to `validate`.
- **Why:** in real projects nobody lets Hibernate change the production schema. Every DB change is a versioned SQL script reviewed in the PR.
- From now on, every ticket that changes a table needs a new `V<n>__*.sql` file.

### LMS-22 · Story · 3 pts · Pagination and sorting for leave list
- `GET /api/leaves?page=0&size=10&sort=startDate,desc`
- Response includes content, page number, total elements, total pages.
- Max page size 50 (if client asks for 1000, return 50).
- UI: Previous / Next buttons.
**Hint:** `Pageable`, `Page<T>`. Don't return Spring's `Page` object directly — make your own `PageResponse<T>` DTO. (Why? Ask in review.)

### LMS-23 · Story · 3 pts · Search / filter leaves
- `GET /api/leaves?status=PENDING&employeeId=2&from=2026-12-01&to=2026-12-31` – every filter optional, can be combined, still paginated.
**Hint:** 2⁴ = 16 combinations → you can't write 16 repository methods. Look at `JpaSpecificationExecutor` / `Specification`.

### LMS-24 · Bug · 3 pts · Employee can book more leave than they have  🔴 High priority
**Reported by HR:** "Arjun has 20 days. He applied for 3 leaves of 15 days each. All 3 were accepted as PENDING. The manager approved the first one and was confused by the other two."
**Expected:** When applying, the check must be: *requested days + days already PENDING ≤ balance*.
**Hint:** new repository query using `SUM`. Think about what `SUM` returns when there are no rows.

### LMS-25 · Bug · 3 pts · Overlapping leaves allowed
**Reported:** "Priya has two approved leaves for 10–12 Dec and 11–13 Dec."
**Expected:** Reject a new leave if it overlaps any PENDING or APPROVED leave of the same employee → 400 with the conflicting dates.
**Hint:** two ranges overlap when `newStart <= existingEnd AND newEnd >= existingStart`. Write edge-case tests: touching dates, same day, one inside the other.

---

## Sprint 3 – Business rules & design changes (week 6–7)

### LMS-31 · Story · 5 pts · Leave types
- Types: `CASUAL`, `SICK`, `UNPAID`. `leaveType` is required when applying.
- `UNPAID` leave does not use the balance.
- `SICK` leave longer than 2 days needs a `medicalCertificateUrl`, otherwise 400.
- **Discuss in your PR:** did you add a column, or a new table? Why? What happens to existing rows (Flyway migration default value)?

### LMS-32 · Story · 3 pts · Don't count weekends
- A leave from Friday to Monday is 2 days, not 4.
- A leave that is only Saturday + Sunday → 400 "No working days in selected range".
- Move the day calculation into its own small class (`WorkingDayCalculator`) with its own unit tests.

### LMS-33 · Story · 3 pts · Audit fields
- Every table gets `createdAt`, `updatedAt`, `createdBy`, `updatedBy`.
- Use Spring Data JPA Auditing (`@EnableJpaAuditing`, `@CreatedDate`, `@LastModifiedDate`, a `@MappedSuperclass BaseEntity`).
- For now `createdBy` = `"system"` (becomes the logged-in user in Sprint 4).

### LMS-34 · Story · 2 pts · Manager comment on reject  (API contract change!)
- `PUT /api/leaves/{id}/reject` now takes a body `{ "comment": "Project release that week" }`, comment required, max 500 chars.
- Returned in the leave response and shown in the UI.
- **Think:** the mobile team still calls the old API without a body. How do you avoid breaking them? (Look up API versioning, or make it backward compatible.) Write your decision in the PR.

---

## Sprint 4 – Security (week 8–9)

### LMS-41 · Story · 8 pts · Login with JWT and roles
- Add a `users` table (username, BCrypt password, role `EMPLOYEE`/`MANAGER`/`HR`, link to employee).
- `POST /api/auth/login` → returns JWT. All other APIs need `Authorization: Bearer <token>`.
- `EMPLOYEE`: apply/cancel own leave, see own leaves. `MANAGER`: approve/reject. `HR`: create employees.
- 401 for no/invalid token, 403 for wrong role.
- UI: login page, store token, send it in `api.js`, logout.

### LMS-42 · Story · 3 pts · Use the logged-in user
- Remove `employeeId` from the apply request – take it from the token.
- An employee calling `GET /api/leaves/5` for someone else's leave → 403.
- `createdBy` / `updatedBy` = logged-in username.

---

## Sprint 5 – Production & performance issues (week 10–11)

### LMS-51 · Bug · 3 pts · GET /api/leaves is slow  🔴 Production
**Reported by monitoring:** "p95 latency of GET /api/leaves went from 80ms to 4s after data grew to 50k leaves."
**Investigate:** turn on SQL logging, call the API, count the queries. (Hint: 1 query for leaves + 1 per employee = the **N+1 problem**.)
**Fix:** `JOIN FETCH` or `@EntityGraph`. Add a DB index on `employee_id` and `status` in a Flyway script. Write down before/after query counts in the PR.

### LMS-52 · Story · 2 pts · Cache employee lookups
- `GET /api/employees/{id}` is called very often and employees rarely change.
- Add `@Cacheable`, and `@CacheEvict` when an employee is updated. Prove with logs that the 2nd call doesn't hit the DB.

### LMS-53 · Bug · 5 pts · Balance went negative  🔴 Production
**Reported:** "Arjun's balance is −4. Logs show two managers approved two of his leaves at the same second."
**Root cause to find:** two transactions read balance=10 at the same time, both pass the check, both subtract.
**Fix:** optimistic locking with `@Version` on `Employee` + handle `ObjectOptimisticLockingFailureException` → 409 Conflict "Please retry".
**Test:** write a test that runs two approvals in parallel threads (`ExecutorService`) and asserts only one succeeds.

### LMS-54 · Story · 5 pts · Public holidays from external API
- Don't count public holidays as leave days. Get them from `https://date.nager.at/api/v3/PublicHolidays/{year}/IN`.
- Use `RestClient` (or `WebClient`), **connect timeout 2s, read timeout 3s**.
- If the external API is down, don't fail the leave – log a warning and count without holidays.
- Unit-test with a mocked client; never call the real API in tests.

### LMS-55 · Story · 5 pts · Email notification when leave is approved/rejected
- Must not slow down the approve API. Use `@Async` first.
- **Stretch:** publish a `LeaveStatusChangedEvent` to Kafka (run Kafka in Docker), with a consumer that "sends" the email (log it). What happens if the consumer fails? (Look up retries + dead-letter topic.)

---

## Sprint 6 – DevOps (week 12)

### LMS-61 · Task · 3 pts · Dockerize
- `Dockerfile` for backend, `docker-compose.yml` with backend + MySQL (+ frontend).
- `docker compose up` must start everything on a fresh machine.

### LMS-62 · Task · 3 pts · CI pipeline
- GitHub Actions workflow: on every PR run `mvn clean verify` and `npm run build`. PR can't merge if red.
- Add JaCoCo; fail the build if service-layer coverage < 80%.

### LMS-63 · Task · 2 pts · Health & config for production
- Add Spring Boot Actuator (`/actuator/health`). `application-prod.properties` with DB password from an environment variable (never commit secrets).

---

## After every sprint – interview practice

Tell Claude: "Sprint N is done, interview me on it." You'll get the questions an interviewer would ask about that work,
and you answer as if it was your job (e.g. *"Tell me about a production issue you fixed"* → LMS-53).
