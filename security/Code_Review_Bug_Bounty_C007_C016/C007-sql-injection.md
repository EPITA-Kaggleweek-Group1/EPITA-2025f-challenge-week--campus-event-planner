# [C007] SQL Injection — `GET /events/search` and `PATCH /events/:id`

**Severity:** Critical
**CWE:** CWE-89
**File:** `backend/app.py`

---

## Vulnerability 1 — Unsanitized f-string in search query

The `q` parameter from the URL is injected directly into the SQL string via an f-string. An attacker can break out of the LIKE clause and run arbitrary SQL — including `UNION SELECT`, `DROP TABLE`, or reading sensitive tables.

### Vulnerable code

```python
query = request.args.get("q", "")

cursor.execute(
    f"SELECT * FROM events WHERE title LIKE '%{query}%'"
    f" OR description LIKE '%{query}%' ORDER BY date"
)
```

### Attack payload

```
GET /events/search?q=%' UNION SELECT username,password_hash,3,4,5,6,7 FROM users -- -
```

---

## Vulnerability 2 — Dynamic column names in `PATCH` UPDATE

The `PATCH` endpoint iterates over all keys in the attacker-controlled JSON body and interpolates them as column names in the `SET` clause. Column names cannot be parameterized in MySQL — they must be validated against an allowlist before use.

### Vulnerable code

```python
data = request.get_json()

fields = []
values = []

for key, value in data.items():
    fields.append(f"{key} = %s")   # key is unvalidated — injected directly into SQL
    values.append(value)

cursor.execute(
    f"UPDATE events SET {', '.join(fields)} WHERE id = %s", values
)
```

### Attack payload

```
PATCH /events/1
{"id = 0 OR 1=1 -- ": "x"}

→ UPDATE events SET id = 0 OR 1=1 --  = %s WHERE id = 1
```

The attacker escapes the `WHERE id = %s` clause, affecting every row in the table.
