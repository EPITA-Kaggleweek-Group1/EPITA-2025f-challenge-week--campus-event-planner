# [C012] Missing Authentication — All Write and Admin Routes

**Severity:** Critical
**CWE:** CWE-306
**File:** `backend/app.py` — all routes

---

## Description

Not a single route performs any identity check. There are no session checks, no API key validation, and no role verification anywhere in `app.py`. The admin panel, event creation, event mutation, and registration deletion are all open to anonymous HTTP requests.

`SECRET_KEY` and `API_KEY` are defined in the file but **never used** for any access control check.

```python
# These are defined but do nothing
SECRET_KEY = "super-secret-key-123"
API_KEY = "sk-1234-epita-admin-key"
```

## Affected routes

| Route | Method | Risk |
|-------|--------|------|
| `/admin` | GET | Exposes all event data + XSS vector |
| `/events` | POST | Anyone can create events (XSS delivery) |
| `/events/<id>` | PATCH | Anyone can mutate any event |
| `/events/<id>/registrations` | DELETE | Anyone can delete registrations |

## Attack example

```bash
# Create a fake event as an anonymous user — no credentials needed
curl -X POST http://localhost:5000/events \
  -H "Content-Type: application/json" \
  -d '{"title": "Free iPhone giveaway", "date": "2026-05-01T00:00:00"}'

# Access the full admin panel — no credentials needed
curl http://localhost:5000/admin
```
