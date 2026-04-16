"""
Test suite for the Campus Event Planner API.

Tests marked with 'xfail' are EXPECTED TO FAIL because the corresponding
features have not been implemented yet. Students must make these tests pass.
"""

import json
import pytest
from werkzeug.wrappers import response
from datetime import datetime
from seed import seed_users, seed


# ------------------------------------------------------------------ #
#  Passing tests — features that already work
# ------------------------------------------------------------------ #


class TestListEvents:
    """GET /events"""

    def test_get_events_returns_list(self, client):
        """The events endpoint should return a JSON array."""
        response = client.get("/events")
        assert response.status_code == 200
        data = response.get_json()["data"]
        assert isinstance(data, list)
        assert len(data) >= 2  # we seeded at least 2 events

    def test_events_contain_required_fields(self, client):
        """Each event object should include the core fields."""
        response = client.get("/events")
        data = response.get_json()["data"]
        event = data[0]
        for field in (
            "id",
            "title",
            "description",
            "date",
            "location",
            "capacity",
            "image_url",
        ):
            assert field in event, f"Missing field: {field}"


class TestGetEventById:
    """GET /events/<id>"""

    def test_get_event_by_id(self, client):
        """Fetching an existing event by ID should return that event."""
        response = client.get("/events/1")
        assert response.status_code == 200
        data = response.get_json()
        assert data["id"] == 1
        assert "title" in data

    def test_get_event_not_found(self, client):
        """Fetching a non-existent event should return 404."""
        response = client.get("/events/9999")
        assert response.status_code == 404
        data = response.get_json()
        assert "error" in data


class TestPatchEventById:
    def test_event_update_sql_injection_via_field_name_98(self, client):
        """
        This is reported in: https://github.com/EPITA-Kaggleweek-Group1/EPITA-2025f-challenge-week--campus-event-planner/issues/98
        Ensure malicious field names cannot break SQL structure.
        """

        # 1. create a new event
        create_payload = {
            "title": "Injection Test Event",
            "description": "Safe event",
            "date": "2026-06-01T10:00:00",
            "location": "Lab",
            "capacity": 10,
        }

        create_resp = client.post(
            "/events",
            data=json.dumps(create_payload),
            content_type="application/json",
        )
        assert create_resp.status_code == 201

        event_id = create_resp.get_json()["id"]

        # 2. malicious payload
        malicious_payload = {f'title = "HACKED" where id = {event_id} -- ': "x"}

        response = client.patch(
            f"/events/{event_id}",
            data=json.dumps(malicious_payload),
            content_type="application/json",
        )

        # 3. assert safe behavior

        # assert response.status_code in (400, 422, 500)

        # 4. verify DB not corrupted by checking event still exists correctly
        get_resp = client.get(f"/events/{event_id}")
        assert get_resp.status_code == 200

        data = get_resp.get_json()

        assert data["title"] == "Injection Test Event"
        assert data["description"] == "Safe event"


class TestCreateEvent:
    """POST /events"""

    def test_create_event(self, client):
        """Creating an event with valid data should return 201."""
        payload = {
            "title": "New Workshop",
            "description": "A brand new workshop",
            "date": "2026-05-01T09:00:00",
            "location": "Lab 303",
            "capacity": 40,
        }
        response = client.post(
            "/events",
            data=json.dumps(payload),
            content_type="application/json",
        )
        assert response.status_code == 201
        data = response.get_json()
        assert data["title"] == "New Workshop"
        assert "id" in data

    def test_create_event_missing_fields(self, client):
        """Creating an event without required fields should return 400."""
        response = client.post(
            "/events",
            data=json.dumps({"description": "no title or date"}),
            content_type="application/json",
        )
        assert response.status_code == 400


# ------------------------------------------------------------------ #
#  Failing tests — students must implement these features
# ------------------------------------------------------------------ #


