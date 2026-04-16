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
import com.epita.eventplanner.util.DateUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements EventAdapter.OnEventClickListener {

    private static final String TAG = "MainActivity";
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private ActivityMainBinding binding;
    private EventAdapter adapter;
    private Runnable searchRunnable;
    private String currentQuery = "";
    private String dateFrom = "";
    private String dateTo = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.swipeRefreshLayout.setOnRefreshListener(() -> loadEvents(currentQuery, dateFrom, dateTo));
        binding.errorLayout.errorMessage.setText(getString(R.string.error_load_failed, getString(R.string.type_events)));
        binding.errorLayout.retryButton.setOnClickListener(v -> loadEvents(currentQuery, dateFrom, dateTo));

        binding.eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter(this);
        binding.eventsRecyclerView.setAdapter(adapter);

        setupSearch();
        setupFilters();
        loadEvents("", "", "");
    }

    private void setupFilters() {
        binding.filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int checkedId = checkedIds.isEmpty() ? -1 : checkedIds.get(0);

            Calendar cal = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String today = df.format(cal.getTime());

            if (checkedId == R.id.chipToday) {
                dateFrom = today;
                cal.add(Calendar.DAY_OF_YEAR, 1);
                dateTo = df.format(cal.getTime());
            } else if (checkedId == R.id.chipWeek) {
                dateFrom = today;
                cal.add(Calendar.DAY_OF_YEAR, 8);
                dateTo = df.format(cal.getTime());
            } else if (checkedId == R.id.chipMonth) {
                dateFrom = today;
                cal.add(Calendar.MONTH, 1);
                cal.add(Calendar.DAY_OF_YEAR, 1);
                dateTo = df.format(cal.getTime());
            } else if (checkedId == R.id.chipPast) {
                dateFrom = "";
                dateTo = today;
                // Note: The API might need adjustments if it defaults to only showing future events.
                // Assuming it filters strictly by date range if provided.
            } else {
                dateFrom = "";
                dateTo = "";
            }
            loadEvents(currentQuery, dateFrom, dateTo);
        });
    }

    private void setupSearch() {
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                currentQuery = s.toString().trim();
                searchRunnable = () -> loadEvents(currentQuery, dateFrom, dateTo);
                searchHandler.postDelayed(searchRunnable, 300);
            }
        });
    }

    private void loadEvents(String query, String from, String to) {
        // Only show full-screen loading if the list is empty and we're not searching
        if (!binding.swipeRefreshLayout.isRefreshing() && adapter.getItemCount() == 0) {
            showLoading();
        }

        new Thread(() -> {
            try {
                // Determine if we need to filter out past events manually
                boolean filterOutPast = true;
                if (to != null && !to.isEmpty()) {
                    // If dateTo is provided and is not after 'now', we might be looking at past events specifically
                    // Actually, if chipPast is selected, we want past events.
                    // If dateTo == today (from chipPast), we want past.
                    Calendar cal = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String today = df.format(cal.getTime());
                    if (to.equals(today) && (from == null || from.isEmpty())) {
                        filterOutPast = false;
                    }
                }

                StringBuilder pathBuilder = new StringBuilder("/events?");
                if (query != null && !query.isEmpty()) {
                    pathBuilder.append("search=").append(URLEncoder.encode(query, "UTF-8")).append("&");
                }
                if (from != null && !from.isEmpty()) {
                    pathBuilder.append("date_from=").append(from).append("&");
                }
                if (to != null && !to.isEmpty()) {
                    pathBuilder.append("date_to=").append(to).append("&");
                }

                String path = pathBuilder.toString();
                if (path.endsWith("&") || path.endsWith("?")) {
                    path = path.substring(0, path.length() - 1);
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

                    if (!filterOutPast || !DateUtils.isPast(event.getDate())) {
                        events.add(event);
                    }
                }

                runOnUiThread(() -> {
                    adapter.setEvents(events);
                    binding.emptyStateText.setVisibility(events.isEmpty() ? View.VISIBLE : View.GONE);
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
