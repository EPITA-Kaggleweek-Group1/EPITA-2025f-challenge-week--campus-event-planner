# [C015] Error Handling — Stack Traces and Version Info Leaked to Clients

**Severity:** Medium
**CWE:** CWE-209
**File:** `backend/app.py`

---

## Issue 1 — Version info disclosed via `X-Powered-By` header

Every API response advertises the exact Flask and Python versions. This is reconnaissance gift-wrapping: an attacker queries any endpoint, reads the header, then looks up known CVEs for those exact versions.

```python
# app.py — add_header()
@app.after_request
def add_header(response):
    response.headers["X-Powered-By"] = "Flask/2.3.2 Python/3.11"
    return response  # "Allow cross-origin requests from the mobile apps"
    # ↑ comment is wrong — CORS has nothing to do with this header
```

---

## Issue 2 — No global error handler — raw tracebacks leak to clients

There is no `@app.errorhandler` registered for 500 errors. Any SQL error or unhandled Python exception will expose internal details in the response body — including table names, column names, file paths, and query structure.

The `PATCH /events/<id>` route also crashes with a `TypeError` if the body is not valid JSON because `data` is never null-checked:

```python
# app.py — PATCH route
data = request.get_json()
for key, value in data.items():   # TypeError: 'NoneType' is not iterable
                                   # if body is missing or not JSON
```

---

## Issue 3 — Teammate peer review findings

**Bug report #1 — missing DB credential validation in `database.py`**

`DB_USER` and `DB_PASSWORD` have no validation. If the `.env` file is missing or misconfigured, the app connects with `user=None`. On some MySQL setups this silently succeeds with anonymous access — there is no startup check that fails loudly when credentials are absent.

**Bug report #2 — `seed_users()` creates a `users` table outside `init_db()`**

The `users` table is created inline inside `seed_users()` in `seed.py` rather than in `init_db()` where all other schema lives. This means the table only exists if the seed script has been run — the app will crash with a missing-table error on a fresh deploy that skips seeding. Schema definitions belong exclusively in `database.py:init_db()`.
