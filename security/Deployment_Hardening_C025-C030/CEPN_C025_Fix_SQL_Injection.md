SQL Injection — `GET /events/search` and `PATCH /events/:id`

**Severity:** Critical
**File:** `backend/app.py`


## Vulnerability 1 — Unsanitized f-string in search query
Fix — use parameterized query
cursor.execute(
    "SELECT * FROM events WHERE title LIKE %s
     OR description LIKE %s ORDER BY date",
    (f"%{query}%", f"%{query}%")
)


## Vulnerability 2 — Dynamic column names in `PATCH` UPDATE

Fix — strict column allowlist
ALLOWED_FIELDS = {
    'title', 'description', 'date',
    'location', 'capacity', 'image_url'
}
fields = [f"{k} = %s" for k in data if k in ALLOWED_FIELDS]
if not fields:
    return jsonify({"error": "No valid fields"}), 400

