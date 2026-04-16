package com.epita.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.epita.eventplanner.adapter.EventAdapter;
import com.epita.eventplanner.api.ApiClient;
import com.epita.eventplanner.model.Event;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements EventAdapter.OnEventClickListener {

    private static final String TAG = "MainActivity";

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    
    private ProgressBar loadingSpinner;
    private View errorView;
    private LinearLayout mainContent;
    private Button retryButton;
    private TextView errorMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::loadEvents);

        loadingSpinner = findViewById(R.id.loadingSpinner);
        errorView = findViewById(R.id.errorView);
        mainContent = findViewById(R.id.mainContent);
        retryButton = findViewById(R.id.retryButton);
        errorMessage = findViewById(R.id.errorMessage);

        errorMessage.setText("Failed to load events");
        retryButton.setOnClickListener(v -> loadEvents());

        recyclerView = findViewById(R.id.eventsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter(this);
        recyclerView.setAdapter(adapter);

        loadEvents();
    }

    private void loadEvents() {
        if (!swipeRefreshLayout.isRefreshing()) {
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
                    swipeRefreshLayout.setRefreshing(false);
                });

            } catch (Exception e) {
                Log.e(TAG, "Failed to load events", e);
                runOnUiThread(() -> {
                    // Always show error and clear list on failure as requested
                    adapter.setEvents(new ArrayList<>());
                    showError();
                    swipeRefreshLayout.setRefreshing(false);
                });
            }
        }).start();
    }

    private void showLoading() {
        loadingSpinner.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        mainContent.setVisibility(View.GONE);
    }

    private void showContent() {
        loadingSpinner.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        mainContent.setVisibility(View.VISIBLE);
    }

    private void showError() {
        loadingSpinner.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
        mainContent.setVisibility(View.GONE);
    }

    @Override
    public void onEventClick(Event event) {
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra("event_id", event.getId());
        startActivity(intent);
    }
}
