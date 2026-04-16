package com.epita.eventplanner;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements EventAdapter.OnEventClickListener {

    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private EventAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.swipeRefreshLayout.setOnRefreshListener(this::loadEvents);
        binding.errorLayout.errorMessage.setText(getString(R.string.error_load_failed, getString(R.string.type_events)));
        binding.errorLayout.retryButton.setOnClickListener(v -> loadEvents());

        binding.eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter(this);
        binding.eventsRecyclerView.setAdapter(adapter);

        loadEvents();
    }

    private void loadEvents() {
        if (!binding.swipeRefreshLayout.isRefreshing()) {
            showLoading();
        }

        new Thread(() -> {
            try {
                String json = ApiClient.fetchJson("/events");
                JSONArray array = new JSONArray(json);
                List<Event> events = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    events.add(Event.fromJson(obj));
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
        binding.mainContent.setVisibility(View.GONE);
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
