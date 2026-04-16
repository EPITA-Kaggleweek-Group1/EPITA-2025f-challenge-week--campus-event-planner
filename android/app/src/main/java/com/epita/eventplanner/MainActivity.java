package com.epita.eventplanner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.epita.eventplanner.adapter.EventAdapter;
import com.epita.eventplanner.api.ApiClient;
import com.epita.eventplanner.model.Event;
import com.google.android.material.chip.ChipGroup;
import org.json.JSONArray;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements EventAdapter.OnEventClickListener {
    private EventAdapter adapter;
    private EditText searchBar;
    private ChipGroup filterChipGroup;
    private TextView emptyStateView;
    private List<Event> allEventsMasterList = new ArrayList<>();

    // To prevent the app from filtering too fast while typing
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI Initialization
        searchBar = findViewById(R.id.searchEditText);
        filterChipGroup = findViewById(R.id.filterChipGroup);
        emptyStateView = findViewById(R.id.emptyStateView);
        RecyclerView rv = findViewById(R.id.eventsRecyclerView);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter(this, this);
        rv.setAdapter(adapter);

        // Search logic with "Debounce" (the 300ms delay)
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                searchRunnable = () -> applyFilters();
                searchHandler.postDelayed(searchRunnable, 300);
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        filterChipGroup.setOnCheckedChangeListener((group, checkedId) -> applyFilters());

        fetchAllEventsFromServer();
    }

    private void fetchAllEventsFromServer() {
        new Thread(() -> {
            try {
                String json = ApiClient.fetchJson("/events");
                JSONArray array = new JSONArray(json);
                List<Event> temp = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    temp.add(Event.fromJson(array.getJSONObject(i)));
                }
                runOnUiThread(() -> {
                    allEventsMasterList = temp;
                    applyFilters();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    emptyStateView.setText("Failed to load events. Check connection.");
                    emptyStateView.setVisibility(View.VISIBLE);
                });
            }
        }).start();
    }

    private void applyFilters() {
        String query = searchBar.getText().toString().toLowerCase().trim();
        int checkedChipId = filterChipGroup.getCheckedChipId();
        SharedPreferences prefs = getSharedPreferences("EventPrefs", MODE_PRIVATE);

        List<Event> filteredList = new ArrayList<>();

        for (Event event : allEventsMasterList) {
            // 1. Search Filter
            boolean matchesSearch = query.isEmpty() ||
                    event.getTitle().toLowerCase().contains(query) ||
                    event.getLocation().toLowerCase().contains(query);

            // 2. Date Filter
            boolean matchesDate = matchesDateFilter(event, checkedChipId);

            // 3. Favorites Filter
            boolean matchesFav = true;
            if (checkedChipId == R.id.chipFavorites) {
                matchesFav = prefs.getBoolean("fav_" + event.getId(), false);
            }

            if (matchesSearch && matchesDate && matchesFav) {
                filteredList.add(event);
            }
        }

        // Toggle the "No Events Found" message
        if (filteredList.isEmpty()) {
            emptyStateView.setVisibility(View.VISIBLE);
        } else {
            emptyStateView.setVisibility(View.GONE);
        }

        adapter.setEvents(filteredList);
    }

    private boolean matchesDateFilter(Event event, int chipId) {
        // If "All" or "Favorites" is selected, don't filter by date
        if (chipId == R.id.chipAll || chipId == -1 || chipId == R.id.chipFavorites) return true;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            Date eventDate = sdf.parse(event.getDate());
            if (eventDate == null) return true;

            Calendar now = Calendar.getInstance();
            Calendar eCal = Calendar.getInstance();
            eCal.setTime(eventDate);

            if (chipId == R.id.chipToday) {
                return now.get(Calendar.YEAR) == eCal.get(Calendar.YEAR) &&
                        now.get(Calendar.DAY_OF_YEAR) == eCal.get(Calendar.DAY_OF_YEAR);
            } else if (chipId == R.id.chipWeek) {
                return now.get(Calendar.YEAR) == eCal.get(Calendar.YEAR) &&
                        now.get(Calendar.WEEK_OF_YEAR) == eCal.get(Calendar.WEEK_OF_YEAR);
            } else if (chipId == R.id.chipMonth) {
                return now.get(Calendar.YEAR) == eCal.get(Calendar.YEAR) &&
                        now.get(Calendar.MONTH) == eCal.get(Calendar.MONTH);
            }
        } catch (Exception e) {
            return true;
        }
        return true;
    }

    @Override
    public void onEventClick(Event event) {
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra("event_id", event.getId());
        startActivity(intent);
    }

    @Override
    public void onFavoriteToggle() {
        applyFilters(); // Re-run filter if we are currently in the Favorites tab
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyFilters(); // Catch updates made in the Detail screen
    }
}