# 🎓 CampusHub — College Event Management Platform

A full-stack digital platform that simplifies event registration, scheduling, and participant management for college programs and activities.

---

## 🏗️ Tech Stack

| Layer       | Technology                          |
|-------------|-------------------------------------|
| Backend     | Java 17 · Spring Boot 3.2           |
| Security    | Spring Security · JWT (HS256)       |
| Database    | MySQL 8 · Spring Data JPA           |
| Frontend    | React 18 · Vite · Tailwind CSS      |
| State       | Zustand · React Query               |
| Forms       | React Hook Form · Zod               |
| API Docs    | Swagger / OpenAPI 3                 |
| Testing     | Postman Collection included         |

---

## 📁 Project Structure

```
college-event-platform/
├── backend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/college/events/
│       │   ├── config/          # SecurityConfig, AppConfig
│       │   ├── controller/      # AuthController, EventController, RegistrationController
│       │   ├── dto/             # Request/Response DTOs
│       │   ├── entity/          # User, Event, Registration
│       │   ├── exception/       # GlobalExceptionHandler + custom exceptions
│       │   ├── repository/      # JPA repositories with custom JPQL
│       │   ├── security/        # JWT filter, UserDetails
│       │   └── service/         # Business logic
│       └── resources/
│           └── application.properties
├── frontend/
│   ├── package.json
│   └── src/
│       ├── components/
│       │   ├── auth/            # LoginForm, RegisterForm
│       │   ├── events/          # EventCard, CreateEventForm
│       │   └── shared/          # Navbar, Layout, etc.
│       ├── pages/               # EventsPage, EventDetailPage, Dashboard
│       ├── services/            # api.js (Axios + interceptors)
│       ├── store/               # authStore.js (Zustand)
│       └── App.jsx
├── database/
│   └── schema.sql               # Tables + indexes + seed data
└── docs/
    └── College_Events_API.postman_collection.json
```

---

## 🚀 Getting Started

### 1. Database Setup (MySQL Workbench)
```sql
-- Open MySQL Workbench → New Query → paste and run:
SOURCE /path/to/college-event-platform/database/schema.sql;
```

### 2. Backend (IntelliJ IDEA)
```bash
# 1. Open backend/ folder in IntelliJ as a Maven project
# 2. Edit src/main/resources/application.properties:
#    Set your MySQL password + Gmail credentials

# 3. Run the application:
mvn spring-boot:run
# OR click the green ▶ button in IntelliJ

# Swagger UI available at:
# http://localhost:8080/swagger-ui.html
```

### 3. Frontend (VS Code)
```bash
cd frontend
npm install
npm run dev
# Opens at http://localhost:5173
```

### 4. Test APIs (Postman)
```
1. Open Postman
2. Import → docs/College_Events_API.postman_collection.json
3. Run "Login (auto-save token)" request first
4. TOKEN variable is auto-populated for all subsequent requests
```

---

## 🔑 Default Test Accounts

| Email                      | Password      | Role    |
|----------------------------|---------------|---------|
| admin@college.edu          | password123   | ADMIN   |
| priya.sharma@college.edu   | password123   | FACULTY |
| rahul.verma@college.edu    | password123   | STUDENT |

> ⚠️ Change all passwords before deploying to any real environment!

---

## 🌐 API Endpoints Summary

### Auth
| Method | Endpoint                    | Auth | Description              |
|--------|-----------------------------|------|--------------------------|
| POST   | /api/v1/auth/register       | ❌   | Create account           |
| POST   | /api/v1/auth/login          | ❌   | Get JWT tokens           |
| POST   | /api/v1/auth/refresh        | ❌   | Refresh access token     |
| POST   | /api/v1/auth/forgot-password| ❌   | Send reset email         |
| POST   | /api/v1/auth/reset-password | ❌   | Reset with token         |

### Events
| Method | Endpoint                    | Auth       | Description              |
|--------|-----------------------------|------------|--------------------------|
| GET    | /api/v1/events              | ❌         | Browse events            |
| GET    | /api/v1/events/:id          | ❌         | Event details            |
| GET    | /api/v1/events/search?q=    | ❌         | Full-text search         |
| POST   | /api/v1/events              | FACULTY+   | Create event             |
| PUT    | /api/v1/events/:id          | FACULTY+   | Update event             |
| DELETE | /api/v1/events/:id          | FACULTY+   | Delete/cancel event      |
| POST   | /api/v1/events/:id/banner   | FACULTY+   | Upload banner image      |

### Registrations
| Method | Endpoint                                        | Auth       | Description           |
|--------|-------------------------------------------------|------------|-----------------------|
| POST   | /api/v1/registrations/events/:id               | ✅         | Register for event    |
| DELETE | /api/v1/registrations/:id                      | ✅         | Cancel registration   |
| GET    | /api/v1/registrations/my                       | ✅         | My registrations      |
| GET    | /api/v1/registrations/events/:id/participants  | FACULTY+   | Event participants    |
| POST   | /api/v1/registrations/check-in?token=          | FACULTY+   | QR check-in           |

---

## ✨ Key Features

- **JWT Auth** with auto-refresh — tokens rotate silently without logout
- **Role-based access** — STUDENT / FACULTY / ADMIN with method-level security
- **Event search & filters** — category, status, full-text across title/description/tags
- **Capacity management** — auto-tracks registered count, blocks over-capacity
- **QR Check-in** — unique per-registration token for attendance tracking
- **Email notifications** — verification, reset, and registration confirmation
- **File uploads** — event banners stored locally (swap for S3 in production)
- **Swagger UI** — interactive API docs at `/swagger-ui.html`
- **React Query** — smart caching, background refetch, optimistic updates
- **Zod validation** — type-safe forms with instant field-level feedback

---

## 🗺️ Roadmap / Next Steps

- [ ] QR code image generation for check-in tokens
- [ ] Email templates with HTML (Thymeleaf)
- [ ] Admin dashboard with analytics charts
- [ ] Event waitlist auto-promotion
- [ ] Export attendance to CSV/Excel
- [ ] Push notifications (Firebase)
- [ ] Docker Compose for one-command startup
- [ ] Deploy to Render / Railway (backend) + Vercel (frontend)

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m "feat: add your feature"`
4. Push and open a Pull Request

---

## 📄 License

MIT License — free to use for educational projects.
