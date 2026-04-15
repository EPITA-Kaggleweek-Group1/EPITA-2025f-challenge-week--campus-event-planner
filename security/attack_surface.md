# Attack surface (all untrusted inputs)
| Input point | Type | Vulnerability | CWE |
|-------------|------|---------------|-----|
| GET /events/search?q= |	Query string |	SQL Injection — directly concatenated into LIKE query |	CWE-89 |
| PATCH /events/:id body keys | JSON keys as column names |	SQL Injection — column names from attacker-controlled dict keys | CWE-89 |
| POST /events — title, description |	JSON body |	Stored XSS — rendered unescaped in /admin HTML template (f-string interpolation) |	CWE-79 |
| DELETE /events/:id/registrations?reg_id= |	Query string integer |	IDOR — no ownership check, deletes any registration |	CWE-639 |
| Source code (any env) |	Hardcoded literal |	Secrets in code: secret_key, SECRET_KEY, API_KEY |	CWE-798 |
| Flask run config |	Environment/config |	debug=True exposes Werkzeug console over network |	CWE-489 |
| MySQL config |	Config default |	root user, empty password |	CWE-521 |
| All routes |	Missing auth |	No authentication/authorisation on any endpoint |	CWE-306 |