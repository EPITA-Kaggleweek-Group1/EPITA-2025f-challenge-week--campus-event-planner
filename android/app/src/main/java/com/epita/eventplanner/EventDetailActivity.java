package com.epita.eventplanner;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.epita.eventplanner.api.ApiClient;
import com.epita.eventplanner.model.Event;
import org.json.JSONObject;

/**
 * Detail screen for a single event.
 * Implements TODO 1: Fetching specific event data on load.
 */
public class EventDetailActivity extends AppCompatActivity {

  private static final String TAG = "EventDetailActivity";

  // UI References based on your activity_event_detail.xml
  private TextView detailTitle;
  private TextView detailDate;
  private TextView detailLocation;
  private TextView detailCapacity;
  private TextView detailDescription;
  private ImageView detailImage;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_event_detail);

    // 1. Initialize the views from your layout
    detailTitle = findViewById(R.id.detailTitle);
    detailDate = findViewById(R.id.detailDate);
    detailLocation = findViewById(R.id.detailLocation);
    detailCapacity = findViewById(R.id.detailCapacity);
    detailDescription = findViewById(R.id.detailDescription);
    detailImage = findViewById(R.id.detailImage);

    // 2. Read the "event_id" extra passed from MainActivity
    int eventId = getIntent().getIntExtra("event_id", -1);

    if (eventId != -1) {
      // 3. Call GET /events/<id> via ApiClient
      loadEventDetails(eventId);
    } else {
      Toast.makeText(this, "Error: Event ID not found", Toast.LENGTH_SHORT)
          .show();
      finish();
    }
  }

  /**
   * Fetches event details from the backend on a background thread.
   */
  private void loadEventDetails(int id) {
    new Thread(() -> {
      try {
        // Execute the network request
        String jsonResponse = ApiClient.fetchJson("/events/" + id);

        // Parse the JSON into an Event object using your model's factory
        JSONObject jsonObject = new JSONObject(jsonResponse);
        Event event = Event.fromJson(jsonObject);

        // 4. Update the UI on the main thread
        runOnUiThread(() -> populateUI(event));

      } catch (Exception e) {
        Log.e(TAG, "Failed to load event details", e);
        runOnUiThread(()
                          -> Toast
                                 .makeText(this, "Failed to load event details",
                                           Toast.LENGTH_SHORT)
                                 .show());
      }
    }).start();
  }

  /**
   * Populates the TextViews with data from the Event object.
   */
  private void populateUI(Event event) {
    detailTitle.setText(event.getTitle());
    detailLocation.setText(event.getLocation());
    detailDescription.setText(event.getDescription());

    // Display capacity
    detailCapacity.setText("Capacity: " + event.getCapacity());

    // Format date (For now using raw string, you can add a formatter later)
    detailDate.setText(event.getDate());

    // Load image
    String imageUrl = event.getImageUrl();
    if (imageUrl != null && !imageUrl.isEmpty()) {
      detailImage.setVisibility(android.view.View.VISIBLE);
      Glide.with(this)
          .load(imageUrl)
          .centerCrop()
          .into(detailImage);
    } else {
      detailImage.setVisibility(android.view.View.GONE);
    }

    // Set the Activity Title to the Event Name for better UX
    if (getSupportActionBar() != null) {
      getSupportActionBar().setTitle(event.getTitle());
    }
  }
}