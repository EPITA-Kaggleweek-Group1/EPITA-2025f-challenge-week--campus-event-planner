package com.epita.eventplanner;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.epita.eventplanner.api.ApiClient;
import com.epita.eventplanner.databinding.ActivityEventDetailBinding;
import com.epita.eventplanner.model.Event;
import com.epita.eventplanner.util.DateUtils;

import org.json.JSONObject;

public class EventDetailActivity extends AppCompatActivity {
    private static final String TAG = "EventDetailActivity";
    private ActivityEventDetailBinding binding;
    private int eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEventDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        eventId = getIntent().getIntExtra("event_id", -1);

        binding.errorLayout.errorMessage.setText("Failed to load event details");
        binding.swipeRefreshLayout.setOnRefreshListener(() -> loadEventDetails(eventId));
        binding.errorLayout.retryButton.setOnClickListener(v -> loadEventDetails(eventId));

        if (eventId != -1) {
            loadEventDetails(eventId);
        } else {
            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadEventDetails(int id) {
        if (!binding.swipeRefreshLayout.isRefreshing()) {
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
                    binding.swipeRefreshLayout.setRefreshing(false);
                });

            } catch (Exception e) {
                Log.e(TAG, "Failed to load event details", e);
                runOnUiThread(() -> {
                    showError();
                    binding.swipeRefreshLayout.setRefreshing(false);
                });
            }
        }).start();
    }

    private void showLoading() {
        binding.loadingSpinner.setVisibility(View.VISIBLE);
        binding.errorLayout.errorView.setVisibility(View.VISIBLE);
        binding.actualContent.setVisibility(View.GONE);
        // Note: We hide error view specifically if we are loading
        binding.errorLayout.errorView.setVisibility(View.GONE);
    }

    private void showContent() {
        binding.loadingSpinner.setVisibility(View.GONE);
        binding.errorLayout.errorView.setVisibility(View.GONE);
        binding.actualContent.setVisibility(View.VISIBLE);
    }

    private void showError() {
        binding.loadingSpinner.setVisibility(View.GONE);
        binding.errorLayout.errorView.setVisibility(View.VISIBLE);
        binding.actualContent.setVisibility(View.GONE);
    }

    private void populateUI(Event event) {
        // Accessing views through the included layout_event_detail_content
        // Note: the include in activity_event_detail doesn't have an ID, 
        // but its views are merged if not using a bind-able include ID.
        // Actually, it's better to give the include an ID or access views directly if they have IDs.
        
        binding.eventDetailContent.detailTitle.setText(event.getTitle());
        binding.eventDetailContent.detailLocation.setText(event.getLocation());
        binding.eventDetailContent.detailDescription.setText(event.getDescription());
        binding.eventDetailContent.detailCapacity.setText("Capacity: " + event.getCapacity());
        binding.eventDetailContent.detailDate.setText(DateUtils.formatToHuman(event.getDate()));

        String imageUrl = event.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            binding.detailImage.setVisibility(android.view.View.VISIBLE);
            Glide.with(this).load(imageUrl).centerCrop().into(binding.detailImage);
        } else {
            binding.detailImage.setVisibility(android.view.View.GONE);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(event.getTitle());
        }
    }
}
