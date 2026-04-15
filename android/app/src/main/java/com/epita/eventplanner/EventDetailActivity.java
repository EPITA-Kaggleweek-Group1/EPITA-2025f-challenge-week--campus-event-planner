package com.epita.eventplanner;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.epita.eventplanner.api.ApiClient;
import com.epita.eventplanner.model.Event;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONObject;

public class EventDetailActivity extends AppCompatActivity {
    private ProgressBar loadingSpinner;
    private LinearLayout errorView;
    private ScrollView detailContent;
    private Button retryButton, registerButton;
    private TextView detailTitle, detailDate, detailLocation, detailCapacity, detailDescription, remainingSpots;
    private ImageView detailImage;
    private int eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        loadingSpinner = findViewById(R.id.loadingSpinner);
        errorView = findViewById(R.id.errorView);
        detailContent = findViewById(R.id.detailContent);
        retryButton = findViewById(R.id.retryButton);
        registerButton = findViewById(R.id.registerButton);
        remainingSpots = findViewById(R.id.remainingSpots);
        detailImage = findViewById(R.id.detailImage);
        detailTitle = findViewById(R.id.detailTitle);
        detailDate = findViewById(R.id.detailDate);
        detailLocation = findViewById(R.id.detailLocation);
        detailCapacity = findViewById(R.id.detailCapacity);
        detailDescription = findViewById(R.id.detailDescription);

        eventId = getIntent().getIntExtra("event_id", -1);
        retryButton.setOnClickListener(v -> loadEventDetails(eventId));
        registerButton.setOnClickListener(v -> showRegisterDialog());

        if (eventId != -1) loadEventDetails(eventId);
    }

    private void loadEventDetails(int id) {
        showLoading();
        new Thread(() -> {
            try {
                // 1. Mandatory: Fetch Event Basic Info
                String eventJson = ApiClient.fetchJson("/events/" + id);
                Event event = Event.fromJson(new JSONObject(eventJson));

                // 2. Optional: Fetch Registration Count (Nested Try-Catch)
                int regCount = 0;
                try {
                    String regJson = ApiClient.fetchJson("/events/" + id + "/registrations");
                    regCount = new JSONObject(regJson).optInt("count", 0);
                } catch (Exception e) {
                    Log.e("API_WARNING", "Registration count failed, but showing event anyway.");
                }

                int finalRegCount = regCount;
                runOnUiThread(() -> {
                    populateUI(event, finalRegCount);
                    showContent();
                });

            } catch (Exception e) {
                Log.e("API_ERROR", "Core event fetch failed", e);
                runOnUiThread(this::showError);
            }
        }).start();
    }

    private void populateUI(Event event, int regCount) {
        detailTitle.setText(event.getTitle());
        detailDescription.setText(event.getDescription());
        detailLocation.setText(event.getLocation());
        detailDate.setText(event.getDate());
        detailCapacity.setText("Capacity: " + event.getCapacity());

        int left = event.getCapacity() - regCount;
        remainingSpots.setText("Spots remaining: " + (left < 0 ? 0 : left));

        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(event.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(detailImage);
        }
    }

    private void showRegisterDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_register, null);
        TextInputEditText nameInput = view.findViewById(R.id.editName);
        TextInputEditText emailInput = view.findViewById(R.id.editEmail);

        new AlertDialog.Builder(this)
                .setTitle("Register")
                .setView(view)
                .setPositiveButton("Submit", (d, w) -> {
                    submitRegistration(nameInput.getText().toString(), emailInput.getText().toString());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void submitRegistration(String name, String email) {
        new Thread(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("user_name", name);
                body.put("email", email);
                ApiClient.postJson("/events/" + eventId + "/register", body.toString());
                runOnUiThread(() -> {
                    Toast.makeText(this, "Successfully Registered!", Toast.LENGTH_SHORT).show();
                    loadEventDetails(eventId);
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Registration currently unavailable.", Toast.LENGTH_LONG).show());
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
        detailContent.setVisibility(View.VISIBLE);
    }

    private void showError() {
        loadingSpinner.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
    }
}