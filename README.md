# MediTicketApp — Desktop Frontend

## Project Overview

MediTicketApp is the desktop client for the MediTicket healthcare management system. Built with **Java Swing**, **FlatLaf**, and **MigLayout**, it provides role-specific dashboards for Patients, Doctors, Nurses, and Administrators.

The app communicates with the [MediTicket2 Spring Boot backend](https://github.com/JoshBlack25/MediTicket2) over a local REST API.

---

## Technology Stack

| Technology | Version / Notes |
|---|---|
| Java | 22 |
| Java Swing | Desktop UI framework |
| FlatLaf | 3.7.1 — modern look and feel |
| MigLayout | 11.3 — responsive layout manager |
| Jackson | 2.18.9 — JSON serialization |
| LGoodDatePicker | 11.2.1 — date input fields |
| TwelveMonkeys ImageIO | 3.13.1 — quality image rendering |
| modal-dialog | 2.6.1 — toast and modal feedback |
| Maven | 3.9+ (system install required — no `mvnw`) |
| maven-shade-plugin | 3.5.2 — produces executable fat JAR |

---

## Building and Running

### Prerequisites

- Java 22 JDK
- Maven 3.9+ installed on your system (`mvn` available in PATH)
- The [MediTicket2 backend](https://github.com/JoshBlack25/MediTicket2) running locally on port 8080

### Build the fat JAR

```powershell
cd MediTicketApp
mvn package "-Dmaven.test.skip=true"
```

This produces: `target/cput-1.0-SNAPSHOT.jar`

### Run the app

```powershell
java -jar target\cput-1.0-SNAPSHOT.jar
```

The login window will open. The backend **must be running first** or the app will show network errors.

> For the full step-by-step launch guide covering both backend and frontend, see [`docs/LAUNCH.md`](../docs/LAUNCH.md).

---

## Backend Connection

The app connects to:

```
http://localhost:8080/api
```

This is configured in `BaseApiClient.java`. The backend must be running on port 8080 before launching the app.

---

## Architecture

```
src/main/java/za/ac/cput/
├── api/
│   ├── BaseApiClient         HTTP client (GET/POST/PUT/PATCH/DELETE + JWT auth)
│   ├── ApiClientProvider     Singleton — provides typed clients per domain entity
│   └── clients/              One client class per backend entity
│
├── model/domain/             Frontend representations of backend domain objects
│   ├── Appointment
│   ├── ClinicStaff
│   ├── Doctor
│   ├── Notification
│   ├── Patient
│   ├── PatientTicket
│   ├── Payment
│   └── ...
│
├── session/
│   └── SessionManager        Singleton — holds logged-in user state (token, role, name)
│
├── ui/
│   ├── auth/                 Login and registration screens
│   ├── patient/              Patient dashboard and pages
│   ├── doctor/               Doctor dashboard and pages
│   ├── clinicstaff/
│   │   ├── admin/            Admin dashboard and pages
│   │   └── nurse/            Nurse dashboard and pages
│   ├── layout/               Shared Sidebar, TopHeader, NavItem
│   └── theme/                AppTheme colours, FontManager
│
└── Main.java                 Entry point — initialises FlatLaf and opens the login window
```

---

## Authentication & Sessions

1. User logs in via `LoginPanel` — credentials sent to `POST /api/auth/login`
2. Backend returns JWT access token + refresh token + user type
3. `SessionManager` stores: `userId`, `userType`, `staffRole`, `accessToken`, `refreshToken`, `fullName`, `email`
4. `BaseApiClient.setAuthToken()` attaches `Authorization: Bearer <token>` to all subsequent requests
5. `LoginPanel` then fetches the user's full name based on their role (Patient / Doctor / Clinic Staff)
6. Routing: `PATIENT` → `PatientDashboard`, `DOCTOR` → `DoctorDashboard`, `CLINIC_STAFF+ADMIN` → `AdminDashboard`, `CLINIC_STAFF+NURSE` → `NurseDashboard`

---

## Role-Based Dashboards

### Patient Dashboard
- Appointments — book, view, cancel
- Tickets — view consultation tickets
- Payments — view payment history
- Notifications — view and mark as read
- Profile — view account details

### Doctor Dashboard
- Appointments — view scheduled appointments
- Tickets — view and complete consultations
- Patients — view patient list
- Notifications — view and mark as read
- Profile — view account details

### Admin Dashboard (Clinic Staff — Admin role)
- Appointments — manage all appointments
- Tickets — create and manage patient tickets
- Patients — view patient list
- Staff — manage clinic staff
- Notifications — view and mark as read
- Profile — view account details

### Nurse Dashboard (Clinic Staff — Nurse role)
- Appointments — view appointments
- Tickets — view and manage assigned tickets
- Notifications — view and mark as read
- Profile — view account details

---

## Key Flows

| Flow | Who | How |
|---|---|---|
| Book appointment | Patient | `BookAppointmentDialog` → `POST /api/appointments` |
| Create ticket | Nurse/Admin | `CreateTicketDialog` → `POST /api/patient-tickets` |
| Complete consultation | Doctor | `CompleteConsultationDialog` → `PATCH /api/patient-tickets/{id}/status` |
| Generate payment | Nurse/Admin | `GeneratePaymentDialog` → `POST /api/payments` |
| View notifications | All roles | Notification page → `GET /api/notifications/user/{id}` |
| Mark notification read | All roles | Notification action → `PATCH /api/notifications/{id}/read` |

All API calls run on a `SwingWorker` thread to keep the UI responsive.

---

## Team Members

| Team Member            | Student Number | UI Area                          |
|------------------------|----------------|----------------------------------|
| Joshua Reid Adams      | 230317693      | PatientTicket UI, Auth (Team Lead) |
| Abdullahi Raage Farah  | 230971091      | Payments UI, TicketStatus UI     |
| Aidan Barends          | 230255639      | Patient UI, Registration         |
| Jaden Clayton Abrahams | 222206721      | Doctor UI, Doctor Dashboard      |
| Joshua Peter Bonzet    | 221312536      | Appointments UI                  |
| Matthew Barron         | 230398863      | Clinic Staff UI, Admin Dashboard |
| Raul Jaaim Everts      | 230270565      | Notifications UI                 |

---

## Development Workflow

```bash
# 1. Pull latest
git checkout main && git pull origin main

# 2. Switch to your branch
git checkout <your-branch>

# 3. Make changes

# 4. Build and test locally
mvn package "-Dmaven.test.skip=true"
java -jar target\cput-1.0-SNAPSHOT.jar

# 5. Commit and push
git add .
git commit -m "feat: describe your change"
git push origin <your-branch>

# 6. Open a Pull Request to main
```

---

## Related

- **Backend (REST API):** [MediTicket2](https://github.com/JoshBlack25/MediTicket2)
- **Full launch guide:** [`docs/LAUNCH.md`](../docs/LAUNCH.md)
