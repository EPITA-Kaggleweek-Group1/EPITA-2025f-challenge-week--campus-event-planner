# [C013] CORS — Wildcard Origin Policy

**Severity:** Medium
**CWE:** CWE-942
**File:** `backend/app.py` line 19

---

## Description

`CORS(app)` with no arguments sets `Access-Control-Allow-Origin: *` on every response. This means any web page on the internet can make cross-origin requests to the API and read the response.

Combined with missing authentication, attacker-controlled pages can make API calls on behalf of a visitor's browser.

There is also a secondary risk: when authentication is later added, developers often mistakenly add `supports_credentials=True` without restricting origins — which is a security bypass that allows cross-site credential theft.

## Vulnerable code

```python
from flask_cors import CORS

CORS(app)  # → Access-Control-Allow-Origin: *
```

## Attack scenario

```html
<!-- Hosted on attacker.com — silently calls the API from victim's browser -->
<script>
  fetch("http://localhost:5000/events", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ title: "<script>alert(1)</script>", date: "2026-01-01T00:00:00" })
  });
</script>
```
