# C027 — Secure Configuration

**Files:** `backend/src/routes.py`, `backend/src/app_factory.py`, `backend/src/app.py`

---

## Fix 1 — Remove X-Powered-By header (CWE-200)

Every response was advertising the exact Flask and Python versions,
giving attackers a free shortcut to relevant CVEs.

**Before (vulnerable)** — `backend/src/routes.py` lines 32–34:
```python
@app.after_request
def add_header(response):
    response.headers["X-Powered-By"] = "Flask/2.3.2 Python/3.11"
    return response
```

**After (fixed)** — lines removed entirely.

The comment in the original code claimed this was needed for CORS —
it has nothing to do with CORS. The header served no legitimate
purpose and was removed.

---

## Fix 2 — Move hardcoded secrets to environment variables (CWE-798)

Three secrets were hardcoded as plaintext string literals in source
code. Anyone with repository read access had full credentials
immediately. Once committed to git, secrets persist in history
even after deletion.

**Before (vulnerable)** — `backend/src/app_factory.py` lines 24–26:
```python
app.secret_key = "changeme"
SECRET_KEY = "super-secret-key-123"
API_KEY = "sk-1234-epita-admin-key"
```

**After (fixed)** — `backend/src/app_factory.py`:
```python
import os

app.secret_key = os.environ.get("FLASK_SECRET_KEY")
SECRET_KEY = os.environ.get("JWT_SECRET_KEY")
API_KEY = os.environ.get("ADMIN_API_KEY")
```

Set real values in `.env`:
```yaml
FLASK_SECRET_KEY: <random-32-byte-hex>
JWT_SECRET_KEY: <random-32-byte-hex>
ADMIN_API_KEY: <rotate-immediately>
```

Also fixed in `backend/src/routes.py` — `require_api_key` now
reads from environment instead of hardcoded string:
```python
import os

def require_api_key(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        key = request.headers.get("X-API-Key", "")
        if key != os.environ.get("ADMIN_API_KEY"):
            return jsonify({"error": "Unauthorized"}), 401
        return f(*args, **kwargs)
    return decorated
```

---

## Fix 3 — Disable debug mode (CWE-489)

Flask was started with `debug=True` and bound to `host="0.0.0.0"`,
exposing the Werkzeug interactive Python REPL to the entire network.
ffuf scan confirmed `/console` returned 200 with no authentication.

**Before (vulnerable)** — `backend/src/app.py`:
```python
app.run(host="0.0.0.0", port=5000, debug=True)
```

**After (fixed)**:
```python
import os

debug = os.environ.get("FLASK_DEBUG", "0") == "1"
app.run(host="0.0.0.0", port=5000, debug=debug)
```

Set in `docker-compose.yml`:
```yaml
environment:
  FLASK_DEBUG: "0"   # never "1" in production
```

With `debug=False`:
- Werkzeug debugger disabled — `/console` returns 404
- Stack traces no longer sent to clients
- Interactive REPL no longer accessible

---

## Fix 4 — CORS wildcard origin (CWE-942)

Every response included `Access-Control-Allow-Origin: *` meaning
any website on the internet could make cross-origin requests to
the API from a visitor's browser. Combined with the missing auth
on the original app, a malicious page could silently call
`POST /events` and store XSS payloads on behalf of any visitor.

**Before (vulnerable)** — `backend/src/app_factory.py`:
```python
CORS(app)  # → Access-Control-Allow-Origin: *
```

**After (fixed)**:
```python
CORS(app, origins=[os.environ.get("ALLOWED_ORIGINS")])
```

Added to `.env`:
```
ALLOWED_ORIGINS=http://localhost:3000
```

In production this should be set to the actual frontend domain:
```
ALLOWED_ORIGINS=https://campus-app.epita.fr
```

Only the specified origin is now permitted to make cross-origin
requests. Any other origin receives a CORS error from the browser
and the request is blocked before it reaches the API.

---

## Fix 5 — MySQL root/empty password (CWE-521)

Database was configured to connect as `root` with an empty password
fallback — any network-accessible MySQL instance would accept
anonymous connections.

**Before (vulnerable)** — `backend/src/config/config.py`:
```python
"user": os.environ.get("DB_USER", "root"),
"password": os.environ.get("DB_PASSWORD", ""),  # empty fallback
```

**After (fixed)**:
```python
"user": os.environ.get("DB_USER", "root"),
"password": os.environ.get("DB_PASSWORD"),  # no fallback — fails loudly
```

Set a strong password in `docker-compose.yml`:
```yaml
environment:
  MYSQL_ROOT_PASSWORD: <strong-random-password>
  DB_PASSWORD: <strong-random-password>
```

---

## Fix 6 — Weak password hashing replaced with bcrypt (CWE-328)

Passwords were hashed with MD5 — a general-purpose hash with no salt
and no work factor. A modern GPU can compute 10 billion MD5 hashes
per second, making any MD5-hashed password instantly reversible via
rainbow tables. The hash for "admin123" is `0192023a7bbd73250516f069df18b500`
— crackable in under 1ms on crackstation.net.

**Before (vulnerable)** — `backend/src/seed.py`:
```python
import hashlib

("admin", hashlib.md5(b"admin123").hexdigest(), "admin"),
("alice", hashlib.md5(b"password").hexdigest(), "user"),
("bob",   hashlib.md5(b"bob2026").hexdigest(),  "user"),
```

**After (fixed)** — `backend/src/seed.py`:
```python
import bcrypt

def hash_password(plain: str) -> str:
    return bcrypt.hashpw(
        plain.encode("utf-8"),
        bcrypt.gensalt(rounds=12)
    ).decode("utf-8")

("admin", hash_password("admin123"), "admin"),
("alice", hash_password("password"), "user"),
("bob",   hash_password("bob2026"),  "user"),
```

bcrypt provides three properties MD5 lacks: a built-in random salt
(identical passwords produce different hashes), a tunable cost factor
(rounds=12 means ~250ms per hash — 100 million times slower than MD5
for an attacker), and purpose-built resistance to GPU acceleration.

Added `bcrypt==4.1.2` to `requirements.txt`.

---

