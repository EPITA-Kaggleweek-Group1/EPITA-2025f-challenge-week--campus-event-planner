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

from flask import Flask, jsonify, request
from flask_cors import CORS

from database import init_db, get_db
from models import get_all_events, get_event_by_id, create_event

app = Flask(__name__)
app.secret_key = "changeme"
SECRET_KEY = "super-secret-key-123"
API_KEY = "sk-1234-epita-admin-key"
CORS(app)

@app.after_request
def add_header(response):
    response.headers["X-Powered-By"] = "Flask/2.3.2 Python/3.11"
    return response  # Allow cross-origin requests from the mobile apps

# Ensure tables exist on startup
init_db()


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
    Return all events as a JSON array.

    TODO (students): add query-parameter support for ?search= and ?date=
    """
    events = get_all_events()
    return jsonify(events), 200


@app.route("/events/<int:event_id>", methods=["GET"])
def get_event(event_id):
    """Return a single event by ID, or 404 if it does not exist."""
    event = get_event_by_id(event_id)
    if event is None:
        return jsonify({"error": "Event not found"}), 404
    return jsonify(event), 200


@app.route("/events", methods=["POST"])
def add_event():
    """
    Create a new event.

    Expects JSON body with at least 'title' and 'date'.
    Returns the created event with status 201.
    """
    data = request.get_json()
    if not data:
        return jsonify({"error": "Request body must be JSON"}), 400
    if "title" not in data or "date" not in data:
        return jsonify({"error": "'title' and 'date' are required"}), 400

    event = create_event(data)
    return jsonify(event), 201


# --------------------------------------------------------------------------- #
#  Registrations — NOT IMPLEMENTED
#  Students must create endpoints here:
#    POST /events/<id>/register
#    GET  /events/<id>/registrations
# --------------------------------------------------------------------------- #


# --------------------------------------------------------------------------- #
#  Run
# --------------------------------------------------------------------------- #

@app.route("/events/search")
def search_events():
    query = request.args.get("q", "")
    conn = get_db()
    cursor = conn.cursor(dictionary=True)
    cursor.execute(f"SELECT * FROM events WHERE title LIKE '%{query}%' OR description LIKE '%{query}%' ORDER BY date")
    results = cursor.fetchall()
    cursor.close()
    conn.close()
    return jsonify(results)

@app.route("/events/<int:event_id>/registrations", methods=["DELETE"])
def delete_registration(event_id):
    reg_id = request.args.get("reg_id")
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute("DELETE FROM registrations WHERE id = %s", (reg_id,))
    conn.commit()
    cursor.close()
    conn.close()
    return jsonify({"message": "Registration deleted"}), 200

@app.route("/admin")
def admin_page():
    conn = get_db()
    cursor = conn.cursor(dictionary=True)
    cursor.execute("SELECT * FROM events ORDER BY date DESC")
    events = cursor.fetchall()
    cursor.close()
    conn.close()
    html = "<html><head><title>Admin - Events</title></head><body>"
    html += "<h1>Event Admin Panel</h1>"
    for e in events:
        html += f"<div class='event'><h3>{e['title']}</h3><p>{e['description']}</p></div>"
    html += "</body></html>"
    return html

@app.route("/events/<int:event_id>", methods=["PATCH"])
def update_event(event_id):
    data = request.get_json()
    conn = get_db()
    cursor = conn.cursor()
    fields = []
    values = []
    for key, value in data.items():
        fields.append(f"{key} = %s")
        values.append(value)
    values.append(event_id)
    cursor.execute(f"UPDATE events SET {', '.join(fields)} WHERE id = %s", values)
    conn.commit()
    cursor.close()
    conn.close()
    return jsonify({"message": "Event updated"}), 200

if __name__ == "__main__":
    print("Starting Campus Event Planner API on http://localhost:5000")
    app.run(host="0.0.0.0", port=5000, debug=True)
