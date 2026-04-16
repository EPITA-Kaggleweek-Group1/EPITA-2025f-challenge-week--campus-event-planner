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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
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
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar loadMoreProgress;

    private List<Event> allEventsMasterList = new ArrayList<>();
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    // Pagination Variables
    private int currentPage = 1;
    private boolean isLastPage = false;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchBar = findViewById(R.id.searchEditText);
        filterChipGroup = findViewById(R.id.filterChipGroup);
        emptyStateView = findViewById(R.id.emptyStateView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        loadMoreProgress = findViewById(R.id.loadMoreProgress);
        RecyclerView rv = findViewById(R.id.eventsRecyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rv.setLayoutManager(layoutManager);
        adapter = new EventAdapter(this, this);
        rv.setAdapter(adapter);

        // Pull-to-Refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            currentPage = 1;
            isLastPage = false;
            fetchEventsFromServer(true);
        });

        // Infinite Scroll
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) { // Check for scroll down
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if (!isLoading && !isLastPage) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            currentPage++;
                            fetchEventsFromServer(false);
                        }
                    }
                }
            }
        });

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

        fetchEventsFromServer(true);
    }

    private void fetchEventsFromServer(boolean isRefresh) {
        isLoading = true;
        if (!isRefresh) loadMoreProgress.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                // Assuming API supports ?page=X. If not, it just reloads the main list.
                String json = ApiClient.fetchJson("/events?page=" + currentPage);
                JSONArray array = new JSONArray(json);
                List<Event> temp = new ArrayList<>();

                for (int i = 0; i < array.length(); i++) {
                    temp.add(Event.fromJson(array.getJSONObject(i)));
                }

                if (array.length() == 0) isLastPage = true;

                runOnUiThread(() -> {
                    if (isRefresh) {
                        allEventsMasterList = temp;
                        swipeRefreshLayout.setRefreshing(false);
                    } else {
                        allEventsMasterList.addAll(temp);
                        loadMoreProgress.setVisibility(View.GONE);
                    }
                    applyFilters();
                    isLoading = false;
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    isLoading = false;
                    swipeRefreshLayout.setRefreshing(false);
                    loadMoreProgress.setVisibility(View.GONE);
                    Toast.makeText(this, "Error fetching data", Toast.LENGTH_SHORT).show();
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
            boolean matchesSearch = query.isEmpty() ||
                    event.getTitle().toLowerCase().contains(query) ||
                    event.getLocation().toLowerCase().contains(query);

            boolean matchesDate = matchesDateFilter(event, checkedChipId);
            boolean matchesFav = checkedChipId != R.id.chipFavorites || prefs.getBoolean("fav_" + event.getId(), false);

            if (matchesSearch && matchesDate && matchesFav) {
                filteredList.add(event);
            }
        }

        emptyStateView.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
        adapter.setEvents(filteredList);
    }

    private boolean matchesDateFilter(Event event, int chipId) {
        if (chipId == R.id.chipAll || chipId == -1 || chipId == R.id.chipFavorites) return true;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            Date eventDate = sdf.parse(event.getDate());
            if (eventDate == null) return true;
            Calendar now = Calendar.getInstance();
            Calendar eCal = Calendar.getInstance();
            eCal.setTime(eventDate);

            if (chipId == R.id.chipToday) {
                return now.get(Calendar.YEAR) == eCal.get(Calendar.YEAR) && now.get(Calendar.DAY_OF_YEAR) == eCal.get(Calendar.DAY_OF_YEAR);
            } else if (chipId == R.id.chipWeek) {
                return now.get(Calendar.YEAR) == eCal.get(Calendar.YEAR) && now.get(Calendar.WEEK_OF_YEAR) == eCal.get(Calendar.WEEK_OF_YEAR);
            } else if (chipId == R.id.chipMonth) {
                return now.get(Calendar.YEAR) == eCal.get(Calendar.YEAR) && now.get(Calendar.MONTH) == eCal.get(Calendar.MONTH);
            }
        } catch (Exception e) { return true; }
        return true;
    }

    @Override public void onEventClick(Event event) {
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra("event_id", event.getId());
        startActivity(intent);
    }

    @Override public void onFavoriteToggle() { applyFilters(); }

    @Override protected void onResume() { super.onResume(); applyFilters(); }
}