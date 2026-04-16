package com.epita.eventplanner.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.epita.eventplanner.R;
import com.epita.eventplanner.api.ApiClient;
import com.epita.eventplanner.model.Event;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> events = new ArrayList<>();
    private final OnEventClickListener listener;
    private final SharedPreferences prefs;
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface OnEventClickListener {
        void onEventClick(Event event);
        void onFavoriteToggle();
    }

    public EventAdapter(Context context, OnEventClickListener listener) {
        this.listener = listener;
        this.prefs = context.getSharedPreferences("EventPrefs", Context.MODE_PRIVATE);
    }

    public void setEvents(List<Event> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.titleText.setText(event.getTitle());
        holder.dateText.setText(event.getDate().split("T")[0]);
        holder.locationText.setText(event.getLocation());

        // --- FAVORITE LOGIC ---
        boolean isFav = prefs.getBoolean("fav_" + event.getId(), false);
        // Uses Android system icons: big_on (filled) and big_off (outline)
        holder.favBtn.setImageResource(isFav ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);

        holder.favBtn.setOnClickListener(v -> {
            boolean currentStatus = prefs.getBoolean("fav_" + event.getId(), false);
            prefs.edit().putBoolean("fav_" + event.getId(), !currentStatus).apply();
            notifyItemChanged(position);
            if (listener != null) listener.onFavoriteToggle();
        });

        // --- AVAILABILITY LOGIC ---
        holder.statusText.setText("...");
        executorService.execute(() -> {
            try {
                String rJson = ApiClient.fetchJson("/events/" + event.getId() + "/registrations");
                int regCount = new JSONArray(rJson).length();
                int remaining = event.getCapacity() - regCount;
                mainHandler.post(() -> {
                    if (remaining <= 0) {
                        holder.statusText.setText("SOLD OUT");
                        holder.statusText.setTextColor(Color.RED);
                        setCircleColor(holder.indicator, Color.RED);
                    } else {
                        holder.statusText.setText("Available");
                        holder.statusText.setTextColor(Color.parseColor("#2E7D32"));
                        setCircleColor(holder.indicator, Color.parseColor("#2E7D32"));
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> holder.statusText.setText(""));
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onEventClick(event);
        });
    }

    private void setCircleColor(View v, int color) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(color);
        v.setBackground(shape);
    }

    @Override
    public int getItemCount() { return events.size(); }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, dateText, locationText, statusText;
        ImageButton favBtn;
        View indicator;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.eventTitle);
            dateText = itemView.findViewById(R.id.eventDate);
            locationText = itemView.findViewById(R.id.eventLocation);
            statusText = itemView.findViewById(R.id.statusText);
            favBtn = itemView.findViewById(R.id.favoriteButton);
            indicator = itemView.findViewById(R.id.availabilityIndicator);
        }
    }
}