"""
Model functions for the Campus Event Planner.

Thin data-access layer that wraps raw SQL queries and returns
plain dictionaries ready for JSON serialization.
"""

from typing import List

from mysql.connector.types import RowItemType
from database import get_db


class EventFullError(Exception):
    pass


class EventNotFoundError(Exception):
    pass


class AlreadyRegisteredError(Exception):
    pass


def _serialize_row(row):
    """Convert datetime objects in a row dict to strings for JSON serialization."""
    if row is None:
        return None
    result = dict(row)
    for key, value in result.items():
        if hasattr(value, "isoformat"):
            result[key] = value.isoformat()
    return result


def get_all_events() -> List[dict[str, RowItemType] | None]:
    """
    Retrieve every event, ordered by date ascending.

    Returns:
        list[dict]: A list of event dictionaries.
    """
    conn = get_db()
    cursor = conn.cursor(dictionary=True)
    cursor.execute("SELECT * FROM events ORDER BY date ASC")
    rows = cursor.fetchall()
    cursor.close()
    conn.close()
    return [_serialize_row(r) for r in rows]


def get_event_by_id(event_id: int) -> dict | None:
    """
    Retrieve a single event by its primary key.

    Args:
        event_id (int): The event ID.

    Returns:
        dict or None: The event dictionary, or None if not found.
    """
    conn = get_db()
    cursor = conn.cursor(dictionary=True)
    cursor.execute("SELECT * FROM events WHERE id = %s", (event_id,))
    row = cursor.fetchone()
    cursor.close()
    conn.close()
    return _serialize_row(row)


def create_event(data):
    """
    Insert a new event into the database.

    Args:
        data (dict): Must contain at least 'title' and 'date'.
                     Optional: 'description', 'location', 'capacity', 'image_url'.

    Returns:
        dict: The newly created event (including its generated id).
    """
    conn = get_db()
    cursor = conn.cursor(dictionary=True)
    cursor.execute(
        """
        INSERT INTO events (title, description, date, location, capacity, image_url)
        VALUES (%s, %s, %s, %s, %s, %s)
        """,
        (
            data["title"],
            data.get("description", ""),
            data["date"],
            data.get("location", ""),
            data.get("capacity", 50),
            data.get("image_url", ""),
        ),
    )
    conn.commit()
    new_id = cursor.lastrowid
    cursor.execute("SELECT * FROM events WHERE id = %s", (new_id,))
    event = _serialize_row(cursor.fetchone())
    cursor.close()
    conn.close()
    return event


def registration_get_all(event_id: int):
    conn = get_db()
    cursor = conn.cursor(dictionary=True)

    cursor.execute("SELECT * FROM events WHERE id = %s", (event_id,))
    event = cursor.fetchone()

    if event is None:
        return None

    cursor.execute(
        """
        SELECT id, user_name, email, created_at
        FROM registrations
        WHERE event_id = %s
        """,
        (event_id,),
    )
    registrations = cursor.fetchall()

    cursor.close()
    conn.close()
    return [_serialize_row(r) for r in registrations]


def registration_create(conn, event_id, data):
    cursor = conn.cursor(dictionary=True)

    cursor.execute("SELECT id, capacity FROM events WHERE id = %s", (event_id,))
    event = cursor.fetchone()
    if not event:
        raise EventNotFoundError()
    try:
        cursor.execute(
            """
            INSERT INTO registrations (event_id, user_name, email)
            VALUES (%s, %s, %s)
        """,
            (event_id, data["user_name"], data["email"]),
        )

        conn.commit()

        new_id = cursor.lastrowid
        cursor.execute("SELECT * FROM registrations WHERE id = %s", (new_id,))
        registration = _serialize_row(cursor.fetchone())

        return registration

    except Exception as e:
        if "Duplicate entry" in str(e):
            raise AlreadyRegisteredError()
        raise

    finally:
        cursor.close()
