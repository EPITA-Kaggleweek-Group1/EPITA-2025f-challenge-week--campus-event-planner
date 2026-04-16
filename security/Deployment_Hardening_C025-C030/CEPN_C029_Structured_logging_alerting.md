A JSON-format structured logging system was added. Every request produces a log line containing method, path, HTTP status, response time in milliseconds, and client IP. Security events (authentication failures, unhandled exceptions) produce WARNING-level entries with additional context fields, making automated alerting straightforward.


Log format:
{"time":"2026-04-15 14:22:01","level":"INFO","msg":"method":"GET","path":"/events","status":200,"duration_ms":4.2,"ip":"10.0.0.5"}


Authentication failure log:
{"time":"...","level":"WARNING","msg":"Unauthorized access attempt","ip":"185.220.101.1","path":"/admin"}
