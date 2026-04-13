package com.epita.eventplanner;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Detail screen for a single event.
 *
 * The layout is already wired up with placeholder TextViews, but this
 * activity does NOT yet fetch data from the API.
 *
 * TODO (students):
 *   1. Read the "event_id" extra from the Intent
 *   2. Call GET /events/<id> via ApiClient.fetchJson()
 *   3. Parse the JSON and populate the TextViews
 *   4. Display a formatted date (e.g. "Saturday 18 April 2026 at 17:00")
 *   5. Show remaining spots (requires GET /events/<id>/registrations)
 *   6. Add a "Register" button that calls POST /events/<id>/register
 */
public class EventDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        // The event_id is passed via the Intent — students will use this
        int eventId = getIntent().getIntExtra("event_id", -1);

        // TODO: fetch event details and populate the UI
        // For now, show the placeholder layout only.
    }
}
