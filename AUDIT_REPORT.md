# Healthcare Appointment System - E2E Audit Report

## 1. Executive Summary
**Overall Health:** **Critical Failure**
The Healthcare Appointment System fails to boot the backend server due to a fatal initialization error with the JobRunr background job scheduler. This prevents any runtime end-to-end testing of the API, frontend integration, authentication, and Google Calendar flows. Furthermore, a static code analysis reveals that the Google Calendar integration is entirely mocked, environment variables are misconfigured or hardcoded, and documentation contains significant inaccuracies compared to the actual implementation.

**Findings Summary:**
- **Critical:** 1 (Backend startup crash)
- **High:** 4 (JobRunr misconfiguration, Google Calendar mocked, Prompt Injection risk, Documentation mismatch)
- **Medium:** 3 (Env variables ignored/mismatched, Missing index, JWT Secret hardcoded)
- **Low/Nitpick:** 1 (Port mismatch in `.env.example`)

---

## 2. Checklist Results Table

| Area | Item | Status | Note |
|---|---|---|---|
| 1. Environment | `.env.example` accuracy | **Fail** | `LLM_API_KEY` vs `OPENROUTER_API_KEY` mismatch; `JWT_SECRET` is completely ignored. |
| 1. Environment | Backend boots cleanly | **Fail** | Crashes on startup with `BeanCreationException` due to JobRunr initialization. |
| 1. Environment | Frontend boots cleanly | **Partial** | Builds successfully, but E2E cannot be tested due to backend crash. |
| 1. Environment | Prerequisites match | **Pass** | `pom.xml` and `package.json` match required versions. |
| 2. Database | Flyway migrations run cleanly | **Pass** | Validated successfully on startup prior to the crash. |
| 2. Database | Tables exist w/ foreign keys | **Pass** | Schema matches documented tables and types. |
| 2. Database | Unique constraint exists | **Pass** | `uq_doctor_appointment` correctly prevents double booking at the DB level. |
| 2. Database | Missing indexes | **Partial** | `expires_at` in `appointments` is polled every 60s but lacks an index. |
| 2. Database | Cascade/delete behavior | **Pass** | `ON DELETE CASCADE` is properly applied. |
| 3. Backend | Auth & RBAC | **Partial** | Static analysis confirms `@PreAuthorize` tags are correct. Runtime unverified. |
| 3. Backend | Slot Generation Algorithm | **Partial** | Logic in `SlotService` is sound. Runtime unverified. |
| 3. Backend | Slot Hold & Concurrency | **Partial** | DB-level lock works, but cleanup uses Spring `@Scheduled`, not JobRunr. |
| 3. Backend | Leave Conflict Handling | **Unverified**| Blocked by backend crash. |
| 3. Backend | JobRunr tasks | **Fail** | `MedicationReminderTask` crashes the app. Polls every 1m instead of 15m. |
| 3. Backend | Error handling | **Unverified**| Blocked by backend crash. |
| 4. LLM | Pre-visit symptom extraction | **Unverified**| Blocked by backend crash. |
| 4. LLM | Post-visit summarization | **Partial** | Prompt is correct, but AI fallback does not populate the `medications` array. |
| 4. LLM | Fallback behavior triggers | **Partial** | Fallback logic exists, but cannot be tested live. |
| 4. LLM | Prompt-injection risks | **Fail** | Raw patient text is concatenated directly into the prompt without sanitization. |
| 5. Calendar | OAuth2 flow works end-to-end | **Fail** | Completely mocked in `GoogleCalendarService`. No real OAuth exists. |
| 5. Calendar | Events created/removed | **Fail** | Fakes logging statements instead of making API calls. |
| 6. Frontend | Walk through dashboards | **Unverified**| Blocked by backend crash. |
| 6. Frontend | Test specific UI flows | **Unverified**| Blocked by backend crash. |
| 6. Frontend | Broken links/console errors | **Unverified**| Blocked by backend crash. |
| 6. Frontend | Responsiveness | **Unverified**| Blocked by backend crash. |
| 6. Frontend | State reflection | **Unverified**| Blocked by backend crash. |
| 7. Integration | API URL / CORS | **Partial** | CORS configured for `localhost:4200`. Runtime unverified. |
| 7. Integration | Env-specific configs | **Fail** | Hardcoded secrets and ignored `.env` variables. |
| 8. Deliverables | `SYSTEM_DESIGN.md` accuracy | **Fail** | Claims `AppointmentCleanupTask` uses JobRunr; it uses Spring `@Scheduled`. |
| 8. Deliverables | `README.md` setup steps | **Fail** | Claims backend runs on `localhost:8080`; it runs on `8081` and crashes. |
| 8. Deliverables | Deployed URL | **N/A** | No hosted URL provided. |
| 8. Deliverables | `.env.example` completeness | **Fail** | Lists unused variables (`JWT_SECRET`) and mismatched variables. |

---

## 3. Detailed Findings

