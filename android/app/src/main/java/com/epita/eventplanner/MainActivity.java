package com.epita.eventplanner;

import android.content.Intent;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Views
        searchBar = findViewById(R.id.searchEditText);
        filterChipGroup = findViewById(R.id.filterChipGroup);
        emptyStateView = findViewById(R.id.emptyStateView);
        RecyclerView rv = findViewById(R.id.eventsRecyclerView);

        // Setup RecyclerView
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter(this);
        rv.setAdapter(adapter);

        // Search Listener
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyFilters(); }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        // Chip Filter Listener
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
                    Toast.makeText(this, "Server Connection Failed", Toast.LENGTH_SHORT).show();
                    emptyStateView.setText("Error connecting to server");
                    emptyStateView.setVisibility(View.VISIBLE);
                });
            }
        }).start();
    }

    private void applyFilters() {
        String query = searchBar.getText().toString().toLowerCase().trim();
        int checkedChipId = filterChipGroup.getCheckedChipId();

        List<Event> filteredList = new ArrayList<>();

        for (Event event : allEventsMasterList) {
            boolean matchesSearch = query.isEmpty() ||
                    event.getTitle().toLowerCase().contains(query) ||
                    event.getLocation().toLowerCase().contains(query);

            boolean matchesDate = matchesDateFilter(event, checkedChipId);

            if (matchesSearch && matchesDate) {
                filteredList.add(event);
            }
        }

        // TOGGLE EMPTY STATE
        if (filteredList.isEmpty()) {
            emptyStateView.setVisibility(View.VISIBLE);
        } else {
            emptyStateView.setVisibility(View.GONE);
        }

        adapter.setEvents(filteredList);
    }

    private boolean matchesDateFilter(Event event, int chipId) {
        if (chipId == R.id.chipAll || chipId == -1) return true;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            Date eventDate = sdf.parse(event.getDate());
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
        } catch (Exception e) { return true; }
        return true;
    }

    @Override
    public void onEventClick(Event event) {
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra("event_id", event.getId());
        startActivity(intent);
    }
}