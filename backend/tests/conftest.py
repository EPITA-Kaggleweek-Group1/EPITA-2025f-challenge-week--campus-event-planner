"""
Pytest fixtures for the Campus Event Planner test suite.

Creates a temporary MySQL test database for each test session.
"""

import os
import sys
import pytest
import mysql.connector

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

TEST_DB_NAME = "test_campus_events"


@pytest.fixture(scope="session")
def test_db():
    """
    Create a test database that lives for the entire test session.
    The database is dropped when the session ends.
    """
    import database

    # Create the test database
    config_no_db = {k: v for k, v in database.DB_CONFIG.items() if k != "database"}
    conn = mysql.connector.connect(**config_no_db)
    cursor = conn.cursor()
    cursor.execute(f"DROP DATABASE IF EXISTS {TEST_DB_NAME}")
    cursor.execute(f"CREATE DATABASE {TEST_DB_NAME}")
    cursor.close()
    conn.close()

    # Point the app at the test database
    database.DB_CONFIG["database"] = TEST_DB_NAME
    database.init_db()

    # Insert seed events
    db = database.get_db()
    cursor = db.cursor()
    cursor.execute(
        """
        INSERT INTO events (title, description, date, location, capacity)
        VALUES (%s, %s, %s, %s, %s)
        """,
        ("Test Event 1", "Description for test event 1", "2026-04-15T10:00:00", "Room 101", 50),
    )
    cursor.execute(
        """
        INSERT INTO events (title, description, date, location, capacity)
        VALUES (%s, %s, %s, %s, %s)
        """,
        ("Test Event 2", "Description for test event 2", "2026-04-20T14:00:00", "Room 202", 30),
    )
    db.commit()
    cursor.close()
    db.close()

    yield TEST_DB_NAME

    # Cleanup
    conn = mysql.connector.connect(**config_no_db)
    cursor = conn.cursor()
    cursor.execute(f"DROP DATABASE IF EXISTS {TEST_DB_NAME}")
    cursor.close()
    conn.close()


@pytest.fixture()
def client(test_db):
    """
    Provide a Flask test client wired to the test database.
    """
    from app import app as flask_app

    flask_app.config["TESTING"] = True
    with flask_app.test_client() as c:
        yield c
