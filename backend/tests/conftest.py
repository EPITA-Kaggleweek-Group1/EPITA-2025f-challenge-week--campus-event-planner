"""
Pytest fixtures for the Campus Event Planner test suite.

Creates a temporary MySQL test database for each test session.
"""

import os
import sys
from app_factory import create_app
from config.config import get_test_db_config
import pytest


sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))


@pytest.fixture(scope="session")
def test_db():
    """
    Create a test database that lives for the entire test session.
    The database is dropped when the session ends.
    """
    from database import Database

    db = Database(get_test_db_config())

    # Create the test database
    conn = db.get_admin_connection()
    cursor = conn.cursor()
    cursor.execute(f"DROP DATABASE IF EXISTS {db.config['database']}")
    cursor.execute(f"CREATE DATABASE {db.config['database']}")
    cursor.close()
    conn.close()

    # Point the app at the test database
    db.init_db()

    # Insert seed events
    conn = db.get_connection()
    cursor = conn.cursor()
    cursor.execute(
        """
        INSERT INTO events (title, description, date, location, capacity)
        VALUES (%s, %s, %s, %s, %s)
        """,
        (
            "Test Event 1",
            "Description for test event 1",
            "2026-04-15T10:00:00",
            "Room 101",
            50,
        ),
    )
    cursor.execute(
        """
        INSERT INTO events (title, description, date, location, capacity)
        VALUES (%s, %s, %s, %s, %s)
        """,
        (
            "Test Event 2",
            "Description for test event 2",
            "2026-04-20T14:00:00",
            "Room 202",
            30,
        ),
    )
    conn.commit()
    cursor.close()
    conn.close()

    yield db.config["database"]

    # Cleanup
    conn = db.get_admin_connection()
    cursor = conn.cursor()
    cursor.execute(f"DROP DATABASE IF EXISTS {db.config['database']}")
    cursor.close()
    conn.close()


@pytest.fixture()
def client(test_db):
    """
    Provide a Flask test client wired to the test database.
    """
    flask_app = create_app(get_test_db_config())

    flask_app.config["TESTING"] = True
    with flask_app.test_client() as c:
        yield c
