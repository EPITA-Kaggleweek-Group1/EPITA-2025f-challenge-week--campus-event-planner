package com.epita.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.epita.eventplanner.adapter.EventAdapter;
import com.epita.eventplanner.api.ApiClient;
import com.epita.eventplanner.databinding.ActivityMainBinding;
import com.epita.eventplanner.model.Event;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements EventAdapter.OnEventClickListener {

    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private EventAdapter adapter;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private String currentQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.swipeRefreshLayout.setOnRefreshListener(() -> loadEvents(currentQuery));
        binding.errorLayout.errorMessage.setText(getString(R.string.error_load_failed, getString(R.string.type_events)));
        binding.errorLayout.retryButton.setOnClickListener(v -> loadEvents(currentQuery));

        binding.eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter(this);
        binding.eventsRecyclerView.setAdapter(adapter);

        setupSearch();
        loadEvents("");
    }

    private void setupSearch() {
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                currentQuery = s.toString().trim();
                searchRunnable = () -> loadEvents(currentQuery);
                searchHandler.postDelayed(searchRunnable, 300);
            }
        });
    }

    private void loadEvents(String query) {
        // Only show full-screen loading if the list is empty and we're not searching
        if (!binding.swipeRefreshLayout.isRefreshing() && adapter.getItemCount() == 0) {
            showLoading();
        }

        new Thread(() -> {
            try {
                String path = "/events";
                if (query != null && !query.isEmpty()) {
                    path += "?search=" + URLEncoder.encode(query, "UTF-8");
                }
                
                String json = ApiClient.fetchJson(path);
                JSONArray array = new JSONArray(json);
                List<Event> events = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    Event event = Event.fromJson(obj);
                    
                    // Fetch registration count for each event
                    try {
                        String countResponse = ApiClient.fetchJson("/events/" + event.getId() + "/registrations/count");
                        JSONObject countObject = new JSONObject(countResponse);
                        event.setRegistrationCount(countObject.getInt("count"));
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to load count for event " + event.getId(), e);
                    }
                    
                    events.add(event);
                }

                runOnUiThread(() -> {
                    adapter.setEvents(events);
                    showContent();
                    binding.swipeRefreshLayout.setRefreshing(false);
                });

            } catch (Exception e) {
                Log.e(TAG, "Failed to load events", e);
                runOnUiThread(() -> {
                    adapter.setEvents(new ArrayList<>());
                    showError();
                    binding.swipeRefreshLayout.setRefreshing(false);
                });
            }
        }).start();
    }

    private void showLoading() {
        binding.loadingSpinner.setVisibility(View.VISIBLE);
        binding.errorLayout.errorView.setVisibility(View.GONE);
        // Do not hide mainContent to avoid search bar blinking
    }

    private void showContent() {
        binding.loadingSpinner.setVisibility(View.GONE);
        binding.errorLayout.errorView.setVisibility(View.GONE);
        binding.mainContent.setVisibility(View.VISIBLE);
    }

    private void showError() {
        binding.loadingSpinner.setVisibility(View.GONE);
        binding.errorLayout.errorView.setVisibility(View.VISIBLE);
        binding.mainContent.setVisibility(View.GONE);
    }

    @Override
    public void onEventClick(Event event) {
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra("event_id", event.getId());
        startActivity(intent);
    }
}
