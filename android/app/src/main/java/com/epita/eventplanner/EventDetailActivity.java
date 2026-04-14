package com.epita.eventplanner;

import android.os.Bundle;
import android.util.Patterns; // For email validation
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.epita.eventplanner.api.ApiClient;
import com.epita.eventplanner.model.Event;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

public class EventDetailActivity extends AppCompatActivity {

    private ProgressBar loadingSpinner;
    private LinearLayout errorView;
    private ScrollView detailContent;
    private Button retryButton, registerButton;
    private TextView detailTitle, detailDate, detailLocation, detailCapacity, detailDescription, remainingSpots;
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

        detailTitle = findViewById(R.id.detailTitle);
        detailDate = findViewById(R.id.detailDate);
        detailLocation = findViewById(R.id.detailLocation);
        detailCapacity = findViewById(R.id.detailCapacity);
        detailDescription = findViewById(R.id.detailDescription);

        eventId = getIntent().getIntExtra("event_id", -1);

        registerButton.setOnClickListener(v -> showRegisterDialog());
        retryButton.setOnClickListener(v -> loadEventDetails(eventId));

        if (eventId != -1) loadEventDetails(eventId);
    }

    private void showRegisterDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_register, null);

        // References to Layouts (for showing errors) and Edits (for getting text)
        TextInputLayout layoutName = dialogView.findViewById(R.id.layoutName);
        TextInputLayout layoutEmail = dialogView.findViewById(R.id.layoutEmail);
        TextInputEditText editName = dialogView.findViewById(R.id.editName);
        TextInputEditText editEmail = dialogView.findViewById(R.id.editEmail);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Register")
                .setView(dialogView)
                .setPositiveButton("Submit", null) // Set to null first to override closing behavior
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String name = editName.getText().toString().trim();
                String email = editEmail.getText().toString().trim();

                boolean isValid = true;

                // 1. Validate Name
                if (name.isEmpty()) {
                    layoutName.setError("Name is required");
                    isValid = false;
                } else if (name.length() < 2) {
                    layoutName.setError("Name is too short");
                    isValid = false;
                } else {
                    layoutName.setError(null);
                }

                // 2. Validate Email
                if (email.isEmpty()) {
                    layoutEmail.setError("Email is required");
                    isValid = false;
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    layoutEmail.setError("Invalid email format");
                    isValid = false;
                } else {
                    layoutEmail.setError(null);
                }

                // 3. Submit if valid
                if (isValid) {
                    submitRegistration(name, email);
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    private void submitRegistration(String name, String email) {
        new Thread(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("user_name", name);
                json.put("email", email);

                ApiClient.postJson("/events/" + eventId + "/register", json.toString());

                runOnUiThread(() -> Toast.makeText(this, "Registration Successful!", Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Registration Failed", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void loadEventDetails(int id) {
        showLoading();
        new Thread(() -> {
            try {
                String json = ApiClient.fetchJson("/events/" + id);
                Event event = Event.fromJson(new JSONObject(json));
                runOnUiThread(() -> {
                    populateUI(event);
                    showContent();
                });
            } catch (Exception e) {
                runOnUiThread(this::showError);
            }
        }).start();
    }

    private void populateUI(Event event) {
        detailTitle.setText(event.getTitle());
        detailDescription.setText(event.getDescription());
        detailLocation.setText(event.getLocation());
        detailDate.setText(event.getDate());
        detailCapacity.setText("Capacity: " + event.getCapacity());
        remainingSpots.setText("Spots available: " + event.getCapacity());
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