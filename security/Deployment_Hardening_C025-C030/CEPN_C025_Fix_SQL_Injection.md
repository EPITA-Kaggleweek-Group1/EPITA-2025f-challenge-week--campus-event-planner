# C025 — Fix SQL Injection (parameterized queries)

**Files:** `backend/src/service.py`

---

## Fix 1 — Search endpoint

**Before (vulnerable)** — `backend/app.py`:
```python
cursor.execute(
    f"SELECT * FROM events WHERE title LIKE '%{query}%'"
    f" OR description LIKE '%{query}%' ORDER BY date"
)
```
User input concatenated directly into SQL string using f-string.
Any character including `'`, `--`, `UNION` is interpreted as SQL.

**After (fixed)** — `backend/src/service.py` lines 27–31:
```python
conditions.append("(title LIKE %s OR description LIKE %s OR location LIKE %s)")
like = f"%{search}%"
params.extend([like, like, like])
cursor.execute(query, tuple(params))
```
User input passed as a parameter — the database driver escapes it
and treats it as pure data, never as SQL syntax.

---

## Fix 2 — PATCH column name injection

**Before (vulnerable)** — `backend/app.py`:
```python
for key, value in data.items():
    fields.append(f"{key} = %s")   # key is unvalidated — injected as column name
    values.append(value)

cursor.execute(f"UPDATE events SET {', '.join(fields)} WHERE id = %s", values)
```
JSON keys from the request body used directly as SQL column names.
Attacker sends `{"capacity = 0 WHERE 1=1 -- ": "x"}` to wipe all rows.

**After (fixed)** — `backend/src/service.py` lines 87–107:
```python
ALLOWED_FIELDS = {"title", "description", "date", "location", "capacity", "image_url"}

for key, value in data.items():
    if key not in ALLOWED_FIELDS:
        continue   # unknown keys silently rejected

fields.append(f"{key} = %s")
values.append(value)
```
Column names cannot be parameterized in SQL — the fix uses a whitelist
to ensure only known safe column names are ever used in the query.

---