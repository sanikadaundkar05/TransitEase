# TransitEase - Smart Urban Transit Pass System

TransitEase is an end-to-end digital public transit pass and fare management system. It provides commuter pass application, document verification, stage-gate approval, instant digital pass issuance with PDF downloads, pass renewal, in-app notifications, and comprehensive administrative controls for routes and transit personnel.

---

## 🏗️ Architecture

```
TransitEase/
├── backend/       # Spring Boot 3.4.1 (Java 21) REST API + Spring Security + JWT
└── frontend/      # React 19 + Vite single-page application with plain CSS
```

- **Backend**: Spring Boot 3.4.1, Java 21, Spring Data JPA, Spring Security, JWT (`jjwt`), iText PDF Generator, MySQL (Production/Dev), H2 In-Memory (Automated Tests).
- **Frontend**: React 19, Vite, Plain CSS design system with responsive layouts, transit-themed color palette, and zero unnecessary runtime dependencies.

---

## 🚀 Prerequisites

1. **Java Development Kit (JDK)**: Java 21 or higher.
2. **Node.js**: v18.0.0 or higher (v20+ recommended) with `npm`.
3. **Database**: MySQL 8.x running locally or remotely (port 3306).

---

## 🔧 Backend Setup & Execution

### 1. Configure Environment Variables
The backend reads database credentials and the JWT secret from environment variables or sensible local defaults in [`backend/src/main/resources/application.properties`](backend/src/main/resources/application.properties):

| Environment Variable | Default (Local Dev) | Description |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/transitease?createDatabaseIfNotExist=true` | MySQL JDBC Connection URL |
| `DB_USERNAME` | `root` | MySQL Database Username |
| `DB_PASSWORD` | `root` | MySQL Database Password |
| `JWT_SECRET` | `404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970` | 256-bit Hex JWT Signing Key |

### 2. Run the Backend Server
From the project root:
```bash
cd backend
.\mvnw.cmd spring-boot:run
```
*(On Linux/macOS: `./mvnw spring-boot:run`)*

The backend will start on **`http://localhost:8080`**. Database tables will be automatically updated with nullable schema changes (`ddl-auto=update`), and default seed accounts will be populated.

### 3. Run Backend Test Suite
The automated test suite runs against an isolated in-memory H2 database (`test` profile) and never affects existing MySQL data:
```bash
cd backend
.\mvnw.cmd test
```
**Current Test Status**: **154 / 154 Tests Passing (BUILD SUCCESS, 0 Failures, 0 Errors)**.

---

## 🚌 PMPML Pass Categories & Document Requirements

Pass selection is organized into 4 official PMPML commuter groups with Indian Rupee (INR) pricing and configurable document checklists:

### A. General Passenger Passes
- **PMC & PCMC Area Daily Pass**: ₹70 (24-hour validity). No document upload required.
- **All-PMPML / PMRDA Area Daily Pass**: ₹150 (24-hour validity). No document upload required.
- **General Monthly Pass**: ₹1,500 (30 days). Mandatory: Application form, Government photo ID, Recent photo. Optional: Residence proof.

### B. Student Passes
- **Monthly Student Pass**: ₹750 (30 days). Mandatory: School/college ID or bonafide certificate, Recent photo. Optional: Fee receipt, Age proof, Parent/institution verification.
- **Quarterly Student Pass**: ₹3,000 (90 days).
- **Half-Yearly Student Pass**: ₹4,000 (180 days).
- **Annual Student Pass**: ₹5,000 (365 days).
- **Student Punching Pass**: Configurable stage-based concession coupons as per municipal transport rules.

### C. Senior Citizen Passes
- **Senior Citizen Monthly Pass**: Fare on confirmation (configurable). Mandatory: Government-issued age/identity proof.
- **Senior Citizen Daily / Concessional Pass**: Fare on confirmation. Mandatory: Government age proof.

### D. Other Concessionary Passes
- **Person with Disability Pass**: Configurable subsidized fare. Mandatory: Government ID, Disability certificate / UDID card.
- **Visually Impaired Passenger Pass**: 100% concessional / free travel. Mandatory: Government ID, Visual impairment certificate.
- **Journalist / Reporter Pass**: Media quota fare. Mandatory: Press accreditation or journalist ID.
- **Freedom Fighter Pass**: 100% honorary waiver. Mandatory: Official freedom fighter pension certificate.

