# Healthcare Appointment System

A modern, full-stack Healthcare Appointment System featuring intelligent LLM-driven symptom analysis, automated medication reminders, and robust concurrency handling. Built with Angular (Frontend) and Spring Boot (Backend).

## Features

- **Role-Based Access Control (RBAC):** Distinct dashboards for Patients, Doctors, and Administrators.
- **Smart Appointment Booking:** Prevents double-booking via robust database-level constraints.
- **Slot Holds & Expiration:** Reserves slots temporarily (10 mins) during checkout to prevent race conditions.
- **AI-Powered Diagnostics:** Integrates with OpenRouter (GPT-3.5) to summarize clinical notes and extract structured symptoms.
- **Automated Medication Reminders:** Daily cron jobs queue medication reminder emails to patients based on doctor prescriptions.
- **Google Calendar Integration:** Automatically syncs scheduled and cancelled appointments to the user's Google Calendar.

---

## 1. Prerequisites

- **Java 17+** (Backend)
- **Node.js 18+ & npm** (Frontend)
- **Maven** (Backend build tool)
- **H2 Database** (Configured automatically in-memory for local development)
- **OpenRouter API Key** (For LLM features)

---

## 2. Environment Setup

### Environment Variables (`.env`)
At the root of the project, create a `.env` file based on the provided `.env.example`:

```bash
cp .env.example .env
```
Ensure you fill in your `LLM_API_KEY` (OpenRouter API), `JWT_SECRET`, and SMTP details for email notification functionality.

### Database Schema
The database schema is managed automatically using **Flyway**.
When the Spring Boot backend starts, Flyway runs all SQL migrations in `backend/src/main/resources/db/migration/`.
The database initializes with standard tables: `users`, `doctor_profiles`, `patient_profiles`, `appointments`, `doctor_availabilities`, `doctor_leaves`, and `medication_reminders`.

---

## 3. Running the Backend (Spring Boot)

Navigate to the backend directory and run the application using the Maven wrapper:

```bash
cd backend
./mvnw clean install
./mvnw spring-boot:run
```
The API will be available at `http://localhost:8081`.

---

## 4. Running the Frontend (Angular)

Navigate to the frontend directory, install dependencies, and start the development server:

```bash
cd frontend
npm install
npm start
```
The Angular application will be available at `http://localhost:4200`.

---

## 5. Google Calendar Setup Instructions

To enable Google Calendar syncing:
1. Go to the [Google Cloud Console](https://console.cloud.google.com/).
2. Create a new project and navigate to **APIs & Services > Library**.
3. Enable the **Google Calendar API**.
4. Go to **Credentials** and click **Create Credentials > OAuth client ID**.
5. Set the Application Type to **Web application**.
6. Add `http://localhost:4200` to the **Authorized JavaScript origins**.
7. Add `http://localhost:8081/api/calendar/callback` to the **Authorized redirect URIs**.
8. Copy your **Client ID** and **Client Secret**.
9. Update `backend/src/main/resources/application.properties` (or your `.env`) with:
   - `google.client.id`
   - `google.client.secret`

---

## 6. Architecture & Concurrency Model

See [SYSTEM_DESIGN.md](./SYSTEM_DESIGN.md) for a detailed 800-word architectural deep dive explaining how the system prevents double-booking, manages database transactions, and handles background job failures.
