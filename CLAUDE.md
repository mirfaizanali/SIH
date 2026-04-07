# CLAUDE.md — Campus Placement Portal

This file is read by Claude Code at the start of every session. Follow all instructions here without being asked.

---

## Project Overview

Full-stack Campus Internship & Placement Management Portal.

| Layer | Technology |
|---|---|
| Frontend | Angular 20.3.0, Angular Material 20 (M3), standalone components, Signals, RxJS 7.8.0 |
| Backend | Spring Boot 4.0.5, Java 21, Spring WebMVC (primary) |
| Database | MySQL 8.x, managed by Flyway |
| Auth | JWT (jjwt 0.12.6) + Spring Security 6 + OAuth2 |
| Cache | Caffeine via Spring Cache abstraction |
| Real-time | STOMP over SockJS (WebSocket) |
| Email | Spring WebFlux WebClient (non-blocking outbound only) |

---

## Repository Structure

```
PlacementPortal/
├── CLAUDE.md                          ← this file
├── Backend/
│   └── placement-portal-backend/      ← Spring Boot project root
│       ├── pom.xml
│       └── src/main/
│           ├── java/com/placement/portal/
│           └── resources/
│               ├── application.properties
│               └── db/migration/      ← Flyway SQL files
└── Frontend/
    └── Placement-portal/              ← Angular project root
        ├── package.json
        └── src/app/
```

---

## Backend Conventions

### Package Root
`com.placement.portal` — all Java code lives here.

### Package Layout
```
com.placement.portal
├── config/          — Spring @Configuration classes only
├── security/
│   ├── jwt/         — JwtTokenProvider, JwtAuthenticationFilter
│   ├── oauth2/      — OAuth2 handlers
│   └── rbac/        — Role enum, Permission enum
├── domain/          — JPA @Entity classes, no business logic
├── repository/      — Spring Data JPA interfaces only
├── service/         — Business logic, @Transactional
│   ├── auth/
│   ├── user/
│   ├── placement/
│   ├── notification/
│   ├── recommendation/
│   ├── analytics/
│   ├── resume/
│   └── admin/
├── controller/      — @RestController, thin — delegate to services
├── dto/
│   ├── request/     — inbound DTOs (validated with @Valid)
│   └── response/    — outbound DTOs
├── mapper/          — Entity ↔ DTO conversion
├── exception/       — GlobalExceptionHandler + custom exceptions
└── util/            — AppConstants, SecurityUtils
```

### Coding Rules — Backend
- **Never put business logic in controllers.** Controllers call one service method and return.
- **Never put queries in service classes.** All DB access goes through repositories.
- **All entities use UUID primary keys** (`CHAR(36)` in MySQL, `String` in Java with `@GeneratedValue` strategy custom or `UUID.randomUUID().toString()`).
- **All endpoints are versioned** under `/api/` (no `/api/v1/` needed for now — keep it simple).
- **DTOs are validated** with `jakarta.validation` annotations (`@NotBlank`, `@Email`, `@Size`, etc.).
- **GlobalExceptionHandler** (`@ControllerAdvice`) catches all exceptions — never return raw exceptions from controllers.
- **`spring.jpa.hibernate.ddl-auto=validate`** — Hibernate never touches the schema. Flyway owns the schema.
- **Flyway rule:** Never modify a committed migration file. Add new `V{n}__alter_*.sql` instead.
- **Lombok** is allowed for `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` on entities and DTOs.
- **WebMVC is the primary web layer.** `spring.main.web-application-type=servlet` must stay set. WebFlux `WebClient` is used only in `EmailNotificationService` for outbound HTTP.
- **Spring Security:** Use `@EnableMethodSecurity` on `SecurityConfig`. Use `@PreAuthorize` on controller methods for role/permission checks.
- **CORS:** Only allow `http://localhost:4200` in development.
- **Never log passwords, tokens, or PII** at any log level.

### Security Rules
- Passwords: BCryptPasswordEncoder strength=12.
- JWT access token: 15 minutes. Stored in-memory on frontend only.
- JWT refresh token: 7 days. Stored as httpOnly, SameSite=Strict cookie. Saved **hashed** (SHA-256) in DB.
- Rate limit: 100 req/min per authenticated user; 20 req/min per IP on `/api/auth/login`.
- File uploads: MIME whitelist — `application/pdf`, `application/msword`, `application/vnd.openxmlformats-officedocument.wordprocessingml.document`. Max 10MB.

### RBAC
```
ADMIN > PLACEMENT_OFFICER > FACULTY_MENTOR > STUDENT
EMPLOYER (peer, no hierarchy with academic roles)
```
Configured via `RoleHierarchyImpl` bean in `SecurityConfig`.

---

## Frontend Conventions

