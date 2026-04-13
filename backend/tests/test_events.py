"""
Test suite for the Campus Event Planner API.

Tests marked with 'xfail' are EXPECTED TO FAIL because the corresponding
features have not been implemented yet. Students must make these tests pass.
"""

import json
import pytest


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
        for field in ("id", "title", "date", "location", "capacity"):
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

class TestRegistration:
    """Registration endpoints (NOT YET IMPLEMENTED)."""

    @pytest.mark.xfail(reason="Registration endpoint not implemented yet")
    def test_register_for_event(self, client):
        """POST /events/<id>/register should create a registration."""
        payload = {
            "user_name": "Alice Dupont",
            "email": "alice.dupont@epita.fr",
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
