# SkillMatch

A small job-search app that matches jobs to the skills you actually have.
Built by **Shubham** as a 2-day practical assessment, using AI tools throughout.

- **Android app**: Kotlin + Jetpack Compose (this folder, open it in Android Studio)
- **Backend**: Next.js API routes + PostgreSQL, both running in Docker (`backend/`, `docker-compose.yml`)

## Run it

**1. Start the backend + database** (needs Docker Desktop):

```bash
docker compose up -d --build
curl http://localhost:3000/api/health   # → {"ok":true}
```

The database is created and filled with sample data on first start (`backend/db/*.sql`).
To reset to fresh sample data: `docker compose down -v && docker compose up -d`.

**2. Run the app** from Android Studio (green ▶) on a phone or emulator.

The app calls `http://localhost:3000/`, so forward that port from the device to your PC once per connection:

```bash
adb reverse tcp:3000 tcp:3000
```

(The address lives in `gradle.properties` → `skillmatch.apiBaseUrl`. On an emulator you can use `http://10.0.2.2:3000/` instead and skip `adb reverse`.)

**3. Log in** with the demo account: `shubham@skillmatch.dev` / `password123`, or create a new account.

## What's in it

| Screen | What it does |
|---|---|
| Login / Sign up | Email + password. Token is stored on the device, so you stay logged in. |
| Jobs | Tabs: **Recommended** (you have ≥ 50% of the job's skills, best match first), **All Jobs**, **Saved**, **Applied**. Search by title, company, location or skill. Each card shows the fit badge, the skills you match, and your gaps. Heart to save; Apply on detail. |
| Job detail | Location, type, experience, salary, match %, skills you have vs. skills to learn, description, save. |
| Skills | Your skills grouped Expert → Beginner. Tap a skill to change its level or remove it; add new ones. Job matches update straight away. |
| Profile | Name, preferred location, experience level, app language, job alerts. Saved to the database. |
| Hindi | Switch language in Profile; the whole UI switches to Hindi (and the choice is saved with your profile). |

### How matching works

For each job the API splits the job's skills into **matched** (the user has them) and **gaps** (they don't), in one SQL query (`backend/src/lib/jobs.ts`).
`matchScore = matched / total`. **Excellent Fit** ≥ 80%, **Good Fit** ≥ 50%, otherwise **Stretch**.

## API

All endpoints except auth, skills and health need `Authorization: Bearer <token>`.

| Method | Path | Notes |
|---|---|---|
| POST | `/api/auth/register` | `{ name, email, password }` → `{ token, user }` |
| POST | `/api/auth/login` | `{ email, password }` → `{ token, user }` |
| GET | `/api/jobs?tab=recommended\|all\|saved&q=` | list with `matched`, `gaps`, `matchScore`, `fit`, `saved` |
| GET | `/api/jobs/:id` | one job, with description |
| POST / DELETE | `/api/jobs/:id/save` | save / unsave |
| GET / PUT | `/api/me` | profile; PUT accepts any of `name, location, experienceLevel, language, jobAlerts, skills` |
| GET | `/api/skills` | skill catalogue |
| GET | `/api/health` | DB check |

## Tech choices

- **Native Kotlin + Compose** – the Android Studio project was already set up; Compose keeps each screen in one readable file.
- **Retrofit + Gson + DataStore** – standard, small, easy to explain. No DI framework; the app is small enough for plain ViewModels.
- **Next.js route handlers + `pg` with plain SQL** – no ORM, so the matching query is visible and easy to change live.
- **Passwords hashed with bcrypt inside Postgres** (`pgcrypto`'s `crypt()`), **JWT** (`jose`) for sessions.
- **Docker Compose** – one command brings up Postgres (with schema + seed) and the API.
- **In-app language switch** uses AppCompat per-app locales (`values-hi/strings.xml`).

## Code map

```
app/src/main/java/in/binkgo/app/
  MainActivity.kt        app shell: login vs. main tabs, bottom nav, back handling
  data/Api.kt            Retrofit API, models, error handling
  data/SessionStore.kt   saves the login token
  ui/AppViewModel.kt     session, navigation, profile + skills saving, language
  ui/JobsViewModel.kt    job list, search (debounced), detail, save/unsave
  ui/*Screen.kt          one file per screen
backend/
  db/01_schema.sql, 02_seed.sql
  src/lib/jobs.ts        matching query
  src/app/api/**         route handlers
```

## Done / not done

**Done:** login + sign up, job list with search and tabs (including Applied), job detail with Apply, save jobs, skills editor that drives matching, profile settings saved to the DB, full Hindi UI, local job-alert test notification.

**Not done / known gaps:**
- Job alerts send a local device notification (not cloud/FCM push).
- Job content (titles, descriptions) and server error messages are English only; only the app UI is translated.
- No Learning/courses section; map view is a stylized location cluster (not Google Maps).
- No automated tests (skipped by choice for the 2-day scope); the API was checked with curl and the app on a real phone.
- No pagination (24 sample jobs); `JWT_SECRET` in `docker-compose.yml` is a dev value.

All companies, jobs and people in the sample data are made up.
