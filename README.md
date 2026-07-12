# 🚗 Traction — Car Dealership Inventory System

Traction is a high-fidelity, full-stack car dealership inventory management system. Designed using strict **Test-Driven Development (TDD)** vertical slices, it features secure JWT-based role authorization (USER and ADMIN), responsive catalogs, purchase transaction security, and direct Cloudinary media uploads.

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| **Backend** | Spring Boot 3.5.16, Java 23, JPA/Hibernate, Flyway Migration |
| **Database** | PostgreSQL 16 (dev/prod), Testcontainers PostgreSQL (testing) |
| **Authentication** | Spring Security 6 + JJWT (Json Web Token) |
| **Media Storage** | Cloudinary SDK (Image Uploads under `traction/vehicles` folder) |
| **Frontend** | React 19, JavaScript, Vite, Tailwind CSS v3, Axios, React Hot Toast |
| **Testing (Backend)** | JUnit 5, Mockito, MockMvc, Testcontainers (87/87 tests passed) |
| **Testing (Frontend)** | Vitest, React Testing Library, jsdom (14/14 tests passed) |
| **Containerization** | Docker, Docker Compose |
| **Coverage Analysis** | JaCoCo Maven Plugin |

---

## 🚀 Getting Started

### 📋 Prerequisites
* Java 21+ / Java 23 (Recommended)
* Node.js 20+
* Docker & Docker Compose (for PostgreSQL container)
* Maven 3.9+

---

## 🔧 Installation & Setup

### 1. Database Setup
Spin up the local PostgreSQL database using Docker Compose:
```bash
docker-compose up -d
```
This boots up a PostgreSQL instance mapped to `localhost:5432` with database `traction_db`.

### 2. Cloudinary Setup
To enable image uploads, set the following environment variables on your system (or add them to `backend/src/main/resources/application-dev.yml`):
```properties
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret
```

### 3. Run Backend
Navigate to the backend directory and boot the Spring Boot application:
```bash
cd backend
mvn spring-boot:run
```
The server will start on `http://localhost:8080`.
* **Swagger OpenAPI Documentation**: `http://localhost:8080/swagger-ui.html`

### 4. Run Frontend
Navigate to the frontend directory, install packages, and boot the Vite development server:
```bash
cd frontend
npm install
npm run dev
```
The client will start on `http://localhost:5173`. Proxies configured in `vite.config.js` forward `/api` requests to the backend automatically.

---

## 🧪 Test Execution & Coverage Reports

### Backend Tests
Execute unit and database container integration tests:
```bash
cd backend
mvn test
```
The reports are compiled via **JaCoCo**. You can open the interactive coverage report in any browser:
* **Report Path**: `backend/target/site/jacoco/index.html`

### Frontend Tests
Execute Vitest test suites:
```bash
cd frontend
npx vitest run
```

---

## 📡 API Reference Mapping

### Authentication (`/api/auth`)
* `POST /api/auth/register` — Create a new customer account (USER role)
* `POST /api/auth/login` — Sign in and retrieve JWT Bearer token

### Showroom Vehicles (`/api/vehicles`)
* `GET /api/vehicles` — Paginated search/listings with dynamic filtering
* `GET /api/vehicles/{id}` — Fetch detailed info for a single vehicle
* `POST /api/vehicles` — Create a new listing (**ADMIN only**)
* `PUT /api/vehicles/{id}` — Update details for a listing (**ADMIN only**)
* `DELETE /api/vehicles/{id}` — Remove a listing (**ADMIN only**)
* `PATCH /api/vehicles/{id}/status` — Transition stock status (**ADMIN only**)
* `POST /api/vehicles/{id}/image` — Attach image file and upload to Cloudinary (**ADMIN only**)

### Showroom Inventory (`/api/vehicles/{id}`)
* `POST /api/vehicles/{id}/purchase` — Instantly buy a vehicle (transitions status to SOLD, persisted record)
* `POST /api/vehicles/{id}/restock` — Restock a SOLD/RESERVED vehicle (**ADMIN only**)

---

## 🤖 AI Usage & Pair Programming Reflection

### What AI Accelerated
AI pair programming (via Antigravity and Cursor Composer) dramatically accelerated writing boilerplate components, repetitive mock configurations in tests, and Flyway migration SQL code. Defining precise REST specs and database schemas took seconds, allowing us to focus on the logical integration of the vertical slices.

### Correction of Incorrect AI Suggestions
During test configuration, Cursor generated an `argLine` parameter override in `pom.xml` to specify timezones, which unintentionally deleted JaCoCo's JVM agent instrumentation properties. We noticed that JaCoCo was skipping report generation due to a "missing execution data file." We manually resolved this by prepending `@{argLine}` to preserve the test coverage instrumentation.

### Code Ownership Maintenance
Code ownership was strictly maintained by enforcing the RED/GREEN/REFACTOR cycle. Every test suite was compiled and run to confirm failure (RED) before any production code was written. We reviewed each vertical slice design, kept entity relationship scopes focused (using lazy loading properties), and verified that no unauthorized AI tags were attached to config/initialization commits.

---

## 📄 License
This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
