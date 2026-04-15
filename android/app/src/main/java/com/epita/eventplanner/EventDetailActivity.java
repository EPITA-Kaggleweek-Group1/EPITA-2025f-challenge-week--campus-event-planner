package com.epita.eventplanner;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.epita.eventplanner.api.ApiClient;
import com.epita.eventplanner.model.Event;

import org.json.JSONObject;

public class EventDetailActivity extends AppCompatActivity {

    private ProgressBar loadingSpinner;
    private LinearLayout errorView;
    private ScrollView detailContent;
    private Button retryButton;

    private TextView detailTitle, detailDate, detailLocation, detailCapacity, detailDescription;
    private int eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        // Initialize UI State containers
        loadingSpinner = findViewById(R.id.loadingSpinner);
        errorView = findViewById(R.id.errorView);
        detailContent = findViewById(R.id.detailContent);
        retryButton = findViewById(R.id.retryButton);

        // Initialize detail fields
        detailTitle = findViewById(R.id.detailTitle);
        detailDate = findViewById(R.id.detailDate);
        detailLocation = findViewById(R.id.detailLocation);
        detailCapacity = findViewById(R.id.detailCapacity);
        detailDescription = findViewById(R.id.detailDescription);

        eventId = getIntent().getIntExtra("event_id", -1);

        // Retry logic
        retryButton.setOnClickListener(v -> loadEventDetails(eventId));

        if (eventId != -1) {
            loadEventDetails(eventId);
        }
    }

    private void loadEventDetails(int id) {
        // Show loading state, hide others
        showLoading();

        new Thread(() -> {
            try {
                String json = ApiClient.fetchJson("/events/" + id);
                JSONObject jsonObject = new JSONObject(json);
                Event event = Event.fromJson(jsonObject);

                runOnUiThread(() -> {
                    populateUI(event);
                    showContent(); // Show success state
                });

            } catch (Exception e) {
                runOnUiThread(this::showError); // Show error state
            }
        }).start();
    }

    private void showLoading() {
        loadingSpinner.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        detailContent.setVisibility(View.GONE);
    }

    private void showContent() {
        loadingSpinner.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        detailContent.setVisibility(View.VISIBLE);
    }

    private void showError() {
        loadingSpinner.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
        detailContent.setVisibility(View.GONE);
    }

    private void populateUI(Event event) {
        detailTitle.setText(event.getTitle());
        detailDescription.setText(event.getDescription());
        detailLocation.setText(event.getLocation());
        detailDate.setText(event.getDate());
        detailCapacity.setText("Capacity: " + event.getCapacity());
    }
}