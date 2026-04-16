# Team members
- Muhammad Danish JAVED
- Huizhe SU
- Tien Anh BUI
- Ibrahim JAREKJI

---

[TOC]

# Campus Event Planner

A mobile application for browsing, searching, and registering for campus events. Built with a Python/Flask + MySQL backend and native Android (Java) or iOS (Swift) frontends.

This is a **challenge week project** (~60% complete). The backend API is functional and seeded with data. The mobile apps display the events list but several features remain to be implemented.

---

## Architecture

```
┌──────────────┐         HTTP/JSON          ┌──────────────────┐
│              │  ◄────────────────────────► │                  │
│  Flask API   │        GET /events          │  Android (Java)  │
│  port 5000   │        GET /events/:id      │       or         │
│              │        POST /events         │  iOS (Swift)     │
│  ┌────────┐  │                             │                  │
│  │ MySQL  │  │  TODO: POST /events/:id/    │  TODO: detail,   │
│  │        │  │        register             │  registration,   │
│  └────────┘  │  TODO: GET /events/:id/     │  search,         │
│              │        registrations        │  favorites       │
└──────────────┘                             └──────────────────┘
```

## Prerequisites

| Tool           | Version |
| -------------- | ------- |
| Python         | 3.8+    |
| pip            | latest  |
| Android Studio | 2023.1+ |
| MySQL          | 8.0+    |
| *or* Xcode     | 15+     |

---

## Quick Start - MySQL server

```bash
cd backend
# Make sure you have docker compose installed and docker server running
docker compose up

# For fist time running, you might want to seed the database
python src/seed.py
```

## Quick Start — Backend

First of all, make sure you create `.env` based on `.env.example`.
Please do not commit `.env` to the repository.

```bash
cd backend
# make sure you have docker installed, and server running.
docker compose up

# If you want seed data
docker compose exec app python src/seed.py
```

Please wait for 30s, so that the MySQL will be correctly set up.

The API is now running at **http://localhost:5000**. Verify with:

```bash
curl http://localhost:5000/events
```

### Running tests

```bash
cd backend
python -m pytest tests/ -v
```

Some tests are marked `xfail` — they test features that students must implement.

---

## Quick Start — Android

1. Open the `android/` folder in **Android Studio**.
2. Let Gradle sync.
3. Make sure the Flask backend is running.
4. Run the app on an emulator (the API client uses `10.0.2.2:5000`).
5. You should see the list of events.

> If using a physical device, change `BASE_URL` in `ApiClient.java` to your computer's LAN IP.

---

## Quick Start — iOS

See [`ios/README-iOS.md`](ios/README-iOS.md) for detailed instructions. In short:

1. Create a new Xcode project named `CampusEventPlanner`.
2. Copy the provided Swift files into the project.
3. Set `EventListViewController` as the root view controller.
4. Run on the iOS Simulator with the Flask backend running.

---

## API Documentation

### `GET /`

Health check.

**Response** `200`
```json
{ "message": "Campus Event Planner API is running" }
```

### Events

#### `GET /events`

List all events ordered by date.

**Response** `200`
```json
[
  {
    "id": 1,
    "title": "Welcome Back Barbecue",
    "description": "Kick off the semester...",
    "date": "2026-03-15T12:00:00",
    "location": "Main Quad",
    "capacity": 200,
    "image_url": "https://...",
    "created_at": "2026-03-28T10:00:00"
  }
]
```

#### `GET /events/<id>`

Single event by ID.

**Response** `200` — event object (same shape as above)
**Response** `404` — `{ "error": "Event not found" }`

#### `POST /events`

Create a new event.

**Request body** (JSON):
```json
{
  "title": "My Event",
  "date": "2026-05-01T09:00:00",
  "description": "Optional",
  "location": "Optional",
  "capacity": 40,
  "image_url": "Optional"
}
```

**Response** `201` — the created event object
**Response** `400` — if `title` or `date` is missing

### Registration

#### GET /events/<id>/registration

List all the registrations related to one event.

**Response** `200` - the list of registrations
**Response** `404` - the event does not exist

#### POST /events/<id>/register

Create a new registration

**Request body** (JSON):
```json
{
  "user_name": "User Name",
  "email": "email@example.com"
}
```

**Response** `201` - The created registration.
**Response** `400` - If the request is not valid.
**Response** `404` - If the event is not presented.
**Response** `409` - If the user already registered.
---

## Student TODOs

### TODO 1 — Event Detail Screen

**Goal:** When a user taps an event in the list, the detail screen loads and displays all event information.

