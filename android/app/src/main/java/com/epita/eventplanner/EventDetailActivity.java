package com.epita.eventplanner;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.epita.eventplanner.api.ApiClient;
import com.epita.eventplanner.model.Event;
import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventDetailActivity extends AppCompatActivity {
    private int eventId;
    private TextView title, desc, date, loc, spots;
    private Button regBtn;
    private ImageView img;
    private ProgressBar loadingSpinner;
    private View detailContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        // UI References
        title = findViewById(R.id.detailTitle);
        desc = findViewById(R.id.detailDescription);
        date = findViewById(R.id.detailDate);
        loc = findViewById(R.id.detailLocation);
        spots = findViewById(R.id.remainingSpots);
        regBtn = findViewById(R.id.registerButton);
        img = findViewById(R.id.detailImage);
        loadingSpinner = findViewById(R.id.loadingSpinner);
        detailContent = findViewById(R.id.detailContent);

        eventId = getIntent().getIntExtra("event_id", -1);

        regBtn.setOnClickListener(v -> showRegisterDialog());

        loadData();
    }

    private void loadData() {
        // Show loader while fetching
        runOnUiThread(() -> {
            loadingSpinner.setVisibility(View.VISIBLE);
            detailContent.setVisibility(View.INVISIBLE);
        });

        new Thread(() -> {
            try {
                // 1. Fetch Event Details
                String eJson = ApiClient.fetchJson("/events/" + eventId);
                Event event = Event.fromJson(new JSONObject(eJson));

                // 2. Fetch Registrations to calculate occupancy
                String rJson = ApiClient.fetchJson("/events/" + eventId + "/registrations");
                int currentRegistrationCount = new JSONArray(rJson).length();

                runOnUiThread(() -> {
                    updateUI(event, currentRegistrationCount);
                    loadingSpinner.setVisibility(View.GONE);
                    detailContent.setVisibility(View.VISIBLE);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    loadingSpinner.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading event data", Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void updateUI(Event event, int count) {
        title.setText(event.getTitle());
        desc.setText(event.getDescription());
        loc.setText(event.getLocation());

        // Format Date
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            SimpleDateFormat out = new SimpleDateFormat("MMM dd, yyyy @ HH:mm", Locale.US);
            Date d = in.parse(event.getDate());
            date.setText(out.format(d));
        } catch (Exception e) {
            date.setText(event.getDate());
        }

        // Logic to Disable Button if Full
        int totalCapacity = event.getCapacity();
        int remainingSpots = totalCapacity - count;

        if (remainingSpots <= 0) {
            // EVENT FULL STATE
            spots.setText("Event is Sold Out (0 spots left)");
            spots.setTextColor(Color.parseColor("#757575")); // Grey

            regBtn.setEnabled(false);
            regBtn.setText("Sold Out");
            regBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.LTGRAY));
        } else {
            // EVENT AVAILABLE STATE
            spots.setText(remainingSpots + " spots left of " + totalCapacity);

            regBtn.setEnabled(true);
            regBtn.setText("Register for Event");
            regBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#2196F3"))); // Default Blue

            // Visual warning if spots are low
            if (remainingSpots < 5) {
                spots.setTextColor(Color.RED);
            } else {
                spots.setTextColor(Color.parseColor("#2E7D32")); // Green
            }
        }

        Glide.with(this).load(event.getImageUrl()).into(img);
    }

    private void showRegisterDialog() {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_register, null);
        TextInputEditText nIn = v.findViewById(R.id.editName);
        TextInputEditText eIn = v.findViewById(R.id.editEmail);

        new AlertDialog.Builder(this)
                .setTitle("Confirm Registration")
                .setView(v)
                .setPositiveButton("Submit", (d, w) -> {
                    String name = nIn.getText().toString().trim();
                    String email = eIn.getText().toString().trim();

                    if(name.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Toast.makeText(this, "Please enter a valid name and email", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    submitRegistration(name, email);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void submitRegistration(String n, String e) {
        new Thread(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("user_name", n);
                body.put("email", e);

                ApiClient.postJson("/events/" + eventId + "/register", body.toString());

                runOnUiThread(() -> {
                    Toast.makeText(this, "Successfully registered!", Toast.LENGTH_SHORT).show();
                    loadData(); // REFRESH UI to update spots and disable button if it was the last spot
                });
            } catch (Exception ex) {
                runOnUiThread(() -> {
                    // Check if the error is because it's full (if backend sends 403 or 400)
                    if (ex.getMessage().contains("400") || ex.getMessage().contains("403")) {
                        Toast.makeText(this, "Registration failed: Event is full", Toast.LENGTH_LONG).show();
                    } else if (ex.getMessage().contains("409")) {
                        Toast.makeText(this, "You are already registered!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Registration failed. Try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }
}