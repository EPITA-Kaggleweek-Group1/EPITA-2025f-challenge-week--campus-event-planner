Hardcoded Secrets — `app.py`
**File:** `backend/app.py` lines 21–23


Fix — replace with bcrypt

import bcrypt

def hash_password(plain: str) -> str:
    return bcrypt.hashpw(
        plain.encode("utf-8"),
        bcrypt.gensalt(rounds=12)   # cost factor 12 is a safe default
    ).decode("utf-8")

def verify_password(plain: str, stored_hash: str) -> bool:
    return bcrypt.checkpw(plain.encode("utf-8"), stored_hash.encode("utf-8"))

# In seed_users():
pw_hash = hash_password("admin123")
