"""
Service for the Campus Event Planner

This is a layer for implementing related services, but not modify the models
"""


def service_filter_events(
    conn,
    search=None,
    title=None,
    description=None,
    date_from=None,
    date_to=None,
):
    cursor = conn.cursor(dictionary=True)

    conditions = []
    params = []

    # 1. global search (OR group)
    if search:
        conditions.append("(title LIKE %s OR description LIKE %s OR location LIKE %s)")
        like = f"%{search}%"
        params.extend([like, like, like])

    # 2. field filters (AND group)
    if title:
        conditions.append("title LIKE %s")
        params.append(f"%{title}%")

    if description:
        conditions.append("description LIKE %s")
        params.append(f"%{description}%")

    # 3. date range filters
    if date_from:
        conditions.append("date >= %s")
        params.append(date_from)

    if date_to:
        conditions.append("date <= %s")
        params.append(date_to)

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
