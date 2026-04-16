package com.epita.eventplanner;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.epita.eventplanner.adapter.EventAdapter;
import com.epita.eventplanner.api.ApiClient;
import com.epita.eventplanner.model.Event;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements EventAdapter.OnEventClickListener {
    private EventAdapter adapter;
    private EditText searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchBar = findViewById(R.id.searchEditText);
        RecyclerView rv = findViewById(R.id.eventsRecyclerView);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter(this);
        rv.setAdapter(adapter);

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadEvents(s.toString());
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        loadEvents("");
    }

    private void loadEvents(String query) {
        new Thread(() -> {
            try {
                // S010: search functionality
                String endpoint = query.isEmpty() ? "/events" : "/events?search=" + query;
                String json = ApiClient.fetchJson(endpoint);
                JSONArray array = new JSONArray(json);
                List<Event> events = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    events.add(Event.fromJson(array.getJSONObject(i)));
                }
                runOnUiThread(() -> adapter.setEvents(events));
            } catch (Exception e) {
                Log.e("API", "Error: ", e);
                runOnUiThread(() -> Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @Override
    public void onEventClick(Event event) {
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra("event_id", event.getId());
        startActivity(intent);
    }
}