**Acceptance criteria:**
- The detail screen calls `GET /events/<id>` and populates all fields.
- The date is formatted in a human-readable way (e.g., "Saturday 18 April 2026 at 17:00").
- The description, location, and capacity are displayed.
- A back button returns to the list.

### TODO 2 — Registration

**Goal:** Users can register for an event by entering their name and email.

**Acceptance criteria:**
- A new endpoint `POST /events/<id>/register` accepts `{ "user_name": "...", "email": "..." }` and inserts a row into the `registrations` table.
- The endpoint returns `201` with the created registration, or `400` if fields are missing.
- The endpoint returns `404` if the event does not exist.
- The mobile app has a registration form (name + email fields, submit button) on the detail screen.
- On success, a confirmation message is shown.

### TODO 3 — Remaining Spots

**Goal:** The detail screen shows how many spots are left and disables registration when full.

**Acceptance criteria:**
- A new endpoint `GET /events/<id>/registrations` returns `{ "count": N }` (the number of registrations for that event).
- The mobile app displays "X / Y spots remaining" on the detail screen.
- When `count >= capacity`, the Register button is disabled and shows "Event Full".

### TODO 4 — Search and Filters

**Goal:** Users can search events by keyword and filter by date.

**Acceptance criteria:**
- `GET /events?search=keyword` filters events where `title` or `description` contains the keyword (case-insensitive, SQL `LIKE`).
- `GET /events?date=2026-04-10` filters events on that date.
- Both query parameters can be combined.
- The mobile app's search bar is wired up and sends the query parameter.
- Results update as the user types (with a small debounce).

### TODO 5 — Favorites

**Goal:** Users can mark events as favorites and view them in a separate screen.

**Acceptance criteria:**
- Favorites are stored locally on the device (SharedPreferences on Android, UserDefaults on iOS).
- Each event card has a heart/star icon that toggles the favorite state.
- A "Favorites" tab or button shows only favorited events.
- Favorites persist across app restarts.

---

## Evaluation Criteria

| Criterion                   | Weight |
| --------------------------- | ------ |
| TODO 1 — Event detail       | 20%    |
| TODO 2 — Registration       | 25%    |
| TODO 3 — Remaining spots    | 15%    |
| TODO 4 — Search and filters | 25%    |
| TODO 5 — Favorites          | 15%    |
| Code quality and comments   | Bonus  |
| All provided tests passing  | Bonus  |

---

## Project Structure

```
01-campus-event-planner/
├── backend/
│   ├── app.py              # Flask application
│   ├── database.py         # MySQL connection & schema
│   ├── models.py           # Data access functions
│   ├── seed.py             # Seed script (15 events)
│   ├── requirements.txt    # Python dependencies
│   └── tests/
│       ├── conftest.py     # Pytest fixtures
│       └── test_events.py  # Test suite (some xfail)
├── android/
│   ├── app/
│   │   └── src/main/
│   │       ├── java/com/epita/eventplanner/
│   │       │   ├── MainActivity.java
│   │       │   ├── EventDetailActivity.java
│   │       │   ├── model/Event.java
│   │       │   ├── adapter/EventAdapter.java
│   │       │   └── api/ApiClient.java
│   │       └── res/layout/
│   │           ├── activity_main.xml
│   │           ├── activity_event_detail.xml
│   │           └── item_event.xml
│   ├── build.gradle
│   └── settings.gradle
├── ios/
│   └── CampusEventPlanner/
│       ├── Models/Event.swift
│       ├── Services/APIClient.swift
│       ├── Views/
│       │   ├── EventListViewController.swift
│       │   └── EventDetailViewController.swift
│       └── Info.plist
├── .gitignore
└── README.md
```


---

## SE TODOs (40)


### Core (01-15)

| Code      | TODO                                                              | Status |
| --------- | ----------------------------------------------------------------- | ------ |
| CEPN-S001 | [API] Add capacity + image_url to events table                    | ✅      |
| CEPN-S002 | [API] GET /events returns all fields incl. capacity, image_url    | ✅      |
| CEPN-S003 | [API] GET /events/:id returns single event, 404 if not found      | ✅      |
| CEPN-S004 | [Mobile] Detail screen: call GET /events/:id on load              | ✅      |
| CEPN-S005 | [Mobile] Parse JSON, populate title, description, location, image | ✅      |
| CEPN-S006 | [Mobile] Format date as human-readable                            | ✅      |
| CEPN-S007 | [Mobile] Loading spinner + error view with retry                  | ✅      |
| CEPN-S008 | [API] Create registrations table (id, event_id, user_name, email) | ✅      |
| CEPN-S009 | [API] POST /events/:id/registrations with validation              | ✅      |
| CEPN-S010 | [API] Check capacity vs count, return 409 if full                 | ✅      |
| CEPN-S011 | [API] On success insert + return 201                              | ✅      |
| CEPN-S012 | [Mobile] Register button on detail screen                         | ✅      |
| CEPN-S013 | [Mobile] Form dialog: name + email fields                         | ✅      |
| CEPN-S014 | [Mobile] Client-side validation, inline errors                    | ✅      |
| CEPN-S015 | [Mobile] POST on submit, success toast, error handling            | ✅      |

