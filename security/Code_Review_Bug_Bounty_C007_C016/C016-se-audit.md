# [C016] SE Code Audit — Mobile Client Vulnerabilities

**Severity:** High
**CWE:** CWE-319 / CWE-390 / CWE-209
**Files:** `android/app/src/main/java/com/epita/eventplanner/api/ApiClient.java`, `ios/CampusEventPlanner/Services/APIClient.swift`, `AndroidManifest.xml`, `Info.plist`

---

## Bug Report #1 — Cleartext HTTP transport (Android + iOS)

Both mobile clients use `http://` and explicitly disable platform-level TLS enforcement. All API traffic — including registration data (names and emails) — travels in plaintext. Anyone on the same network can read and modify all traffic with a passive MITM attack.

### Android

```java
// ApiClient.java — line 17
private static final String BASE_URL = "http://10.0.2.2:5000";
```

```xml
<!-- AndroidManifest.xml -->
android:usesCleartextTraffic="true"
```

### iOS

```swift
// APIClient.swift — line 10
static let baseURL = "http://localhost:5000"
```

```xml
<!-- Info.plist -->
<key>NSAllowsArbitraryLoads</key>
<true/>
```

`NSAllowsArbitraryLoads=true` disables Apple's App Transport Security **globally** — this setting alone will cause App Store rejection in production.

---

## Bug Report #2 — HTTP status never validated on iOS (silent decode failures)

In `APIClient.swift`, the `response` object is never cast to `HTTPURLResponse` and the status code is never checked. A 404 or 500 error from the server gets piped directly into `JSONDecoder`, which fails silently — the user sees a confusing decode error instead of a meaningful message.

```swift
// APIClient.swift — fetchEvents and fetchEvent, same pattern in both
URLSession.shared.dataTask(with: url) { data, response, error in
    // 'response' (HTTPURLResponse) is never inspected
    // 4xx / 5xx responses are decoded as if they were valid event data
    // A 404 {"error": "Event not found"} causes a silent JSONDecoder crash
    let events = try JSONDecoder().decode([Event].self, from: data)
}
```

On Android, `fetchJson` throws the raw HTTP status code as a user-visible exception message, which leaks internal API behaviour to the user.

```java
// ApiClient.java — lines 31-34
if (status != HttpURLConnection.HTTP_OK) {
    throw new Exception("HTTP error: " + status);
    // Raw status code surfaced directly to the UI
}
```
