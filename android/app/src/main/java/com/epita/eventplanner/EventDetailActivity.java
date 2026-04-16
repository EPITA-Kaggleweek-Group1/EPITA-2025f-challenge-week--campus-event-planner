package com.epita.eventplanner;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.epita.eventplanner.api.ApiClient;
import com.epita.eventplanner.model.Event;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONArray;
import org.json.JSONObject;

public class EventDetailActivity extends AppCompatActivity {
    private int eventId;
    private SharedPreferences prefs;
    private TextView title, desc, date, loc, spots;
    private Button regBtn;
    private ImageButton favBtn;
    private ImageView img;
    private ProgressBar loadingSpinner;
    private View detailContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        // 1. Initialize Views
        prefs = getSharedPreferences("EventPrefs", Context.MODE_PRIVATE);
        eventId = getIntent().getIntExtra("event_id", -1);

        title = findViewById(R.id.detailTitle);
        desc = findViewById(R.id.detailDescription);
        date = findViewById(R.id.detailDate);
        loc = findViewById(R.id.detailLocation);
        spots = findViewById(R.id.remainingSpots);
        regBtn = findViewById(R.id.registerButton);
        favBtn = findViewById(R.id.detailFavBtn);
        img = findViewById(R.id.detailImage);
        loadingSpinner = findViewById(R.id.loadingSpinner);
        detailContent = findViewById(R.id.detailContent);

        // 2. Favorite Toggle Logic
        updateFavIcon();
        favBtn.setOnClickListener(v -> {
            boolean isFav = prefs.getBoolean("fav_" + eventId, false);
            prefs.edit().putBoolean("fav_" + eventId, !isFav).apply();
            updateFavIcon();
            Toast.makeText(this, !isFav ? "Saved to Favorites" : "Removed", Toast.LENGTH_SHORT).show();
        });

        // 3. Register Button Logic
        regBtn.setOnClickListener(v -> showRegisterDialog());

        // 4. Initial Data Load
        loadData();
    }

    private void updateFavIcon() {
        boolean isFav = prefs.getBoolean("fav_" + eventId, false);
        favBtn.setImageResource(isFav ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
    }

    private void loadData() {
        // Show loader, hide content while fetching
        runOnUiThread(() -> {
            loadingSpinner.setVisibility(View.VISIBLE);
            detailContent.setVisibility(View.INVISIBLE);
        });

        new Thread(() -> {
            try {
                // Fetch Event Info
                String eJson = ApiClient.fetchJson("/events/" + eventId);
                Event event = Event.fromJson(new JSONObject(eJson));

                // Fetch Current Registrations to calculate spots
                String rJson = ApiClient.fetchJson("/events/" + eventId + "/registrations");
                int regCount = new JSONArray(rJson).length();

                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;

                    title.setText(event.getTitle());
                    desc.setText(event.getDescription());
                    loc.setText(event.getLocation());

                    // Format Date (assuming ISO format YYYY-MM-DDTHH:MM:SS)
                    if (event.getDate() != null && event.getDate().contains("T")) {
                        date.setText(event.getDate().split("T")[0]);
                    } else {
                        date.setText(event.getDate());
                    }

                    // Calculate Capacity
                    int remaining = event.getCapacity() - regCount;
                    spots.setText(remaining + " spots left of " + event.getCapacity());

                    if (remaining <= 0) {
                        regBtn.setEnabled(false);
                        regBtn.setText("Sold Out");
                        spots.setTextColor(Color.RED);
                    } else {
                        regBtn.setEnabled(true);
                        regBtn.setText("Register for Event");
                        spots.setTextColor(Color.parseColor("#2E7D32"));
                    }

                    // --- IMAGE LOADING FIX ---
                    if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
                        Glide.with(this)
                                .load(event.getImageUrl())
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(android.R.drawable.stat_notify_error)
                                .into(img);
                    }

                    loadingSpinner.setVisibility(View.GONE);
                    detailContent.setVisibility(View.VISIBLE);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    loadingSpinner.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load event details", Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void showRegisterDialog() {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_register, null);
        TextInputEditText nameInput = v.findViewById(R.id.editName);
        TextInputEditText emailInput = v.findViewById(R.id.editEmail);

        new AlertDialog.Builder(this)
                .setTitle("Event Registration")
                .setView(v)
                .setPositiveButton("Confirm", (d, w) -> {
                    String name = nameInput.getText().toString().trim();
                    String email = emailInput.getText().toString().trim();
                    if (!name.isEmpty() && !email.isEmpty()) {
                        submitRegistration(name, email);
                    } else {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    }
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

                // POST request to server
                ApiClient.postJson("/events/" + eventId + "/register", body.toString());

                runOnUiThread(() -> {
                    Toast.makeText(this, "Successfully registered!", Toast.LENGTH_SHORT).show();
                    // Refresh data to update the "spots left" count
                    loadData();
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Registration failed. Try again.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}