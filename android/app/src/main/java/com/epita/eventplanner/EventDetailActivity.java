package com.epita.eventplanner;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.epita.eventplanner.api.ApiClient;
import com.epita.eventplanner.model.Event;
import com.epita.eventplanner.util.DateUtils;

import org.json.JSONObject;

public class EventDetailActivity extends AppCompatActivity {
    private static final String TAG = "EventDetailActivity";

    private ProgressBar loadingSpinner;
    private View errorView;
    private ScrollView detailContent;
    private View actualContent;
    private Button retryButton;
    private TextView errorMessage;
    private SwipeRefreshLayout swipeRefreshLayout;

    private int eventId;

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

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        loadingSpinner = findViewById(R.id.loadingSpinner);
        errorView = findViewById(R.id.errorView);
        detailContent = findViewById(R.id.detailContent);
        actualContent = findViewById(R.id.actualContent);
        retryButton = findViewById(R.id.retryButton);
        errorMessage = findViewById(R.id.errorMessage);

        detailTitle = findViewById(R.id.detailTitle);
        detailDate = findViewById(R.id.detailDate);
        detailLocation = findViewById(R.id.detailLocation);
        detailCapacity = findViewById(R.id.detailCapacity);
        detailDescription = findViewById(R.id.detailDescription);
        detailImage = findViewById(R.id.detailImage);

        eventId = getIntent().getIntExtra("event_id", -1);

        errorMessage.setText("Failed to load event details");
        swipeRefreshLayout.setOnRefreshListener(() -> loadEventDetails(eventId));
        retryButton.setOnClickListener(v -> loadEventDetails(eventId));

        if (eventId != -1) {
            loadEventDetails(eventId);
        } else {
            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadEventDetails(int id) {
        if (!swipeRefreshLayout.isRefreshing()) {
            showLoading();
        }

        new Thread(() -> {
            try {
                String jsonResponse = ApiClient.fetchJson("/events/" + id);
                JSONObject jsonObject = new JSONObject(jsonResponse);
                Event event = Event.fromJson(jsonObject);

                runOnUiThread(() -> {
                    populateUI(event);
                    showContent();
                    swipeRefreshLayout.setRefreshing(false);
                });

            } catch (Exception e) {
                Log.e(TAG, "Failed to load event details", e);
                runOnUiThread(() -> {
                    showError();
                    swipeRefreshLayout.setRefreshing(false);
                });
            }
        }).start();
    }

    private void showLoading() {
        loadingSpinner.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        actualContent.setVisibility(View.GONE);
    }

    private void showContent() {
        loadingSpinner.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        actualContent.setVisibility(View.VISIBLE);
    }

    private void showError() {
        loadingSpinner.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
        actualContent.setVisibility(View.GONE);
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
