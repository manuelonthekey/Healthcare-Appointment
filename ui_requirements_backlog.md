# UI/UX Requirements Backlog

This document keeps track of all the user interface (UI) and user experience (UX) components that have been intentionally skipped during the backend and architectural phases. These will need to be developed once we shift focus to the presentation layer.

## Milestone 1: Authentication UI
* **Login Page:**
  * Clean form with Email and Password fields.
  * Error state visualizations (e.g., red text for "Invalid Credentials").
  * Redirection logic to route users to the correct dashboard based on their role (Admin vs. Doctor vs. Patient).
* **Registration Page:**
  * Form for new users to sign up (Email, Password, Role selection).
  * Password confirmation field and validation states.

## Milestone 2: Admin Dashboard & Doctor Profiles
* **Admin Dashboard:**
  * A data table or grid view listing all doctors.
  * A filter mechanism (e.g., tabs or dropdown) to quickly view "PENDING" vs "ACTIVE" doctors.
  * Action buttons (Approve/Reject) next to pending doctor profiles to trigger the status update.
* **Doctor Profile Setup:**
  * A profile form for doctors to enter their Name, Specialization, and preferred Slot Duration (e.g., 15m, 30m, 60m).
* **Doctor Availability Scheduler:**
  * A weekly schedule interface (checkboxes for Monday-Sunday) with time pickers for `start_time` and `end_time`.
* **Doctor Leave Management:**
  * A visual calendar (date picker) allowing doctors to click days they will be on leave, preventing patients from booking on those dates.
* **Patient Slot Viewer (Preview):**
  * A date picker for patients to select a day, triggering the display of dynamically generated time-slot chips (e.g., [ 09:00 AM ], [ 09:30 AM ]) based on the algorithm.