### Angular Rules
- **Standalone components only.** No NgModules.
- **Signals for state.** Use `signal()`, `computed()`, `effect()` — avoid manual `Subject`/`BehaviorSubject` for component state.
- **Lazy-loaded routes.** Every feature is lazy-loaded via `loadComponent`.
- **`accessToken` stored in-memory** inside `AuthService` private field — never in `localStorage` or `sessionStorage`.
- **`refreshToken` is an httpOnly cookie** managed by the browser — never read by JavaScript.
- **HTTP interceptors** (functional style) registered in `app.config.ts` via `withInterceptors([...])`.
- **`@stomp/stompjs`** for WebSocket (STOMP over SockJS). Authenticate at STOMP CONNECT via `connectHeaders: { Authorization: 'Bearer <token>' }`.
- **Prefix all API calls** with `environment.apiUrl` (e.g., `http://localhost:8081`).
- **`has-role` structural directive** for template-level role checks — do not use `*ngIf` with inline role logic.

### UI Framework — Angular Material (M3)
- **Angular Material 20** with M3 theming. All UI uses Material components.
- **Global theme** defined in `src/styles.scss` using `mat.define-theme()`.
- **Dark/Light mode** toggled via `ThemeService` (`core/services/theme.service.ts`). Toggle button is in the navbar. Theme persisted to `localStorage`, falls back to OS preference. CSS class `dark-theme` is added to `<html>`.
- **CSS Variables** in `:root` and `html.dark-theme` provide color tokens for both themes. Always use `var(--surface)`, `var(--bg)`, `var(--text)`, `var(--border)`, etc. — **never hardcode colors** like `white`, `#f8fafc`, `#64748b`.
- **Font:** Roboto (loaded from Google Fonts in `index.html`).
- **Icons:** Material Icons font (loaded from Google Fonts). Use `<mat-icon>icon_name</mat-icon>` — **never use emoji icons**.

### Material Component Usage Rules
| Element | Use This | Import |
|---|---|---|
| Buttons | `mat-flat-button` (primary), `mat-stroked-button` (secondary), `mat-flat-button color="warn"` (danger), `mat-icon-button` (icon-only) | `MatButtonModule` |
| Cards/Panels | `<mat-card>` | `MatCardModule` |
| Text inputs | `<mat-form-field appearance="outline"><input matInput>` | `MatFormFieldModule`, `MatInputModule` |
| Dropdowns | `<mat-form-field><mat-select>` — use `(selectionChange)` not `(change)`, value is `$event.value` | `MatSelectModule` |
| Textarea | `<mat-form-field><textarea matInput>` | `MatFormFieldModule`, `MatInputModule` |
| Dialogs/Modals | `MatDialog.open(Component, { data })` with `MAT_DIALOG_DATA` injection | `MatDialogModule` |
| Toolbar/Navbar | `<mat-toolbar color="primary">` | `MatToolbarModule` |
| Sidebar | `<mat-sidenav-container>` + `<mat-sidenav>` + `<mat-nav-list>` | `MatSidenavModule`, `MatListModule` |
| Pagination | `<mat-paginator>` with `(page)` event | `MatPaginatorModule` |
| Spinner | `<mat-spinner diameter="40">` | `MatProgressSpinnerModule` |
| Notification badge | `[matBadge]` on `mat-icon-button` + `mat-menu` | `MatBadgeModule`, `MatMenuModule` |
| Tooltips | `[matTooltip]` | `MatTooltipModule` |

### Icon Reference (Material Icon names)
| Concept | Icon Name |
|---|---|
| Dashboard/Home | `home` |
| Search/Jobs | `search` |
| Applications | `assignment` |
| Business/Company | `business` |
| Recommendations | `star` |
| Reports/Docs | `description` |
| Resume/Files | `folder` |
| Profile/Person | `person` |
| Users/Groups | `groups` / `people` |
| Create/Add | `add_circle` |
| Calendar/Date | `event` |
| Settings/Config | `settings` |
| Analytics/Chart | `bar_chart` |
| Audit/Logs | `receipt_long` |
| Drives | `track_changes` |
| Students/Grad | `school` |
| Work/Jobs | `work` |
| Notifications | `notifications` |
| Dark mode | `dark_mode` / `light_mode` |
| Logout | `logout` |
| Email | `email` |
| Lock/Password | `lock` |
| Close | `close` |
| Check/Approve | `check_circle` |
| Location | `location_on` |
| Payments/Salary | `payments` |

### Dark Mode Rules
- **All component CSS must use CSS variables** — never hardcode colors.
- Use `var(--surface)` for card/panel backgrounds (not `white`).
- Use `var(--bg)` for page background.
- Use `var(--text)` for primary text, `var(--text-muted)` for secondary text.
- Use `var(--border)` for borders, `var(--surface-hover)` for hover states.
- Use `var(--primary)` for accent colors — it automatically adjusts in dark mode.
- Badge classes (`.badge-success`, `.badge-warning`, etc.) are already dark-mode aware in `styles.scss`.
- The Material theme handles all `mat-*` component colors automatically via the M3 theme system.