### Intermediate (16-25)

| Code      | TODO                                                | Status |
| --------- | --------------------------------------------------- | ------ |
| CEPN-S016 | [API] GET /events/:id/registrations/count           | ✅      |
| CEPN-S017 | [Mobile] Display 'X/Y spots remaining'              | ✅      |
| CEPN-S018 | [Mobile] Color remaining: green/orange/red/gray     | ✅      |
| CEPN-S019 | [Mobile] Disable Register when full                 | ✅      |
| CEPN-S020 | [Mobile] Availability indicator on list cards       | ✅      |
| CEPN-S021 | [API] Search param on GET /events (LIKE title/desc) | ✅      |
| CEPN-S022 | [API] date_from/date_to params                      | ✅      |
| CEPN-S023 | [API] Combine search + date filters, ORDER BY date  | ✅      |
| CEPN-S024 | [Mobile] Search bar with 300ms debounce             | ✅      |
| CEPN-S025 | [Mobile] Date filter chips: Today/Week/Month/All    | ✅      |

### Advanced (26-35)

| Code      | TODO                                                       | Status |
| --------- | ---------------------------------------------------------- | ------ |
| CEPN-S026 | [Mobile] 'No events found' empty state                     |
| CEPN-S027 | [Mobile] Favorite icon on cards + detail                   |        |
| CEPN-S028 | [Mobile] Store favorites locally (SharedPref/UserDefaults) |        |
| CEPN-S029 | [Mobile] Toggle favorite, filled/outline icon              |        |
| CEPN-S030 | [Mobile] Favorites screen in navigation                    |        |
| CEPN-S031 | [Mobile] Favorites persist across restart                  |        |
| CEPN-S032 | [API] Pagination: page + limit params, total_count         | ✅      |
| CEPN-S033 | [Mobile] Infinite scroll with loading indicator            | ✅      |
| CEPN-S034 | [Mobile] Pull-to-refresh on event list                     | ✅      |
| CEPN-S035 | [Mobile] Event image thumbnails + placeholder              |

### Polish (36-40)

| Code      | TODO                                             | Status |
| --------- | ------------------------------------------------ | ------ |
| CEPN-S036 | [Mobile] Share button (title + date + location)  |        |
| CEPN-S037 | [Mobile] Countdown ('Starts in 3 days')          |        |
| CEPN-S038 | [Mobile] Location as secondary text on cards     |        |
| CEPN-S039 | [Mobile] Past Events section/toggle              |        |
| CEPN-S040 | [Mobile] Final polish: spacing, fonts, landscape |        |

## CS TODOs (30)


### Threat Modeling (C001-C006)

| Code      | TODO                                                   | Status |
| --------- | ------------------------------------------------------ | ------ |
| CEPN-C001 | Architecture diagram + data flows + trust boundaries   | ✅      |
| CEPN-C002 | Endpoint inventory (method, inputs, auth, sensitivity) | ?      |
| CEPN-C003 | STRIDE analysis on main flow                           | ?      |
| CEPN-C004 | Attack surface mapping (all untrusted inputs)          | ?      |
| CEPN-C005 | Attack tree (3+ paths, AND/OR nodes)                   | ?      |
| CEPN-C006 | 1-page threat assessment (top 5 risks)                 | ?      |

### Code Review / Bug Bounty (C007-C016)

| Code      | TODO                                      | Status |
| --------- | ----------------------------------------- | ------ |
| CEPN-C007 | Find SQL Injection (CWE-89)               | ✅      |
| CEPN-C008 | Find Stored XSS (CWE-79)                  | 🚧      |
| CEPN-C009 | Find IDOR (CWE-639)                       | 🚧      |
| CEPN-C010 | Find hardcoded secrets (CWE-798)          | 🚧      |
| CEPN-C011 | Find weak password hashing (CWE-328)      | 🚧      |
| CEPN-C012 | Audit missing authentication (CWE-306)    | 🚧      |
| CEPN-C013 | Analyze CORS configuration                | 🚧      |
| CEPN-C014 | Find debug mode exposure (CWE-489)        | 🚧      |
| CEPN-C015 | Review error handling (CWE-209)           | 🚧      |
| CEPN-C016 | Audit SE teammates' code (2+ bug reports) | 🚧      |

