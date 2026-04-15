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
import com.epita.eventplanner.util.DateUtils;

import org.json.JSONObject;

public class EventDetailActivity extends AppCompatActivity {

    private static final String TAG = "EventDetailActivity";

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

        detailTitle = findViewById(R.id.detailTitle);
        detailDate = findViewById(R.id.detailDate);
        detailLocation = findViewById(R.id.detailLocation);
        detailCapacity = findViewById(R.id.detailCapacity);
        detailDescription = findViewById(R.id.detailDescription);
        detailImage = findViewById(R.id.detailImage);

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
                String jsonResponse = ApiClient.fetchJson("/events/" + id);
                JSONObject jsonObject = new JSONObject(jsonResponse);
                Event event = Event.fromJson(jsonObject);

                runOnUiThread(() -> populateUI(event));

            } catch (Exception e) {
                Log.e(TAG, "Failed to load event details", e);
                runOnUiThread(() -> Toast.makeText(this, "Failed to load event details", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void populateUI(Event event) {
        detailTitle.setText(event.getTitle());
        detailLocation.setText(event.getLocation());
        detailDescription.setText(event.getDescription());
        detailCapacity.setText("Capacity: " + event.getCapacity());
        detailDate.setText(DateUtils.formatToHuman(event.getDate()));

        String imageUrl = event.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            detailImage.setVisibility(android.view.View.VISIBLE);
            Glide.with(this).load(imageUrl).centerCrop().into(detailImage);
        } else {
            detailImage.setVisibility(android.view.View.GONE);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(event.getTitle());
        }
    }
}
