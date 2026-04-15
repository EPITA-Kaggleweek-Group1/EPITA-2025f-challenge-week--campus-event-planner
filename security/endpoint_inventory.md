# Endpoint Inventory
| Method | Path | Inputs | Auth |Sensitivity |
|--------|------|--------|------|------------|
| GET | `/` | — | None | Low |
| GET | `/events` | — | None | Low |
| GET | `/events/<id>` | `id` (int, path) | None | Low |
| POST | `/events` | title, date, description, location, capacity, image_url (JSON body) | None | High — creates data |
| GET | `/events/search` | q (query string) | None | Critical — raw SQL concat | 
| PATCH | `/events/<id>` | any JSON keys (body) | None | Critical — arbitrary column write |
| DELETE | `/events/<id>/registrations` | `reg_id` (query string) | None | High — no ownership check | 
| GET | /admin | — | None | High — full data dump, Stored XSS |