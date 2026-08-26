# System Design: Healthcare Appointment System

## 1. Concurrency and Double-Booking Prevention
One of the most critical requirements of any healthcare scheduling system is the absolute prevention of double-booking slots. Relying purely on application-level locks (`@Lock`) can lead to race conditions across multiple JVM instances in a clustered environment or fail when locks timeout unexpectedly.

To guarantee zero double-bookings, this system utilizes a **Database-Level Unique Constraint** combined with **Optimistic Concurrency Control**.
*   **Unique Index**: A unique composite index (`UNIQUE (doctor_profile_id, appointment_datetime)`) is applied directly to the `appointments` table via Flyway migration. 
*   **Execution**: When two patients attempt to click "Hold Slot" for the exact same slot concurrently, both transactions execute asynchronously. The database itself enforces uniqueness at the storage layer. 
*   **Resolution**: The first transaction to commit successfully acquires the slot. The second transaction hits a `DataIntegrityViolationException`, which the application catches and surfaces gracefully to the user as a "Slot no longer available" error. This ensures a 100% fail-safe mechanism against double-booking without introducing heavy pessimistic locks that could degrade system throughput.

## 2. Temporary Slot Holds & Cart Expiration
When a patient selects a slot, they must fill out a triage questionnaire (symptoms) and review the booking details. To prevent frustration where a slot is snatched away while the patient is typing, the system implements a **Temporary Slot Hold** pattern (similar to ticket reservation systems).

*   **Hold State**: Clicking a slot transitions it to the `HELD` status and assigns an `expires_at` timestamp (current time + 10 minutes).
*   **Completion**: If the user completes the booking form within 10 minutes, the status transitions to `SCHEDULED` and `expires_at` is nullified.
*   **Expiration (Abandonment)**: If the user closes the tab or abandons the flow, the hold naturally expires. A recurring JobRunr background cron job (`@PostConstruct` schedule: `* * * * *`) scans the database for any appointments in the `HELD` state where `expires_at < NOW()`. These rows are automatically soft-deleted or hard-deleted, freeing the slot for other patients to book immediately.

## 3. Leave Handling & Doctor Availability
Doctors require flexible scheduling, including standard weekly hours and dynamic, ad-hoc leave days (e.g., sickness, vacation).

*   **Availability**: A `doctor_availabilities` table stores generic recurring week-day patterns (e.g., Monday 09:00 - 17:00).
*   **Leave Overrides**: A separate `doctor_leaves` table stores specific dates a doctor is unavailable.
*   **Slot Generation Algorithm**: The `SlotService` calculates available slots in real-time. When a patient views a specific date, the algorithm first checks the `doctor_leaves` table. If a leave exists for that date, it immediately returns an empty array. If no leave exists, it generates slots based on `doctor_availabilities` and the doctor's `slot_duration_mins`. It then queries the `appointments` table and subtracts any `HELD` or `SCHEDULED` slots, returning only genuinely free inventory to the frontend.

## 4. Background Workers & Notification Failures
The system heavily integrates with external APIs: Google Calendar (for event syncing), OpenRouter (for LLM symptom/note processing), and SMTP (for Email notifications). Blocking the main HTTP request thread while waiting for these external services would severely degrade perceived application performance and risk request timeouts.

*   **Asynchronous Execution**: We implemented the **Outbox / Background Worker** pattern using **JobRunr**. Actions like "Cancel Appointment" immediately commit the database state change and then enqueue a background job (`BackgroundJob.enqueue(...)`) to send the cancellation email and delete the Google Calendar event. The HTTP response is returned to the user instantly.
*   **Failure Recovery & Retries**: External APIs fail (network blips, rate limits). JobRunr provides robust, automatic exponential backoff retries for failed jobs. If an email fails to send due to an SMTP timeout, it doesn't crash the booking flow. The job fails independently in the background, is flagged for retry, and eventually succeeds without user intervention.
*   **Idempotency**: Jobs are designed idempotently. The Google Calendar API utilizes the unique `appointment_id` as part of its event payload to prevent duplicate calendar events.
*   **Medication Reminders**: A cron-based background job executes every minute to scan the `medication_reminders` table for entries that are currently due (`reminder_time <= NOW()` and active). To ensure patients don't receive duplicate emails if the cron runs twice in the same minute, the job performs an idempotency check by verifying and updating the `last_notified_at` timestamp before sending the email.
