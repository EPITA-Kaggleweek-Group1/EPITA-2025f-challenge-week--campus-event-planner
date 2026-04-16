"""
Service for the Campus Event Planner

This is a layer for implementing related services, but not modify the models
"""


def service_filter_events(conn, search=None, title=None, description=None):
    cursor = conn.cursor(dictionary=True)

    conditions = []
    params = []

    # 1. global search (OR across fields)
    if search:
        conditions.append("(title LIKE %s OR description LIKE %s OR location LIKE %s)")
        like = f"%{search}%"
        params.extend([like, like, like])

    # 2. specific title filter
    if title:
        conditions.append("title LIKE %s")
        params.append(f"%{title}%")

    # 3. specific description filter
    if description:
        conditions.append("description LIKE %s")
        params.append(f"%{description}%")

    # build WHERE clause
    where_clause = ""
    if conditions:
        where_clause = "WHERE " + " AND ".join(conditions)

    query = f"""
        SELECT *
        FROM events
        {where_clause}
        ORDER BY date
    """

    cursor.execute(query, tuple(params))
    results = cursor.fetchall()
    cursor.close()
    return results
