package com.epita.eventplanner;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

// Crucial Imports to resolve your errors:
import com.epita.eventplanner.api.ApiClient;
import com.epita.eventplanner.model.Event;
import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

public class EventDetailActivity extends AppCompatActivity {
    private ProgressBar loadingSpinner;
    private LinearLayout errorView;
    private ScrollView detailContent;
    private TextView detailTitle, detailDescription, remainingSpots;
    private ImageView detailImage;
    private Button registerButton, retryButton;
    private int eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        loadingSpinner = findViewById(R.id.loadingSpinner);
        errorView = findViewById(R.id.errorView);
        detailContent = findViewById(R.id.detailContent);
        remainingSpots = findViewById(R.id.remainingSpots);
        detailTitle = findViewById(R.id.detailTitle);
        detailDescription = findViewById(R.id.detailDescription);
        detailImage = findViewById(R.id.detailImage);
        registerButton = findViewById(R.id.registerButton);
        retryButton = findViewById(R.id.retryButton);

        eventId = getIntent().getIntExtra("event_id", -1);

        retryButton.setOnClickListener(v -> loadEventDetails(eventId));
        registerButton.setOnClickListener(v -> showRegisterDialog());

        if (eventId != -1) loadEventDetails(eventId);
    }

    private void loadEventDetails(int id) {
        showLoading();
        new Thread(() -> {
            try {
                String eventJson = ApiClient.fetchJson("/events/" + id);
                Event event = Event.fromJson(new JSONObject(eventJson));

                int regCount = 0;
                try {
                    String regJson = ApiClient.fetchJson("/events/" + id + "/registrations");
                    regCount = new JSONObject(regJson).optInt("count", 0);
                } catch (Exception e) {
                    Log.w("API", "Registration count failed, skipping color logic");
                }

                int finalRegCount = regCount;
                runOnUiThread(() -> {
                    updateUI(event, finalRegCount);
                    showContent();
                });
            } catch (Exception e) {
                runOnUiThread(this::showError);
            }
        }).start();
    }

    private void updateUI(Event event, int regCount) {
        detailTitle.setText(event.getTitle());
        detailDescription.setText(event.getDescription());

        int remaining = event.getCapacity() - regCount;
        remainingSpots.setText(remaining + " / " + event.getCapacity() + " spots remaining");

        // Color Logic
        double ratio = (double) remaining / event.getCapacity();
        if (remaining <= 0) {
            remainingSpots.setTextColor(Color.GRAY);
            registerButton.setEnabled(false);
        } else if (ratio < 0.1) {
            remainingSpots.setTextColor(Color.RED);
        } else if (ratio < 0.5) {
            remainingSpots.setTextColor(Color.parseColor("#FFA500")); // Orange
        } else {
            remainingSpots.setTextColor(Color.parseColor("#2E7D32")); // Green
        }

        Glide.with(this).load(event.getImageUrl()).into(detailImage);
    }

    private void showRegisterDialog() {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_register, null);
        TextInputEditText nameInput = v.findViewById(R.id.editName);
        TextInputEditText emailInput = v.findViewById(R.id.editEmail);

        new AlertDialog.Builder(this)
                .setTitle("Register")
                .setView(v)
                .setPositiveButton("Submit", (dialog, which) -> {
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
                    Toast.makeText(this, "Registered!", Toast.LENGTH_SHORT).show();
                    loadEventDetails(eventId);
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show());
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