class TestGetRegistrations:
    """GET /events/<id>/registrations"""

    def test_return_empty_list_if_no_registration(self, client):
        response = client.get("/events/1/registrations")
        assert response.status_code == 200
        data = response.get_json()
        assert isinstance(data, list)
        assert len(data) == 0  # There is no registration yet.

    def test_return_not_found_if_event_not_exist(self, client):
        response = client.get("/events/9999/registrations")
        assert response.status_code == 404
        data = response.get_json()
        assert "error" in data


class TestCreateRegistration:
    # TODO: We need to refine this test, since we should use create event to create new event for testing.
    """POST /events/<id>/register"""

    def test_register_for_event(self, client):
        """Create normally."""
        payload = {
            "user_name": "Alice Dupont",
            "email": "alice@test.com",
        }
        response = client.post(
            "/events/1/register",
            data=json.dumps(payload),
            content_type="application/json",
        )
        assert response.status_code == 201
        data = response.get_json()
        assert data["user_name"] == "Alice Dupont"
        assert data["event_id"] == 1

    def test_register_event_not_found(self, client):
        """The event is not found"""
        payload = {
            "user_name": "Alice2",
            "email": "alice2@test.com",
        }

        response = client.post(
            "/events/9999/register",
            data=json.dumps(payload),
            content_type="application/json",
        )

        assert response.status_code == 404
        assert response.get_json()["error"] == "Event not found"

    def test_register_missing_fields(self, client):
        """Missing field should return 400"""
        payload = {
            "user_name": "Alice"
            # missing email
        }
        response = client.post(
            "/events/1/register",
            data=json.dumps(payload),
            content_type="application/json",
        )

        assert response.status_code == 400

    def test_register_empty_body(self, client):
        """Missing body should return 400"""
        response = client.post(
            "/events/1/register",
            data=json.dumps({}),
            content_type="application/json",
        )

        assert response.status_code == 400

    def test_register_duplicate(self, client):
        """Multiple registration should return 409"""
        payload = {
            "user_name": "Alice4",
            "email": "alice4@test.com",
        }

        # first request
        r1 = client.post(
            "/events/1/register",
            data=json.dumps(payload),
            content_type="application/json",
        )
        assert r1.status_code == 201

        # duplicate
        r2 = client.post(
            "/events/1/register",
            data=json.dumps(payload),
            content_type="application/json",
        )

        assert r2.status_code == 409
        assert "already" in r2.get_json()["error"].lower()

    def test_user_can_register_multiple_events(self, client):
        payload = {
            "user_name": "Alice3",
            "email": "alice3@test.com",
        }

        # register event 1
        r1 = client.post(
            "/events/1/register",
            data=json.dumps(payload),
            content_type="application/json",
        )

        assert r1.status_code == 201

        # register event 2
        r2 = client.post(
            "/events/2/register",
            data=json.dumps(payload),
            content_type="application/json",
        )

        assert r2.status_code == 201

        data = r2.get_json()
        assert data["event_id"] == 2

    def test_event_capacity_limit_reached(self, client):
        """Event should reject registrations when capacity is full."""

        # 1. create event with capacity = 1
        payload = {
            "title": "Limited Workshop",
            "description": "Only one seat available",
            "date": "2026-05-01T09:00:00",
            "location": "Lab 303",
            "capacity": 1,
        }

        create_resp = client.post(
            "/events",
            data=json.dumps(payload),
            content_type="application/json",
        )

        assert create_resp.status_code == 201
        event = create_resp.get_json()
        event_id = event["id"]

        # 2. first registration → should succeed
        user1 = {
            "user_name": "Alice",
            "email": "alice@test.com",
        }

        r1 = client.post(
            f"/events/{event_id}/register",
            data=json.dumps(user1),
            content_type="application/json",
        )

        assert r1.status_code == 201

        # 3. second registration → should fail (event full)
        user2 = {
            "user_name": "Bob",
            "email": "bob@test.com",
        }

        r2 = client.post(
            f"/events/{event_id}/register",
            data=json.dumps(user2),
            content_type="application/json",
        )

        assert r2.status_code in (400, 409)

        data = r2.get_json()
        assert "error" in data or "message" in data


