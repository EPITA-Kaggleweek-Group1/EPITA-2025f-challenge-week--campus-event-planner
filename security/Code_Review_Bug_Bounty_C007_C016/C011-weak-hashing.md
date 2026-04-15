# [C011] Weak Password Hashing — MD5 with no salt

**Severity:** Critical
**CWE:** CWE-328
**File:** `backend/seed.py`

---

## Description

Passwords are hashed with MD5 — a general-purpose hash function, not a password-hashing function. It has no salt (identical passwords produce identical hashes) and no work factor. A modern GPU can compute 10 billion MD5 hashes per second, making any MD5-hashed password instantly reversible via rainbow tables.

## Vulnerable code

```python
import hashlib

pw_hash = hashlib.md5(b"admin123").hexdigest()
# → "0192023a7bbd73250516f069df18b500"
# Crackable in < 1ms on crackstation.net
```

## Comparison

| | MD5 — do not use | bcrypt / argon2 — use instead |
|---|---|---|
| Salt | None | Built-in random salt |
| Work factor | None | Tunable cost factor |
| Speed (GPU) | ~10 billion hashes/sec | ~100 hashes/sec |
| Rainbow tables | Exist for all common passwords | Not viable |
