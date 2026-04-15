# Threat assessment: top 5 risks
| #	| Risk | CWE | Likelihood | Impact | Rating | Fix |
|---|------|-----|------------|--------|--------|-----|
| 1 | SQL Injection — GET /events/search?q= concatenates user input directly into a LIKE query with an f-string. Full DB read is a single request away. |	CWE-89 | Trivial | Full data exfiltration, possible write | Critical | Parameterized queries |
| 2 | Stored XSS + debug RCE — POST /events stores raw HTML in title/description. GET /admin renders them with Python f-string interpolation, no escaping. With Werkzeug debug=True, XSS can pivot to remote code execution via the debugger PIN. | CWE-79 / CWE-489 |	Easy | Browser hijack → server RCE | Critical | Escape output; disable debug |
| 3 | Missing authentication (all endpoints) — no auth on POST /events (create), PATCH /events (modify), DELETE registrations, or GET /admin. Any unauthenticated caller has full write access. | CWE-306 | Trivial | Unrestricted data modification | Critical | JWT / session auth layer |
| 4 | IDOR on DELETE /registrations — reg_id comes from the query string; server deletes it with no check that the caller owns it or that it belongs to the given event. | CWE-639 | Medium | Any registration silently deleted | High | Verify ownership in query |
| 5 | Hardcoded secrets + root/empty-password DB — app.secret_key, SECRET_KEY, API_KEY live in source code. DB connects as root with no password. If the repo is public these are immediately leaked. | CWE-798 / CWE-521 | Medium | Full credential compromise | High | Env vars + strong DB password |