### Folder Layout (`src/app/`)
```
core/
  guards/           auth.guard.ts, role.guard.ts, no-auth.guard.ts
  interceptors/     jwt.interceptor.ts, refresh.interceptor.ts, error.interceptor.ts
  services/         auth.service.ts, websocket.service.ts, notification.service.ts, theme.service.ts
  models/           user.model.ts

shared/
  components/       navbar, sidebar, notification-bell, data-table,
                    file-upload, skill-chip-list, status-badge,
                    confirm-dialog, loading-spinner
  pipes/            time-ago.pipe.ts, currency-inr.pipe.ts
  directives/       has-role.directive.ts

features/
  auth/             login, register, forgot-password
  student/          dashboard, profile, resume, job-search,
                    applications, internships, reports, recommendations
  faculty/          dashboard, mentees, report-review
  employer/         dashboard, post-job, applicants, interviews, profile
  placement-officer/ dashboard, drives, employer-management, analytics, student-overview
  admin/            user-management, system-config, audit-logs

layout/
  student-layout, faculty-layout, employer-layout,
  officer-layout, admin-layout, auth-layout
```

---

## Database Conventions

- All PKs are `CHAR(36)` UUID strings.
- All timestamps use `DATETIME` with `DEFAULT CURRENT_TIMESTAMP`.
- ENUM columns are defined in MySQL as `ENUM(...)` and mapped to Java `enum` types.
- Join tables (e.g., `student_skills`, `job_skills`, `drive_jobs`) use composite PKs.
- `audit_logs` uses `BIGINT AUTO_INCREMENT` PK (high-volume insert table).
- All foreign keys are explicitly named: `fk_{table}_{column}`.
- Indexes added on frequently queried columns: `user_id`, `status`, `created_at`.

### Migration Naming
```
V{n}__{snake_case_description}.sql
```
Example: `V1__create_users_and_roles.sql`

---

## Roles & Permissions Quick Reference

| Role | Key Permissions |
|---|---|
| STUDENT | APPLY_JOB, UPLOAD_RESUME, VIEW_OWN_APPLICATIONS, READ_JOBS |
| EMPLOYER | POST_JOB, SCHEDULE_INTERVIEW, VIEW_APPLICANTS |
| FACULTY_MENTOR | APPROVE_REPORT, VIEW_STUDENT_PROFILES |
| PLACEMENT_OFFICER | MANAGE_DRIVE, VIEW_ALL_APPLICATIONS, VIEW_ANALYTICS |
| ADMIN | MANAGE_USERS, VIEW_SYSTEM_LOGS + all above |

---

## API Endpoint Map

| Controller | Base Path |
|---|---|
| AuthController | `/api/auth/**` |
| UserController | `/api/users/**` |
| StudentController | `/api/students/**` |
| FacultyController | `/api/faculty/**` |
| EmployerController | `/api/employers/**` |
| JobController | `/api/jobs/**` |
| InternshipController | `/api/internships/**` |
| ApplicationController | `/api/applications/**` |
| InterviewController | `/api/interviews/**` |
| PlacementDriveController | `/api/drives/**` |
| NotificationController | `/api/notifications/**` |
| ResumeController | `/api/resumes/**` |
| RecommendationController | `/api/recommendations/**` |
| AnalyticsController | `/api/analytics/**` |
| AdminController | `/api/admin/**` |

Public routes (no auth): `/api/auth/**`, `/api/public/**`, `/ws/**`

---

## Notification Service Rules

- Always save notification to `notifications` table before sending WebSocket/email.
- WebSocket: `SimpMessagingTemplate.convertAndSendToUser(userId, "/queue/notifications", payload)`.
- Email: fire-and-forget via `WebClient` — log errors but do not throw.
- Every service that changes significant state must call the appropriate `NotificationService.notify*()` method.

---

## Recommendation Engine Rules

- `ScoringEngine` is a pure stateless class — no DB calls, no Spring beans injected other than constants.
- `MatchCandidateBuilder` handles all DB/cache reads and assembles inputs for `ScoringEngine`.
- Always use `@Cacheable("active_jobs")` and `@Cacheable("student_profiles")` — never bypass cache for recommendation reads.
- Hard disqualifier: if `student.cgpa < job.min_cgpa`, score = 0 and job is excluded from results.

---

## Development Commands

### Backend
```bash
# From Backend/placement-portal-backend/
mvn spring-boot:run
mvn test
mvn flyway:migrate   # manual migration trigger
```

### Frontend
```bash
# From Frontend/Placement-portal/
ng serve             # dev server on http://localhost:4200
ng build             # production build
ng test              # unit tests
```

---

## Environment Setup Checklist
1. MySQL running on `localhost:3306`, database `placement_portal_db` created.
2. MySQL user `placement_user` with password `placement_pass` and full access to the DB.
3. Backend `.env` or `application-local.properties` with `app.jwt.secret` (512-bit base64).
4. Frontend `src/environments/environment.ts` with `apiUrl: 'http://localhost:8081'`.

---

## What NOT to Do
- Do not modify committed Flyway migration files.
- Do not store tokens in `localStorage`.
- Do not add business logic to `@Entity` classes.
- Do not use `spring.jpa.hibernate.ddl-auto=create` or `create-drop`.
- Do not add `@Transactional` to controller methods.
- Do not log sensitive data (passwords, JWT tokens, PII).
- Do not use `NgModule` — all Angular components are standalone.
- Do not call `ScoringEngine` directly from controllers — always go through `RecommendationService`.
