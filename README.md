# Hadiid Vehicle Fabrication ERP



## ⚠️ Build status — please read before assuming this "just works"

This was built in a sandboxed environment whose outbound network access is
restricted to a small allowlist (npm, PyPI, GitHub, etc.). **Maven Central is
not on that allowlist** (the proxy returns `403 Forbidden` for
`repo.maven.apache.org`), so **`mvn compile` / `mvn package` could not be run
here, and the app has not been booted or tested in this environment.** Every
file was written by hand against the actual method signatures, DTO fields and
SQL columns it depends on, and cross-checked manually file-by-file, but that
is not a substitute for a real compiler pass.

**The first thing to do on a machine with normal internet access is:**

```bash
cd hadiid-erp
mvn clean package
```

Fix whatever that surfaces (most likely candidates: a Lombok-generated
getter/setter name that doesn't quite match a call site, or a Thymeleaf
expression that references a model attribute under a slightly different
name — the templates were written against the DTOs but not run through the
Thymeleaf parser). Then:

```bash
mvn spring-boot:run
# or: java -jar target/hadiid-erp.jar
```

The app defaults to the `h2` Spring profile (in-memory H2, MySQL-compatibility
mode) specifically so it can be built and smoke-tested without a real MySQL
server first. Flyway will run all migrations on boot — this seeds only system
configuration (roles, permissions, sections, body types, stages, payment
methods) and a single `admin` account; there is no demo business data. Open
http://localhost:8080 and sign in (see credentials below).

## Running against real MySQL

Create a database and a user, then run with the `mysql` profile:

```bash
export SPRING_PROFILES_ACTIVE=mysql
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=hadiid_erp
export DB_USER=hadiid_app
export DB_PASSWORD=<a real password>
java -jar target/hadiid-erp.jar
```

Flyway will create the schema and seed only system configuration (roles,
permissions, sections, body types, stages, payment methods) plus a single
initial `admin` account — the same as the `h2` profile. The system starts
with no customers, jobs, users (other than `admin`), stage history, labor,
or payments. Everything else is created by `admin` through the running app
(Settings → Users to add the rest of the team, then Customers/Jobs as usual).

## Initial login

| Username | Role  | Password       |
|----------|-------|----------------|
| `admin`  | Admin | `ChangeMe123!` |

This is the only account seeded on first boot. **Change this password
immediately after first login**, then use Settings → Users (as `admin`) to
create every other account the business needs — Supervisors and Normal
Users alike. There is no other pre-created data of any kind; `admin` is
responsible for entering customers, jobs, workers/contractors, and payments
from a clean system.

## What's implemented

- **Identity & Access** — form login, BCrypt, three roles, 24 permissions,
  backend `@PreAuthorize` on every mutating action (not just hidden buttons —
  Rule 32), user management screens (Admin only).
- **Reference Data** — vehicle sections, body types (with active/inactive
  toggle), fabrication stages, the `stage_transitions` policy table, payment
  methods, and the production calendar's day-status overrides — all editable
  without a code change.
- **Customers** — lookup-or-create by registration number so the same
  customer is never duplicated across jobs.
- **Fabrication Jobs** — search/filter/paginate, create, edit, detail screen
  with Overview / History / Labor & Payments tabs, and the two stage actions
  (Advance, Correct — see the business decision below).
- **Stage History** — append-only; the repository has no update/delete
  method by construction.
- **Labor & Payments** — worker/contractor directory, per-job assignments,
  a full payment history per worker (this is the one place the software
  genuinely upgrades the Excel model — no more single "Amount Paid" cell),
  and the job-card-level payment summary against Contract Amount.
- **Production Planning** — monthly capacity targets vs. Planned-to-Start /
  Actual Released (computed), weekly materials/labor budgets vs. actual
  (Labor Actual is always computed from `labor_payments`, never stored), and
  a WIP rollup.
- **Scheduling** — a 14-day window of jobs starting soon, day-status
  overrides, and a "Today's Actions" list (due-soon / overdue).
- **Reporting/Dashboards** — Overview (live stage counts), Finance (contract
  amount vs. paid vs. balance per section), and Stage Movement Analysis
  (average/max dwell time per stage, arrivals per stage).
- **Audit Trail** — every mutating action writes a row here, kept
  conceptually and physically separate from the stage-history log.

## Business decisions this spec flagged as unresolved (still unresolved)

These were called out as **"BUSINESS DECISION REQUIRED"** in the original
architecture document rather than silently guessed at. The software takes a
clearly-labeled working assumption for each so it's usable today, but Hadiid
should confirm the real answer:

1. **§H.1 — Stage transition policy.** Implemented as "Option B": a
   forward-only `advance()` action for everyday use, plus an always-reasoned,
   audited `correct()` action for anything backward/skipped. This is entirely
   data-driven via the `stage_transitions` table (see `V9__seed_reference_data.sql`
   and the Javadoc on `StageTransitionService`) — changing the seeded rows,
   not the code, changes the policy once Hadiid decides.
2. **§H.2 — Working-day calendar.** `WorkingDayCalendar` currently assumes
   Monday–Friday with no holiday exclusions (mirrors the workbook's
   `NETWORKDAYS` default). Kenyan public holidays and whether Saturday counts
   are not yet factored in.
3. **§H.3 — RELEASED is terminal.** No transition rule exists out of
   `RELEASED` in the seed data, so a released job cannot currently be
   reopened. Confirm this is actually the desired behavior.
4. **§H.4 — Day Status is informational only.** The production calendar's
   WORKING / NO_WORK / ON_HOLD overrides are stored and visible on the
   Schedule screen but do not yet affect the Working Days Remaining
   calculation.
5. **§H.6 — Materials Actual is still manual entry** on the weekly budget
   screen (matching the workbook); it isn't derived from anything else yet.

## One simplification I'm surfacing rather than quietly deciding

The pure role-based permission model means `PAYMENT_CREATE` is a
Supervisor/Admin permission. If finance staff are expected to record
payments day-to-day but should otherwise sit at `NORMAL_USER` scope, that
role alone won't let them — a per-user permission override was judged out
of scope for this pass given the effort/time available. The straightforward
fix, when a real finance user is created, is either to give that person the
`SUPERVISOR` role, or to add a narrow per-user permission-override table on
top of the existing role-based model. Worth deciding before go-live.

## Project layout

```
src/main/java/com/hadiid/erp/
  identity/     users, roles/permissions, auth
  reference/    sections, body types, stages, transitions, payment methods, calendar
  customer/     customer master data
  job/          fabrication_jobs, stage history, the stage-transition workflow, calculators
  labor/        contractors, workers, assignments, payments
  planning/     capacity targets, weekly budgets, WIP rollup
  scheduling/   14-day window, today's actions
  reporting/    dashboards, stage movement analysis
  security/     Spring Security wiring, the authenticated principal
  common/       audit trail, pagination, shared exceptions
  web/          home/login routing
src/main/resources/
  db/migration/       V1–V9 Flyway migrations (schema + system config + admin only)
  templates/          Thymeleaf + Tailwind (CDN) screens
  application.yml            default = h2 profile (dev/sandbox)
  application-mysql.yml      real deployment profile
```

## Why no ORM

Per the original spec: every read is an explicit `SELECT ... JOIN` mapped by
a hand-written `RowMapper`, every write is an explicit parameterized
`INSERT`/`UPDATE`. There is no `@Entity` anywhere in this codebase. This was
a hard constraint from the brief, not a stylistic choice.