### Security Testing (C017-C024)

| Code      | TODO                                                 | Status |
| --------- | ---------------------------------------------------- | ------ |
| CEPN-C017 | SQLi exploit script                                  |        |
| CEPN-C018 | XSS exploit (3 payloads)                             |        |
| CEPN-C019 | IDOR enumeration script                              |        |
| CEPN-C020 | Rate limit / DoS test                                |        |
| CEPN-C021 | Automated scan (nmap + Python)                       |        |
| CEPN-C022 | API fuzzer                                           |        |
| CEPN-C023 | Project-specific exploit (Werkzeug/pickle/CSRF/etc.) |        |
| CEPN-C024 | test_security.py (8+ pytest cases)                   |        |

### Deployment & Hardening (C025-C030)

| Code      | TODO                                             | Status |
| --------- | ------------------------------------------------ | ------ |
| CEPN-C025 | Fix SQL Injection (parameterized queries)        |        |
| CEPN-C026 | Add authentication + fix IDOR                    |        |
| CEPN-C027 | Secure configuration (debug, secrets, passwords) |        |
| CEPN-C028 | Security headers + rate limiting                 |        |
| CEPN-C029 | Structured logging + alerting                    |        |
| CEPN-C030 | Final 5-page audit report                        |        |

---
## PR Workflow

This section defines the expected workflow for issues, commits, and pull requests. The goal is to keep collaboration structured, traceable, and reviewable.

### Issues

All work should be tracked through issues.

- If you discover a bug or identify a task/feature, **create an issue first** with a clear description.
- Each issue must include:
  - **Labels by domain**: `frontend`, `backend`, `documents`, `testing`
  - **Type labels**: `enhancement`, `bug`, `security`
- If something is unclear:
  - Ask questions **in the related issue** or **in the PR discussion**
  - Avoid relying on external chat (e.g., group messages), as they are not always visible or traceable

### Commits & Branching

All development must follow a structured branching and commit strategy.

#### Branching

- For every feature or fix:
  1. Create a new branch from `master` (or `main`)
  2. Use a descriptive branch name, e.g.:
     - `feature/user-authentication`
     - `fix/login-validation`
- **Never push directly to `master`**

#### Commit Structure

- Each commit must follow the format:

  ```text
  [type][scope] title
  ```

- Examples:
  
  ```text
  [feature][auth] add login endpoint
  [fix][api] handle null response in user service
  ```


- Rules:
- Each commit should represent **one logical change (one feature or fix)**
- Avoid mixing unrelated changes in a single commit
- Keep commits small and reviewable

#### Rebase

- Before opening a PR:

  1. Fetch the latest changes:
     ```bash
     git fetch origin
     ```

  2. Rebase your branch onto the latest `master`:
     ```bash
     git rebase -i origin/master
     ```

  3. If there are conflicts, resolve them locally:
     - Open the conflicted files and fix the conflicts
     - Then mark them as resolved:
       ```bash
       git add <file>
       ```
     - Continue the rebase:
       ```bash
       git rebase --continue
       ```

  4. If needed, abort the rebase:
     ```bash
     git rebase --abort
     ```

- This ensures:
  - Clean history
  - Minimal merge conflicts

### Pull Requests (PR)

All code must be merged via Pull Requests.

#### Creating a PR

1. Ensure your branch is up to date (rebased on `master`)
2. Squash commits if necessary to meet commit requirements
3. Open a PR targeting `master`

#### PR Requirements

- Each PR should contain:
- **Exactly one main commit**
- Optionally **one additional commit** for:
  - Refactoring
  - Minor fixes related to the implementation

- The additional commit must:
  - Be directly related to the modified code
  - Not introduce unrelated changes

#### Draft PRs

- If the work is not ready for review:
- Mark the PR as **Draft**
- Convert to “Ready for Review” only when:
  - Implementation is complete
  - Basic testing is done

#### Review & Merge

- Requirements before merging:
- At least **one approval**
- All discussions resolved
- Do not self-merge without review unless explicitly agreed

### Additional Guidelines (Recommended)

#### PR Description

- Clearly describe:
- What was done
- Why it was done
- Any important implementation details
- Reference related issues:

  ```text
  Closes #123
  ```

#### Testing

- Add or update tests when applicable
- Ensure existing tests pass before requesting review

#### Code Scope Control

- Avoid “drive-by changes” (unrelated edits)
- Keep PRs focused and minimal

#### Consistency

- Follow existing coding style and project conventions
- Avoid introducing inconsistent patterns
