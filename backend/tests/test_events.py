"""
Test suite for the Campus Event Planner API.

Tests marked with 'xfail' are EXPECTED TO FAIL because the corresponding
features have not been implemented yet. Students must make these tests pass.
"""

import json
import pytest
from werkzeug.wrappers import response

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
        data = response.get_json()
        assert isinstance(data, list)
        assert len(data) >= 2  # we seeded at least 2 events

    def test_events_contain_required_fields(self, client):
        """Each event object should include the core fields."""
        response = client.get("/events")
        data = response.get_json()
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

    @pytest.mark.xfail(reason="Registrations count endpoint not implemented yet")
    def test_registrations_count(self, client):
        """GET /events/<id>/registrations should return registration count."""
        response = client.get("/events/1/registrations")
        assert response.status_code == 200
        data = response.get_json()
        assert "count" in data
        assert isinstance(data["count"], int)


class TestSearch:
    """Search and filter (NOT YET IMPLEMENTED)."""

    @pytest.mark.xfail(reason="Search endpoint not implemented yet")
    def test_event_search(self, client):
        """GET /events?search=test should filter events by keyword."""
        response = client.get("/events?search=Test")
        assert response.status_code == 200
        data = response.get_json()
        assert isinstance(data, list)
        # All returned events should contain 'Test' in the title
        for event in data:
            assert "test" in event["title"].lower()