class TestGetRegistrationsCount:
    def test_registrations_count(self, client):
        """GET /events/<id>/registrations/count should return registration count."""
        response = client.get("/events/1/registrations/count")
        assert response.status_code == 200
        data = response.get_json()
        assert "count" in data
        assert isinstance(data["count"], int)

    def test_registration_count_event_not_found(self, client):
        """GET /events/<id>/registrations/count should return 404 if event does not exist."""
        response = client.get("/events/99999/registrations/count")

        assert response.status_code == 404

    def test_registration_count_matches_created_registrations(self, client):
        """Count endpoint should reflect actual number of registrations."""

        # 1. Create event
        event_payload = {
            "title": "Count Test Event",
            "description": "Testing registration count",
            "date": "2026-05-01T09:00:00",
            "location": "Lab 303",
            "capacity": 100,
        }

        event_resp = client.post(
            "/events",
            data=json.dumps(event_payload),
            content_type="application/json",
        )
        assert event_resp.status_code == 201
        event_id = event_resp.get_json()["id"]

        # 2. Create multiple registrations via loop
        num_registrations = 10

        for i in range(num_registrations):
            reg_payload = {
                "user_name": f"User {i}",
                "email": f"user{i}@test.com",  # ensure uniqueness
            }

            reg_resp = client.post(
                f"/events/{event_id}/register",
                data=json.dumps(reg_payload),
                content_type="application/json",
            )
            assert reg_resp.status_code == 201

        # 3. Check count
        count_resp = client.get(f"/events/{event_id}/registrations/count")
        assert count_resp.status_code == 200

        data = count_resp.get_json()
        assert "count" in data
        assert isinstance(data["count"], int)

        assert data["count"] == num_registrations


