"""
Service for the Campus Event Planner

This is a layer for implementing related services, but not modify the models
"""

from typing import Any, Dict, List

from mysql.connector.types import RowItemType


def service_search_events(
    conn,
    query: str,
) -> List[RowItemType | Dict[str, RowItemType]] | Any:
    cursor = conn.cursor(dictionary=True)
    # Fixed
    cursor.execute(
        "SELECT * FROM events WHERE title LIKE %s OR description LIKE %s ORDER BY date",
        (f"%{query}%", f"%{query}%"),
    )
    results = cursor.fetchall()
    cursor.close()
    return results
