"""
Campus Event Planner — Flask API

Endpoints implemented:
    GET  /events       — list all events
    GET  /events/<id>  — single event detail
    POST /events       — create a new event

Students must add:
    POST /events/<id>/register        — register for an event
    GET  /events/<id>/registrations   — list registrations / count
    GET  /events?search=&date=        — search & filter
"""

import html
from flask import jsonify, request
from models import (
    AlreadyRegisteredError,
    EventFullError,
    EventNotFoundError,
    get_event_by_id,
    create_event,
    registration_create,
    registration_get_all,
    registration_get_count,
)
from service import service_filter_events, service_update_event


def register_events_routes(app):
    @app.after_request
    def add_header(response):
        response.headers["X-Powered-By"] = "Flask/2.3.2 Python/3.11"
        return response  # Allow cross-origin requests from the mobile apps

    # --------------------------------------------------------------------------- #
    #  Health check
    # --------------------------------------------------------------------------- #

    @app.route("/", methods=["GET"])
    def index():
        """Simple health-check endpoint."""
        return jsonify({"message": "Campus Event Planner API is running"}), 200

    # --------------------------------------------------------------------------- #
    #  Events
    # --------------------------------------------------------------------------- #

    @app.route("/events", methods=["GET"])
    def list_events():
        """
        Return all events as JSON array.

        Supported filters:
        - ?search=
        - ?title=
        - ?description=
        - ?date_from=
        - ?date_to= (exclusive)
        - ?order="asc" | "desc" (default)
        """
        search = request.args.get("search")
        title = request.args.get("title")
        description = request.args.get("description")
        date_from = request.args.get("date_from")
        date_to = request.args.get("date_to")
        order = request.args.get("order", "asc")

        conn = app.db.get_connection()

        try:
            events = service_filter_events(
                conn,
                search=search,
                title=title,
                description=description,
                date_from=date_from,
                date_to=date_to,
                order=order,
            )

            return jsonify(events), 200

        finally:
            conn.close()

    @app.route("/events/<int:event_id>", methods=["GET"])
    def get_event(event_id):
        """Return a single event by ID, or 404 if it does not exist."""
        conn = app.db.get_connection()
        event = get_event_by_id(conn, event_id)
        try:
            if event is None:
                return jsonify({"error": "Event not found"}), 404
            return jsonify(event), 200
        finally:
            conn.close()

    @app.route("/events", methods=["POST"])
    def add_event():
        """
        Create a new event.

        Expects JSON body with at least 'title' and 'date'.
        Returns the created event with status 201.
        """
        conn = app.db.get_connection()
        data = request.get_json()
        if not data:
            return jsonify({"error": "Request body must be JSON"}), 400
        if "title" not in data or "date" not in data:
            return jsonify({"error": "'title' and 'date' are required"}), 400

        event = create_event(conn, data)
        conn.close()
        return jsonify(event), 201

    # --------------------------------------------------------------------------- #
    #  Registrations — NOT IMPLEMENTED
    #  Students must create endpoints here:
    #    POST /events/<id>/register
    #    GET  /events/<id>/registrations
    # ---------------------------------------------------------------------------
    @app.route("/events/<int:event_id>/registrations", methods=["GET"])
    def get_event_registrations(event_id: int):
        """
        Return a list of registrations based on event ID.

        Returns:
        200 - Success. A list of the registrations.
        404 - If the event does not exist.
        """
        conn = app.db.get_connection()
        registrations = registration_get_all(conn, event_id)
        conn.close()
        if registrations is None:
            return jsonify({"error": "Event not found"}), 404

        return jsonify(registrations), 200

    @app.route("/events/<int:event_id>/register", methods=["POST"])
    def add_event_registration(event_id: int):
        """
        Create a new register.

        Expects JSON body with at least 'email' and 'user_name'.
        Returns
        201 - The created registration.
        400 - If the request is not valid.
        404 - If the event is not presented.
        409 - If the user already registered, or the event is full.
        """
        conn = app.db.get_connection()
        try:
            data = request.get_json()
            if not data:
                return jsonify({"error": "Request body must be JSON"}), 400
            if "user_name" not in data or "email" not in data:
                return jsonify({"error": "'user_name' and 'email' are required"}), 400

            result = registration_create(conn, event_id, data)
            return jsonify(result), 201
        except EventNotFoundError:
            return jsonify({"error": "Event not found"}), 404
        except AlreadyRegisteredError:
            return jsonify({"error": "Registration already exist"}), 409
        except EventFullError:
            return jsonify({"error": "Event is full"}), 409
        except Exception:
            return jsonify({"error": "Internal server error"}), 500
        finally:
            conn.close()

    @app.route("/events/<int:event_id>/registrations/count", methods=["GET"])
    def get_event_registration_count(event_id: int):
        """
        Return
        200 - Count of the registered {"event_id": <id>, "count": <count>}
        404 - If event not found
        """
        conn = app.db.get_connection()
        try:
            count = registration_get_count(conn, event_id)
            return {"event_id": event_id, "count": count}, 200
        except EventNotFoundError:
            return jsonify({"error": "Event not found"}), 404
        finally:
            conn.close()

    # --------------------------------------------------------------------------- #
    #  Run
    # --------------------------------------------------------------------------- #

    @app.route("/events/<int:event_id>/registrations", methods=["DELETE"])
    def delete_registration(event_id):
        reg_id = request.args.get("reg_id")
        conn = app.db.get_connection()
        cursor = conn.cursor()
        cursor.execute("DELETE FROM registrations WHERE id = %s", (reg_id,))
        conn.commit()
        cursor.close()
        conn.close()
        return jsonify({"message": "Registration deleted"}), 200

    @app.route("/admin")
    def admin_page():
        conn = app.db.get_connection()
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT * FROM events ORDER BY date DESC")
        events = cursor.fetchall()
        cursor.close()
        conn.close()

        html_content = "<html><head><title>Admin - Events</title></head><body>"
        html_content += "<h1>Event Admin Panel</h1>"

        for e in events:
            title = html.escape(e["title"] or "")
            description = html.escape(e["description"] or "")

            html_content += f"""
                <div class='event'>
                    <h3>{title}</h3>
                    <p>{description}</p>
                </div>
            """

        html_content += "</body></html>"
        return html_content

    @app.route("/events/<int:event_id>", methods=["PATCH"])
    def update_event(event_id):
        data = request.get_json() or {}

        conn = app.db.get_connection()
        try:
            service_update_event(conn, event_id, data)
            return jsonify({"message": "Event updated"}), 200
        except ValueError as e:
            return jsonify({"error": str(e)}), 400
        finally:
            conn.close()