class TestSearch:
    """Search and filter (NOT YET IMPLEMENTED)."""

    def test_event_search(self, client):
        """GET /events?search=test should filter events by keyword."""

        response = client.get("/events?search=Test")
        assert response.status_code == 200

        data = response.get_json()["data"]
        assert isinstance(data, list)

        # All returned events should match search in at least one field
        for event in data:
            assert (
                "test" in event["title"].lower()
                or "test" in event["description"].lower()
                or "test" in event["location"].lower()
            )

    def test_event_search_by_title(self, client):
        """GET /events?search=test should filter events by keyword."""

        response = client.get("/events?title=Test")
        assert response.status_code == 200

        data = response.get_json()["data"]
        assert isinstance(data, list)

        # All returned events should match search in at least one field
        for event in data:
            assert "test" in event["title"].lower()

    def test_event_search_by_description(self, client):
        """GET /events?description=... should filter by description."""

        response = client.get("/events?description=Testing")
        assert response.status_code == 200

        data = response.get_json()["data"]
        assert isinstance(data, list)

        for event in data:
            assert "testing" in event["description"].lower()

    def test_event_search_by_title_and_description(self, client):
        """GET /events?title=...&description=... should apply AND filter."""

        response = client.get("/events?title=test&description=event")
        assert response.status_code == 200

        data = response.get_json()["data"]
        assert isinstance(data, list)

        for event in data:
            assert "test" in event["title"].lower()
            assert "event" in event["description"].lower()

    def test_event_search_sql_inject_97(self, client):
        """
        This is a security problem reported on
        https://github.com/EPITA-Kaggleweek-Group1/EPITA-2025f-challenge-week--campus-event-planner/issues/97
        GET /events/search?q=%' UNION SELECT table_name,2,3,4,5,6,7,8 FROM information_schema.tables-- -
        """
        malicious_queries = [
            "%' UNION SELECT table_name,2,3,4,5,6,7,8 FROM information_schema.tables-- -",
            "%' UNION SELECT user_name,email,3,4,5,6,7,8 FROM registrations-- -",
        ]

        for q in malicious_queries:
            response = client.get(f"/events?search={q}")
            assert response.status_code == 200

            data = response.get_json()["data"]
            assert isinstance(data, list)

            # should not return any rows from injection
            assert len(data) == 0

    def test_event_search_date_from(self, client):
        """GET /events?date_from should return events on/after given date."""

        # create event (future date)
        payload = {
            "title": "Date From Test Event",
            "description": "testing date_from filter",
            "date": "2026-06-01T10:00:00",
            "location": "Lab",
            "capacity": 10,
        }

        create_resp = client.post(
            "/events",
            data=json.dumps(payload),
            content_type="application/json",
        )
        assert create_resp.status_code == 201

        response = client.get("/events?date_from=2026-06-01")
        assert response.status_code == 200

        data = response.get_json()["data"]
        assert isinstance(data, list)
        assert len(data) > 0

        for event in data:
            event_dt = parse_api_datetime(event["date"])
            expected_dt = parse_date("2026-06-01")
            assert event_dt >= expected_dt

    def test_event_search_date_to(self, client):
        """GET /events?date_to should return events on/before given date."""

        payload = {
            "title": "Date To Test Event",
            "description": "testing date_to filter",
            "date": "2026-05-10T10:00:00",
            "location": "Lab",
            "capacity": 10,
        }

        create_resp = client.post(
            "/events",
            data=json.dumps(payload),
            content_type="application/json",
        )
        assert create_resp.status_code == 201

        response = client.get("/events?date_to=2026-05-10")
        assert response.status_code == 200

        data = response.get_json()["data"]
        assert isinstance(data, list)
        assert len(data) > 0

        for event in data:
            event_dt = parse_api_datetime(event["date"])
            expected_dt = parse_date("2026-05-10")
            assert event_dt <= expected_dt

    def test_event_search_date_range(self, client):
        """GET /events?date_from&date_to should return events within range."""

        payload = {
            "title": "Date Range Test Event",
            "description": "testing range filter",
            "date": "2026-05-15T10:00:00",
            "location": "Lab",
            "capacity": 10,
        }

        create_resp = client.post(
            "/events",
            data=json.dumps(payload),
            content_type="application/json",
        )
        assert create_resp.status_code == 201

        response = client.get("/events?date_from=2026-05-01&date_to=2026-05-31")
        assert response.status_code == 200

        data = response.get_json()["data"]
        assert isinstance(data, list)
        assert len(data) > 0

        start_dt = parse_date("2026-05-01")
        end_dt = parse_date("2026-05-31")

        for event in data:
            event_dt = parse_api_datetime(event["date"])
            assert start_dt <= event_dt <= end_dt

    def test_events_default_order_is_asc(self, client):
        """GET /events should return events ordered by date DESC by default."""

        response = client.get("/events")
        assert response.status_code == 200

        data = response.get_json()["data"]
        assert isinstance(data, list)
        assert len(data) >= 2

        # check descending order
        for i in range(len(data) - 1):
            d1 = parse_api_datetime(data[i]["date"])
            d2 = parse_api_datetime(data[i + 1]["date"])
            assert d1 <= d2

    def test_events_order_desc(self, client):
        """GET /events?order=desc should return events ordered by date DESC."""

        response = client.get("/events?order=desc")
        assert response.status_code == 200

        data = response.get_json()["data"]
        assert isinstance(data, list)
        assert len(data) >= 2

        for i in range(len(data) - 1):
            d1 = parse_api_datetime(data[i]["date"])
            d2 = parse_api_datetime(data[i + 1]["date"])
            assert d1 >= d2

    def test_events_order_asc(self, client):
        """GET /events?order=asc should return events ordered by date ASC."""

        response = client.get("/events?order=asc")
        assert response.status_code == 200

        data = response.get_json()["data"]
        assert isinstance(data, list)
        assert len(data) >= 2

        for i in range(len(data) - 1):
            d1 = parse_api_datetime(data[i]["date"])
            d2 = parse_api_datetime(data[i + 1]["date"])
            assert d1 <= d2


def parse_api_datetime(value: str) -> datetime:
    """Parse Flask RFC1123 datetime string."""
    return datetime.strptime(value, "%a, %d %b %Y %H:%M:%S GMT")


def parse_date(value: str) -> datetime:
    """Parse YYYY-MM-DD date string."""
    return datetime.strptime(value, "%Y-%m-%d")
