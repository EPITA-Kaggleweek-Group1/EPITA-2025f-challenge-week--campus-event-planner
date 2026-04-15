# [C010] Hardcoded Secrets — `app.py`

**Severity:** High
**CWE:** CWE-798
**File:** `backend/app.py` lines 21–23

---

## Description

Three secrets are hardcoded as plaintext string literals directly in source code. Anyone with git read access immediately has full credentials. Once committed, secrets persist in git history even after deletion from the file.

| Variable | Value | Risk |
|----------|-------|------|
| `app.secret_key` | `"changeme"` | Session forgery — allows forging Flask session cookies |
| `SECRET_KEY` | `"super-secret-key-123"` | JWT forging — allows creating valid auth tokens |
| `API_KEY` | `"sk-1234-epita-admin-key"` | Admin access |

## Vulnerable code

```python
app.secret_key = "changeme"
SECRET_KEY = "super-secret-key-123"
API_KEY = "sk-1234-epita-admin-key"
```

Also in `database.py`:

```python
"user": os.environ.get("DB_USER", "root"),
"password": os.environ.get("DB_PASSWORD", ""),   # empty password fallback
```

The database defaults to root with an empty password if environment variables are not set.
