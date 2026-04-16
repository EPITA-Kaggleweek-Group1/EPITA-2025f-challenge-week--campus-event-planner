package com.epita.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.epita.eventplanner.adapter.EventAdapter;
import com.epita.eventplanner.api.ApiClient;
import com.epita.eventplanner.databinding.ActivityMainBinding;
import com.epita.eventplanner.model.Event;
import com.epita.eventplanner.util.DateUtils;
import com.epita.eventplanner.util.FavoritesManager;

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
    private static final int PAGE_SIZE = 10;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private ActivityMainBinding binding;
    private EventAdapter adapter;
    private FavoritesManager favoritesManager;
    private Runnable searchRunnable;
    private String currentQuery = "";
    private String dateFrom = "";
    private String dateTo = "";
    private int currentOffset = 0;
    private boolean isLoading = false;
    private boolean hasMore = true;
    private boolean isShowingFavorites = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            if (isShowingFavorites) {
                loadFavorites(dateFrom, dateTo);
            } else {
                resetPagination();
                loadEvents(currentQuery, dateFrom, dateTo);
            }
        });
        binding.errorLayout.errorMessage.setText(getString(R.string.error_load_failed, getString(R.string.type_events)));
        binding.errorLayout.retryButton.setOnClickListener(v -> {
            if (isShowingFavorites) {
                loadFavorites(dateFrom, dateTo);
            } else {
                resetPagination();
                loadEvents(currentQuery, dateFrom, dateTo);
            }
        });

        favoritesManager = new FavoritesManager(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.eventsRecyclerView.setLayoutManager(layoutManager);
        adapter = new EventAdapter(this, favoritesManager);
        binding.eventsRecyclerView.setAdapter(adapter);

        binding.eventsRecyclerView.addOnScrollListener(new androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull androidx.recyclerview.widget.RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) { // check for scroll down
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if (!isLoading && hasMore) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            loadEvents(currentQuery, dateFrom, dateTo);
                        }
                    }
                }
            }
        });

        setupSearch();
        setupFilters();
        setupBottomNavigation();
        
        // Initialize dateFrom to today for the default "All" filter
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateFrom = df.format(Calendar.getInstance().getTime());
        
        loadEvents("", dateFrom, "");
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_events) {
                isShowingFavorites = false;
                binding.searchEditText.setVisibility(View.VISIBLE);
                resetPagination();
                loadEvents(currentQuery, dateFrom, dateTo);
                return true;
            } else if (id == R.id.nav_favorites) {
                isShowingFavorites = true;
                binding.searchEditText.setVisibility(View.GONE);
                loadFavorites(dateFrom, dateTo);
                return true;
            }
            return false;
        });
    }

    private void loadFavorites(String from, String to) {
        showLoading();
        new Thread(() -> {
            try {
                // Determine if we need to filter out past events
                boolean filterOutPast = true;
                if (to != null && !to.isEmpty()) {
                    Calendar cal = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String today = df.format(cal.getTime());
                    if (to.equals(today) && (from == null || from.isEmpty())) {
                        filterOutPast = false;
                    }
                }

                // In a real app, we would have a dedicated favorites endpoint with filters.
                // For now, we fetch a large batch and filter locally.
                String json = ApiClient.fetchJson("/events?limit=200");
                JSONObject responseObj = new JSONObject(json);
                JSONArray array = responseObj.getJSONArray("data");
                
                List<Event> favorites = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    Event event = Event.fromJson(array.getJSONObject(i));
                    
                    // Filter 1: Must be favorite
                    if (!favoritesManager.isFavorite(event.getId())) {
                        continue;
                    }

                    // Filter 2: Must match date range
                    if (!DateUtils.isInRange(event.getDate(), from, to)) {
                        continue;
                    }
                    
                    // Filter 3: Apply "All" filter logic (hide past events unless explicitly requested)
                    if (filterOutPast && DateUtils.isPast(event.getDate())) {
                        continue;
                    }

                    try {
                        String countResponse = ApiClient.fetchJson("/events/" + event.getId() + "/registrations/count");
                        JSONObject countObject = new JSONObject(countResponse);
                        event.setRegistrationCount(countObject.getInt("count"));
                    } catch (Exception ignored) {}
                    
                    favorites.add(event);
                }

                runOnUiThread(() -> {
                    adapter.setEvents(favorites);
                    binding.emptyStateText.setVisibility(favorites.isEmpty() ? View.VISIBLE : View.GONE);
                    showContent();
                    binding.swipeRefreshLayout.setRefreshing(false);
                    hasMore = false; 
                });
            } catch (Exception e) {
                Log.e(TAG, "Failed to load favorites", e);
                runOnUiThread(() -> {
                    showError();
                    binding.swipeRefreshLayout.setRefreshing(false);
                });
            }
        }).start();
    }

    private void resetPagination() {
        currentOffset = 0;
        hasMore = true;
        adapter.clear();
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
            } else {
                dateFrom = today;
                dateTo = "";
            }
            
            if (isShowingFavorites) {
                loadFavorites(dateFrom, dateTo);
            } else {
                resetPagination();
                loadEvents(currentQuery, dateFrom, dateTo);
            }
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
                searchRunnable = () -> {
                    resetPagination();
                    loadEvents(currentQuery, dateFrom, dateTo);
                };
                searchHandler.postDelayed(searchRunnable, 300);
            }
        });
    }

    private void loadEvents(String query, String from, String to) {
        if (isLoading || !hasMore) return;
        isLoading = true;

        // Only show full-screen loading if the list is empty and we're not searching
        if (!binding.swipeRefreshLayout.isRefreshing() && adapter.getItemCount() == 0) {
            showLoading();
        }

        new Thread(() -> {
            try {
                // Determine if we need to filter out past events manually
                boolean filterOutPast = true;
                if (to != null && !to.isEmpty()) {
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
                pathBuilder.append("offset=").append(currentOffset).append("&");
                pathBuilder.append("limit=").append(PAGE_SIZE);

                String path = pathBuilder.toString();

                String json = ApiClient.fetchJson(path);
                JSONObject responseObj = new JSONObject(json);
                JSONArray array = responseObj.getJSONArray("data");
                JSONObject pagination = responseObj.getJSONObject("pagination");

                hasMore = pagination.getBoolean("has_more");
                currentOffset = pagination.getInt("next_offset");

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
                    adapter.addEvents(events);
                    binding.emptyStateText.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                    showContent();
                    binding.swipeRefreshLayout.setRefreshing(false);
                    isLoading = false;
                });

            } catch (Exception e) {
                Log.e(TAG, "Failed to load events", e);
                runOnUiThread(() -> {
                    showError();
                    binding.swipeRefreshLayout.setRefreshing(false);
                    isLoading = false;
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
    protected void onResume() {
        super.onResume();
        // Refresh the list to reflect any changes in favorite status from DetailActivity
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onEventClick(Event event) {
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra("event_id", event.getId());
        startActivity(intent);
    }
}
