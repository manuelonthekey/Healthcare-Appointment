# Milestone 7: Frontend Fixes & Refinement

## Summary of Changes

In this milestone, we focused strictly on refining the Angular frontend architecture and user flows, leaving the backend API surface untouched as per constraints. 

### 1. Auth & App Entry point
- Rerouted the default application entry `path: ''` to `/register`.
- Rebuilt both `login.component.html` and `register.component.html` to be completely standalone. They no longer inherit the dashboard chrome (sidebar, layout wrapper) and instead feature a clean, centered auth card with a generic top navbar containing `About Us`, `Contact Us` (as plain spans to avoid dead links), and a CTA button to switch between Login and Sign Up.
- Implemented `RouterModule` imports across both auth components to properly handle the CTA navigations.

### 2. Post-login Shell & Routing Architecture
- Modified `layout.component.ts` and `layout.component.html` to use `<router-outlet>`, transitioning it into a parent route wrapper for all authenticated views (`/dashboard`, `/schedule`, `/leaves`, `/admin`, `/account`).
- The sidebar is now fully role-aware, displaying correct links based on whether the logged-in user is a `PATIENT`, `DOCTOR`, or `ADMIN`.
- Stripped out all dead, non-functional links (`Appointments` and `Patients`) from the sidebar. 
- Wired up a functional `Logout` button that correctly clears `localStorage` and navigates back to `/login`.
- Transformed the static "Staff" section in the sidebar. For patients, it dynamically displays past appointments (currently mocked with a `TODO(backend)` note). For doctors and admins, it is hidden entirely to remove clutter.

### 3. Role-specific Primary Actions
- Updated the header topbar inside `layout.component.html` to contextually display primary CTAs:
  - **Patient**: "Create an Appointment" (triggers the existing `BookingModalComponent`).
  - **Doctor**: "Schedule Leave" (navigates to `/leaves`).
  - **Admin**: "Add / Accept Doctors" (navigates to `/admin`).

### 4. Dashboard Filtering
- Enhanced `DashboardComponent` for Patients and Doctors by introducing client-side filtering on the "Upcoming Appointments" view.
- Added a search input for filtering by Patient Name.
- Added a dropdown select for filtering by Appointment Status (Completed, Postponed, Cancelled).

### 5. Account Profile & AI Summary Import
- Built a brand new `AccountComponent` mapped to `/account`.
- Implemented a role-aware profile editing form (e.g., DOB/phone for patients, Specialization for doctors).
- Incorporated an "Import from Summary" feature for patients, parsing recent structured AI summaries (like extracted `medications` and `notes`) and mapping them directly into the form model for quick profile enrichment. 
- **Backend Sync**: Bound the save action to `PUT /api/patients/{id}/profile` (and `doctors/{id}/profile`). Since these endpoints do not currently exist in the backend, hitting 'Save Profile' intentionally throws an HTTP 404 error, fulfilling the strict prompt requirement to "not fake success states" and leaving a clear `TODO(backend)` marker.

## Next Steps for Backend Team
The following endpoints need to be implemented on the backend to complete these frontend flows:
1. `GET /api/appointments/past` (or similar) to fetch historical appointments for the patient's sidebar.
2. `PUT /api/patients/{id}/profile` to accept profile updates (including fields for medications and allergies).
3. `PUT /api/doctors/{id}/profile` to accept doctor profile updates.