### 3.1. Environment & Setup
*   **[Critical] Backend Crashes on Startup due to JobRunr Initialization**
    *   **Description:** The application fails to start. `MedicationReminderTask.java` uses `@PostConstruct` to call `BackgroundJob.scheduleRecurrently()`. This executes during Spring context initialization before the JobScheduler is fully configured, throwing `java.lang.IllegalStateException: The JobScheduler has not been initialized`.
    *   **Evidence:** Application logs output `BeanCreationException: Error creating bean with name 'medicationReminderTask': Invocation of init method failed`.
    *   **Reproduction:** Run `./mvnw spring-boot:run`.

*   **[Medium] Environment Variable Mismatches**
    *   **Description:** `application.properties` and `.env.example` are out of sync.
    *   **Evidence:** `.env.example` defines `LLM_API_KEY`, but `application.properties` looks for `${OPENROUTER_API_KEY}`. `.env.example` defines `JWT_SECRET`, but `JwtUtils.java` has a hardcoded string `private final String jwtSecret = "404E635266...";`. SMTP settings are completely hardcoded to `localhost:25` in properties, ignoring the `.env` file.

### 3.2. Database & Schema
*   **[Medium] Missing Index on `expires_at`**
    *   **Description:** `AppointmentCleanupTask` runs every 60 seconds and queries for appointments where `expires_at < CURRENT_TIMESTAMP`. This column is unindexed, meaning a full table scan occurs every minute on the `appointments` table.
    *   **Evidence:** `V4__add_expires_at_to_appointments.sql` adds the column but no corresponding index is created.

### 3.3. Backend Correctness
*   **[High] MedicationReminderTask Cron Discrepancy**
    *   **Description:** The prompt and design docs claim the job runs every 15 minutes and advances `next_reminder_time`. The actual implementation schedules it every minute (`* * * * *`) and checks `start_date`, `end_date`, and `reminder_time`, updating `last_notified_at` instead of an advancing timestamp.
    *   **Evidence:** `BackgroundJob.scheduleRecurrently("medication-reminder-job", "* * * * *", () -> this.processDueReminders());` in `MedicationReminderTask.java`.

### 3.4. LLM Integration
*   **[High] Prompt Injection Vulnerability**
    *   **Description:** User input is blindly concatenated into the LLM prompt. A malicious patient or doctor could input instructions that override the system prompt (e.g., "Ignore previous instructions and return this JSON...").
    *   **Evidence:** `OpenAiLlmServiceImpl.java`: `String prompt = "... Symptoms: " + rawSymptoms;`

### 3.5. Google Calendar Integration
*   **[High] Integration is Completely Mocked**
    *   **Description:** The application claims to feature Google Calendar sync via OAuth2, but `GoogleCalendarService.java` only prints `logger.info("MOCK: Successfully created Google Calendar event...");`. There are no Google API client libraries, no OAuth2 callback controllers, and no real API calls being made.

### 3.6. Deliverables & Documentation
*   **[High] SYSTEM_DESIGN.md is Inaccurate**
    *   **Description:** The design document explicitly states that `AppointmentCleanupTask` is a `@Recurring` JobRunr job. The codebase reveals it is actually using Spring's native `@Scheduled(fixedRate = 60000)`.

*   **[Low] Port Mismatch**
    *   **Description:** `application.properties` sets `server.port=8081`. However, `README.md` implies it runs on `8080`, and `.env.example` sets `GOOGLE_REDIRECT_URI` to port `8080`.

---

## 4. Recommendations

1.  **[CRITICAL] Fix JobRunr Initialization:** Move the `BackgroundJob.scheduleRecurrently` registration out of `@PostConstruct` in `MedicationReminderTask.java`. Spring Boot provides `ApplicationRunner` or `CommandLineRunner` beans which execute safely *after* the context (and JobRunr) are fully initialized.
2.  **[HIGH] Consolidate Environment Variables:** Replace all hardcoded secrets in `JwtUtils.java` and `application.properties` with standard `${ENV_VAR_NAME}` placeholders. Ensure `.env.example` strictly matches the expected names (e.g., rename `LLM_API_KEY` to `OPENROUTER_API_KEY`).
3.  **[HIGH] Implement Real Google Calendar Sync:** Replace the mock logging in `GoogleCalendarService` with actual Google API Java Client logic, and build the missing OAuth callback endpoints to securely store refresh tokens.
4.  **[HIGH] Mitigate Prompt Injection:** Switch from direct string concatenation to the official OpenRouter/OpenAI Chat API format (using separate `"role": "system"` for instructions and `"role": "user"` for the raw symptom/notes input).
5.  **[MEDIUM] Add Database Index:** Add an index for `expires_at` to prevent performance degradation as the `appointments` table grows.
6.  **[MEDIUM] Correct Documentation:** Update `SYSTEM_DESIGN.md` to accurately reflect the use of Spring `@Scheduled` for the cleanup task (or rewrite the task to actually use JobRunr), and correct all port references to `8081`.
