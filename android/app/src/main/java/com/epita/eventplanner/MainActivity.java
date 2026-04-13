package com.epita.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.epita.eventplanner.adapter.EventAdapter;
import com.epita.eventplanner.api.ApiClient;
import com.epita.eventplanner.model.Event;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Main screen: displays a scrollable list of campus events fetched from
 * the Flask backend via GET /events.
 *
 * TODO (students):
 *   - Hook up the search bar to filter events (GET /events?search=...)
 *   - Implement pull-to-refresh
 *   - Navigate to EventDetailActivity on item click
 */
public class MainActivity extends AppCompatActivity implements EventAdapter.OnEventClickListener {

    private static final String TAG = "MainActivity";

    private RecyclerView recyclerView;
    private EventAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up RecyclerView
        recyclerView = findViewById(R.id.eventsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter(this);
        recyclerView.setAdapter(adapter);

        // Fetch events from the API
        loadEvents();
    }

    /**
     * Fetch all events from the backend on a background thread and
     * update the adapter on the UI thread.
     */
    private void loadEvents() {
        new Thread(() -> {
            try {
                String json = ApiClient.fetchJson("/events");
                JSONArray array = new JSONArray(json);
                List<Event> events = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    events.add(Event.fromJson(obj));
                }

                // Update UI on the main thread
                runOnUiThread(() -> adapter.setEvents(events));

            } catch (Exception e) {
                Log.e(TAG, "Failed to load events", e);
                runOnUiThread(() ->
                        Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    @Override
    public void onEventClick(Event event) {
        // TODO (students): pass the event ID to EventDetailActivity and load
        // full details from GET /events/<id>
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra("event_id", event.getId());
        startActivity(intent);
    }
}