---

## 💻 Frontend Setup & Execution

### 1. Install Dependencies
From the project root:
```bash
cd frontend
npm install
```

### 2. Configure Environment Variable
A sample `.env.example` is provided:
```env
VITE_API_BASE_URL=http://localhost:8080
```
Create a `.env` file in the `frontend/` directory if you need to point to a different API host (defaults to `http://localhost:8080` if omitted).

### 3. Start Frontend Development Server
```bash
cd frontend
npm run dev
```
The application will be available at **`http://localhost:5173`**.

### 4. Build for Production
To validate and generate optimized production bundles:
```bash
cd frontend
npm run build
```
Build output is saved to `frontend/dist/`.

---

## 👥 Seeded Demo Accounts

You can test every role in the system immediately using pre-seeded accounts or the quick-fill buttons on the login screen:

| Role | Email | Password | Access Level |
|---|---|---|---|
| **System Admin** | `admin@transitease.com` | `admin123` | Full access to reports, staff onboarding, routes, categories, and applications |
| **Verification Officer** | `verifier@transitease.com` | `verifier123` | Review `PENDING` applications, inspect uploaded documents, verify or reject with remarks |
| **Approval Officer** | `officer@transitease.com` | `officer123` | Review `VERIFIED` applications, issue approval (generates digital pass) or reject with remarks |
| **Passenger** | *Self-Register* | *Your Password* | Apply for passes, upload documents, view passes, download PDFs, renew, track notifications |

---

## 🔄 End-to-End Workflow

1. **Registration & Login**:
   - Commuter signs up on the **Passenger Registration** tab.
   - Commuter logs in and receives a JWT token stored securely in `localStorage` and dispatched in all requests as `Authorization: Bearer <token>`.
2. **Browsing Routes & Applying**:
   - Commuter browses active bus routes, stops, and single-journey fares in ₹.
   - Commuter selects a pass category (e.g., Monthly Student Pass) and reviews the dynamic document checklist.
   - Attaches supporting eligibility documents (PDF, JPG, PNG <= 5MB) or submits directly if no documents are required (Daily pass).
   - Application is created in `PENDING` status.
3. **Document Verification**:
   - Verification Officer (`verifier@transitease.com`) logs into the **Officer Portal**.
   - Officer inspects uploaded documents and clicks **Verify** (enters verification remarks). Status updates to `VERIFIED`.
   - Direct approval before verification is strictly prevented by both frontend rules and backend service guards.
4. **Final Approval & Digital Pass Issuance**:
   - Approval Officer (`officer@transitease.com`) reviews the verified application and clicks **Approve**.
   - The backend automatically:
     - Sets application status to `APPROVED`.
     - Issues a unique **Digital Pass** (e.g., `PASS-2026-XXXX`).
     - Emits an **in-app notification** to the passenger.
5. **Pass Download & Renewal**:
   - Passenger opens **My Digital Passes** on the Passenger Portal.
   - Passenger clicks **Download PDF** to download the official TransitEase PDF pass directly to their computer.
   - Once nearing expiration, passenger clicks **Renew Pass** to initiate a renewal application while preserving complete audit history.
6. **Administration & Analytics**:
   - Admin logs in to view live summary metrics (pass distribution, application funnel, category revenue in ₹, user counts).
   - Admin can onboard staff, adjust user roles, manage routes, toggle line availability, and configure pass categories and document requirements.

---

## 🛡️ Security & CORS Configuration

- **Protected Endpoints**: All operational endpoints enforce `@PreAuthorize` role-based access control.
- **CORS Handling**: Configured in `SecurityConfig.java` to allow frontend development origins (`http://localhost:5173`, `http://localhost:3000`) with supported HTTP verbs (`GET`, `POST`, `PUT`, `DELETE`, `PATCH`, `OPTIONS`) and exposed `Content-Disposition` header for file downloads.
- **Database Safety**: Schema updates use safe, nullable Hibernate update mode (`spring.jpa.hibernate.ddl-auto=update`). Existing MySQL records and tables are preserved.
