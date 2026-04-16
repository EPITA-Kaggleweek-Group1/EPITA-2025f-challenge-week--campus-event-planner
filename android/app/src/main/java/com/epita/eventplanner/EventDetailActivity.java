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
import com.epita.eventplanner.api.ApiClient;
import com.epita.eventplanner.model.Event;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

        // Initialize SharedPreferences
        prefs = getSharedPreferences("EventPrefs", Context.MODE_PRIVATE);
        eventId = getIntent().getIntExtra("event_id", -1);

        // UI References
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

        // Favorite Button Logic
        updateFavIcon();
        favBtn.setOnClickListener(v -> {
            boolean isFav = prefs.getBoolean("fav_" + eventId, false);
            prefs.edit().putBoolean("fav_" + eventId, !isFav).apply();
            updateFavIcon();
            String msg = !isFav ? "Added to Favorites" : "Removed from Favorites";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        regBtn.setOnClickListener(v -> showRegisterDialog());

        loadData();
    }

    private void updateFavIcon() {
        boolean isFav = prefs.getBoolean("fav_" + eventId, false);
        favBtn.setImageResource(isFav ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
    }

    private void loadData() {
        runOnUiThread(() -> {
            loadingSpinner.setVisibility(View.VISIBLE);
            detailContent.setVisibility(View.INVISIBLE);
        });

        new Thread(() -> {
            try {
                String eJson = ApiClient.fetchJson("/events/" + eventId);
                Event event = Event.fromJson(new JSONObject(eJson));

                String rJson = ApiClient.fetchJson("/events/" + eventId + "/registrations");
                int regCount = new JSONArray(rJson).length();

                runOnUiThread(() -> {
                    updateUI(event, regCount);
                    loadingSpinner.setVisibility(View.GONE);
                    detailContent.setVisibility(View.VISIBLE);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    loadingSpinner.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading event", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void updateUI(Event event, int count) {
        title.setText(event.getTitle());
        desc.setText(event.getDescription());
        loc.setText(event.getLocation());

        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            SimpleDateFormat out = new SimpleDateFormat("MMM dd, yyyy @ HH:mm", Locale.US);
            Date d = in.parse(event.getDate());
            date.setText(out.format(d));
        } catch (Exception e) {
            date.setText(event.getDate());
        }

        int remaining = event.getCapacity() - count;
        spots.setText(remaining + " spots left of " + event.getCapacity());

        if (remaining <= 0) {
            regBtn.setEnabled(false);
            regBtn.setText("Sold Out");
            spots.setTextColor(Color.RED);
        }

        Glide.with(this).load(event.getImageUrl()).placeholder(android.R.drawable.ic_menu_gallery).into(img);
    }

    private void showRegisterDialog() {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_register, null);
        TextInputEditText nIn = v.findViewById(R.id.editName);
        TextInputEditText eIn = v.findViewById(R.id.editEmail);

        new AlertDialog.Builder(this)
                .setTitle("Register")
                .setView(v)
                .setPositiveButton("Submit", (d, w) -> {
                    submitRegistration(nIn.getText().toString(), eIn.getText().toString());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void submitRegistration(String name, String email) {
        if (name.isEmpty() || email.isEmpty()) return;
        new Thread(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("user_name", name);
                body.put("email", email);
                ApiClient.postJson("/events/" + eventId + "/register", body.toString());
                runOnUiThread(() -> {
                    Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                    loadData();
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}