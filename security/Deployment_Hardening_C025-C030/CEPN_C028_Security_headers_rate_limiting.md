# C028 — Security Headers + Rate Limiting

**Files:** `backend/src/routes.py`, `backend/src/app_factory.py`, `requirements.txt`

---

## Fix 1 — Security Headers

**File:** `backend/src/routes.py`

Replaced the old `add_header` function that leaked version info with
a proper security header function:

```python
@app.after_request
def add_security_headers(response):
    response.headers.pop("X-Powered-By", None)
    response.headers.pop("Server", None)
    response.headers["X-Content-Type-Options"] = "nosniff"
    response.headers["X-Frame-Options"] = "DENY"
    response.headers["Content-Security-Policy"] = "default-src 'self'"
    return response
```

---

### Why each header matters

**`X-Powered-By` — removed**

The original code actively advertised `Flask/2.3.2 Python/3.11` on
every response. Removing it means an attacker cannot immediately look
up CVEs for the exact framework version. They have to guess or probe
— both take more time and effort.

**`Server` — removed**

Same reason as `X-Powered-By`. The `Server` header reveals the web
server software and version. Removing both headers together forces
attackers to fingerprint the stack through other means.

**`X-Content-Type-Options: nosniff`**

Browsers sometimes try to be "helpful" by guessing the content type
of a response even when the server specifies one — this is called MIME
sniffing. An attacker can exploit this by uploading a file that looks
like an image but contains JavaScript — the browser sniffs it and
executes it as a script.

`nosniff` tells the browser: trust the `Content-Type` header exactly
as sent, never try to guess. This prevents a class of attacks where
malicious files are executed as the wrong type.

**`X-Frame-Options: DENY`**

This header prevents the app from being embedded inside an `<iframe>`
on another website. Without it, an attacker can load the admin panel
inside a hidden iframe on their own page, trick an admin into clicking
on it, and perform actions on their behalf — this attack is called
clickjacking.

`DENY` means the page can never be framed by any site, including the
same origin. For an API and admin panel this is the correct setting.

**`Content-Security-Policy: default-src 'self'`**

Even after fixing the XSS vulnerability in `/admin`, CSP adds a
second layer of defence. If an XSS payload somehow gets through in
the future, the browser will refuse to load or execute any resource
that doesn't come from the same origin.

`default-src 'self'` means: only load scripts, styles, images and
other resources from this domain. A payload trying to
`fetch('https://evil.com')` would be blocked by the browser before
it even fires.

---

## Fix 2 — Rate Limiting

**Files:** `backend/src/app_factory.py`, `backend/src/routes.py`

Added Flask-Limiter to control how many requests a single IP can make
in a given time window.

```python
# app_factory.py
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address

limiter = Limiter(
    get_remote_address,
    app=app,
    default_limits=["200 per day", "50 per hour"],
    storage_uri="memory://"
)
```

`get_remote_address` identifies each caller by their IP address.
`storage_uri="memory://"` keeps counters in memory — suitable for a
single-server deployment.

---

### Specific endpoints need stricter limits

**Default limit — 200/day, 50/hour**

Applied to all endpoints automatically. Prevents casual abuse and
automated scraping of the events list without blocking legitimate users
who might browse multiple times per hour.

**`GET /events?search=` — 30 per minute**

```python
@limiter.limit("30 per minute")
def list_events():
```

The search endpoint runs a `LIKE` query against the full events table
on every request. Without a limit, an attacker can send hundreds of
search requests per second, each triggering a full-table scan that
consumes CPU and DB connections — a low-effort DoS. 30 requests per
minute is generous for a human user but stops automated flooding.

This also mitigates the `SLEEP()`-based SQLi DoS demonstrated in
C020 — even if an attacker finds an injectable endpoint in the future,
they can only hold 30 DB connections per minute per IP instead of
thousands.

**`POST /events` — 10 per minute**

```python
@limiter.limit("10 per minute")
def add_event():
```

Creating events is a write operation that inserts rows into the
database. Without a limit, an attacker could flood the database with
thousands of junk events per second, exhausting storage and making
the events list unusable. 10 per minute is more than enough for a
legitimate admin creating events but stops automated bulk insertion.

**`POST /events/<id>/register` — 5 per minute**

```python
@limiter.limit("5 per minute")
def add_event_registration():
```

Registration is the most sensitive write endpoint — it stores user
PII (name and email). A strict limit prevents automated registration
attacks where a bot fills all available spots for an event before real
students can register. 5 per minute per IP is enough for normal use
but stops bulk registration scripts.

---