# IDOR — `DELETE /events/:id/registrations`

**Severity:** High
**File:** `backend/app.py`

FFix — verify ownership before deleting
@app.route("/events/<int:event_id>/registrations", methods=["DELETE"])
@require_auth          # add authentication first
def delete_registration(event_id):
    reg_id = request.args.get("reg_id")
    # Verify this reg belongs to this event (and optionally the caller)
    cursor.execute(
        "SELECT id FROM registrations WHERE id=%s AND event_id=%s",
        (reg_id, event_id)
    )
    if not cursor.fetchone():
        return jsonify({"error": "Not found"}), 404
    cursor.execute("DELETE FROM registrations WHERE id=%s", (reg_id,))



