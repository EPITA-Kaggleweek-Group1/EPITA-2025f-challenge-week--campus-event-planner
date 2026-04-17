# C026 — Authentication + IDOR Fix

**File:** `backend/src/routes.py`

---

## Problem 1 — No authentication on any route (CWE-306)

Every write and admin endpoint accepted requests from anyone with no
identity check. `SECRET_KEY` and `API_KEY` were defined in the app
but never used for any access control.

**Affected routes before fix:**

| Route | Method | Risk |
|-------|--------|------|
| `/events` | POST | Anyone could create events |
| `/events/<id>` | PATCH | Anyone could modify any event |
| `/events/<id>/register` | POST | Anyone could register for events |
| `/events/<id>/registrations` | GET | Anyone could read attendee PII |
| `/events/<id>/registrations` | DELETE | Anyone could delete registrations |
| `/admin` | GET | Anyone could access admin panel |

---

## Fix — API key decorator

Added `require_api_key` decorator before `register_events_routes`:

```python
from functools import wraps

def require_api_key(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        key = request.headers.get("X-API-Key", "")
        if key != os.environ.get("ADMIN_API_KEY", ""):
            return jsonify({"error": "Unauthorized"}), 401
        return f(*args, **kwargs)
    return decorated
```

Applied `@require_api_key` to all 6 protected routes:

```python
@app.route("/events", methods=["POST"])
@require_api_key
def add_event(): ...

@app.route("/events/<int:event_id>/registrations", methods=["GET"])
@require_api_key
def get_event_registrations(event_id): ...

@app.route("/events/<int:event_id>/register", methods=["POST"])
@require_api_key
def add_event_registration(event_id): ...

@app.route("/events/<int:event_id>/registrations", methods=["DELETE"])
@require_api_key
def delete_registration(event_id): ...

@app.route("/admin")
@require_api_key
def admin_page(): ...

@app.route("/events/<int:event_id>", methods=["PATCH"])
@require_api_key
def update_event(event_id): ...
```

Any request missing or providing a wrong `X-API-Key` header
receives `401 Unauthorized` and the function never executes.

---

## Problem 2 — IDOR on DELETE /events/:id/registrations (CWE-639)

The `event_id` path parameter was completely ignored. Any caller
could delete any registration by iterating `reg_id` values,
regardless of which event it belonged to.

**Before (vulnerable):**
```python
def delete_registration(event_id):
    reg_id = request.args.get("reg_id")
    cursor.execute(
        "DELETE FROM registrations WHERE id = %s",
        (reg_id,)
        # event_id never used — deletes any registration
    )
```

**After (fixed):**
```python
@require_api_key
def delete_registration(event_id):
    reg_id = request.args.get("reg_id")
    cursor.execute(
        "DELETE FROM registrations WHERE id = %s AND event_id = %s",
        (reg_id, event_id)   # registration must belong to this event
    )
    conn.commit()
    if cursor.rowcount == 0:
        return jsonify({"error": "Registration not found"}), 404
```

The `AND event_id = %s` clause ensures the registration must belong
to the event in the URL path. A mismatched `reg_id` returns 404
instead of silently deleting an unrelated registration.

`cursor.rowcount == 0` check added — returns 404 if nothing was
deleted, preventing silent failures.

---
