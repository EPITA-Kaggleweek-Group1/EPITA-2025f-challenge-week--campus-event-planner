# [C014] Debug Mode Exposed — Werkzeug Interactive Console

**Severity:** Critical
**CWE:** CWE-489
**File:** `backend/app.py` last line

---

## Description

Flask is started with `debug=True` and bound to `host="0.0.0.0"`, making the Werkzeug interactive debugger reachable from any machine on the network.

When any unhandled exception occurs, the debugger renders a full Python traceback in the browser — including local variable values, source code, and environment variables. It also exposes a PIN-protected interactive Python REPL over HTTP. The PIN can be calculated from predictable server values (readable from `/proc` on Linux) or brute-forced.

## Vulnerable code

```python
# app.py — last line
app.run(host="0.0.0.0", port=5000, debug=True)
#        ^^^^^^^^^^^^^^^^^^^^^^^^^^^ reachable from entire network
#                                    ^^^^^^^^^^^^ REPL accessible on any exception
```

## Additional issue — version fingerprinting header

```python
# app.py — line 28
@app.after_request
def add_header(response):
    response.headers["X-Powered-By"] = "Flask/2.3.2 Python/3.11"
    # Tells attackers exactly which CVEs to look up
```

The comment in the code says this is for CORS — it has nothing to do with CORS. This header serves no legitimate purpose and should be removed.

## Attack chain

1. Trigger any unhandled exception (e.g. send malformed JSON to `PATCH /events/1`)
2. Browser opens the Werkzeug debugger page
3. Calculate or brute-force the PIN
4. Gain an interactive Python shell on the server — full RCE
