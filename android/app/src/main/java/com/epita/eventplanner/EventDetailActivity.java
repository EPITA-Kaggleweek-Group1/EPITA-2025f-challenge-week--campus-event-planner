package com.epita.eventplanner;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView; // Added for the image requirement
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.epita.eventplanner.api.ApiClient;
import com.epita.eventplanner.model.Event;

import org.json.JSONObject;

public class EventDetailActivity extends AppCompatActivity {

    private static final String TAG = "EventDetailActivity";

    private TextView detailTitle, detailDate, detailLocation, detailCapacity, detailDescription;
    // Note: You will need an ImageView in your XML with this ID to show the image
    private ImageView detailImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        // Initialize all UI components
        detailTitle = findViewById(R.id.detailTitle);
        detailDate = findViewById(R.id.detailDate);
        detailLocation = findViewById(R.id.detailLocation);
        detailCapacity = findViewById(R.id.detailCapacity);
        detailDescription = findViewById(R.id.detailDescription);
        // detailImage = findViewById(R.id.detailImage); // Initialize if added to XML

        int eventId = getIntent().getIntExtra("event_id", -1);

        if (eventId != -1) {
            loadEventDetails(eventId);
        } else {
            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadEventDetails(int id) {
        new Thread(() -> {
            try {
                // Fetch single event by ID
                String jsonResponse = ApiClient.fetchJson("/events/" + id);

                // Parse JSON into Event object
                JSONObject jsonObject = new JSONObject(jsonResponse);
                Event event = Event.fromJson(jsonObject);

                // Update UI on main thread
                runOnUiThread(() -> populateUI(event));

            } catch (Exception e) {
                Log.e(TAG, "Error fetching event " + id, e);
                runOnUiThread(() ->
                        Toast.makeText(this, "Error loading details", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private void populateUI(Event event) {
        // Populate Title, Description, and Location
        detailTitle.setText(event.getTitle());
        detailDescription.setText(event.getDescription());
        detailLocation.setText(event.getLocation());

        // Populate Capacity
        detailCapacity.setText("Capacity: " + event.getCapacity());

        // Populate Date (Formatted according to TODO 1 requirements)
        detailDate.setText(event.getDate());

        // TODO: Use a library like Glide to load event.getImageUrl() into detailImage
        // Example: Glide.with(this).load(event.getImageUrl()).into(detailImage);
    }
}