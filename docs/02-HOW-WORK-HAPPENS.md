# 02 – How Real Work Happens in a Company

## 1. The sprint cycle (2 weeks, Scrum)

| When           | Meeting              | What you do / say |
|----------------|----------------------|-------------------|
| Day 1          | Sprint planning      | Team picks tickets. You give estimates in story points (1, 2, 3, 5, 8). |
| Every day, 15m | Daily standup        | "Yesterday I finished LMS-12 and raised the PR. Today I'll fix review comments and start LMS-13. No blockers." |
| Anytime        | Backlog grooming     | Ask questions on unclear tickets BEFORE the sprint starts. |
| Last day       | Sprint review / demo | Show the feature working (Swagger/UI). |
| Last day       | Retrospective        | What went well, what didn't. |

## 2. Life of one ticket (do this for EVERY ticket in the backlog)

```
To Do → In Progress → Code Review → QA → Done
```

1. **Read the ticket fully.** Note acceptance criteria. Ask doubts in the ticket comments.
2. **Impact analysis:** which classes change? (entity? DTO? service? DB? frontend?) Write it in a comment.
3. **Create branch** from latest main.
4. **Code** → layer by layer: entity → repository → DTO → service → controller → frontend.
5. **Write/update tests** – positive, negative, edge case.
6. **Run everything locally:** `mvn test`, start the app, test in Swagger/Postman, test the UI.
7. **Commit, push, raise PR.**
8. **Fix review comments**, push again (same branch; PR updates automatically).
9. **Merge** after approval + green CI build.
10. Move ticket to **QA**, add "How to test" in the ticket comment.

## 3. Git commands you will use daily

```bash
# one-time: put this project on GitHub
git init
git add .
git commit -m "LMS-1: Initial leave management project"
git branch -M main
git remote add origin https://github.com/<your-username>/leave-management.git
git push -u origin main

# for EVERY ticket
git checkout main
git pull                                   # always start from latest code
git checkout -b feature/LMS-12-cancel-leave
# ... code ...
git status                                 # see what changed
git diff                                   # review your own changes before committing!
git add .
git commit -m "LMS-12: Add cancel leave API"
git push -u origin feature/LMS-12-cancel-leave
# → open GitHub → "Compare & pull request"

# after review comments
git add . && git commit -m "LMS-12: Address review comments" && git push

# main moved ahead while you worked? update your branch:
git checkout main && git pull
git checkout feature/LMS-12-cancel-leave
git merge main        # fix conflicts if any, then commit + push
```

**Branch names:** `feature/LMS-12-short-name`, `bugfix/LMS-40-short-name`, `hotfix/LMS-55-short-name` (urgent prod fix).
**Commit message:** always start with the ticket id.

## 4. Pull Request description template

```
## Ticket
LMS-12 – Cancel leave

## What changed
- Added PUT /api/leaves/{id}/cancel
- Added CANCELLED status
- If leave was APPROVED, days are credited back to balance

## How to test
1. Apply leave, approve it → balance 20 → 17
2. PUT /api/leaves/{id}/cancel → status CANCELLED, balance back to 20
3. Cancel again → 400

## Tests
- LeaveServiceTest: 4 new cases (pending, approved, already cancelled, not found)
```

## 5. Common code review comments (learn to avoid them)

| Reviewer says | What it means / fix |
|---------------|---------------------|
| "Move this logic to service layer" | You put business logic in the controller. |
| "Don't return the entity, use a DTO" | Controller returned `LeaveRequest` directly. |
| "Missing test for the negative case" | Add a test where the rule fails. |
| "Magic number 20" | Move to a constant or `application.properties` (`@Value`). |
| "Use constructor injection" | Replace field `@Autowired`. |
| "Don't log sensitive data" | Remove passwords/tokens/PII from logs. |
| "This will cause N+1 queries" | Use `JOIN FETCH` / `@EntityGraph`. |
| "Add @Transactional" | Method updates multiple rows; must be atomic. |
| "Use Optional properly" | Don't call `.get()` without checking; use `orElseThrow`. |
| "Unused import / commented code" | Clean up before raising PR. |

How to reply: fix it, then reply "Done" or explain politely if you disagree ("I kept it here because ... – happy to change if you prefer").

## 6. Environments & deployment (what happens after merge)

```
Your laptop (local) → DEV → QA/SIT → UAT (client tests) → PROD
```

- **CI (Jenkins / GitHub Actions):** on every PR, runs `mvn clean verify` (compile + tests). Red build = cannot merge.
- **CD:** after merge, builds a JAR/Docker image and deploys to DEV automatically; QA/UAT/PROD usually need approval.
- **Config per environment:** `application-dev.properties`, `application-prod.properties`, chosen with `spring.profiles.active`.
- **Production bug?** Check logs (Kibana/Splunk/CloudWatch) → reproduce locally → fix on a `hotfix/` branch → PR → deploy → verify.
