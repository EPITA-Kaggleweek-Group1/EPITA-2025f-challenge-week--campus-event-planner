"""
Model functions for the Campus Event Planner.

Thin data-access layer that wraps raw SQL queries and returns
plain dictionaries ready for JSON serialization.
"""

from database import get_db


def _serialize_row(row):
    """Convert datetime objects in a row dict to strings for JSON serialization."""
    if row is None:
        return None
    result = dict(row)
    for key, value in result.items():
        if hasattr(value, 'isoformat'):
            result[key] = value.isoformat()
    return result


def get_all_events():
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


def get_event_by_id(event_id):
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
