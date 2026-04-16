package com.epita.eventplanner.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.epita.eventplanner.R;
import com.epita.eventplanner.model.Event;
import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private List<Event> events = new ArrayList<>();
    private final OnEventClickListener listener;
    private final SharedPreferences prefs;

    public interface OnEventClickListener {
        void onEventClick(Event event);
        void onFavoriteToggle();
    }

    public EventAdapter(Context context, OnEventClickListener listener) {
        this.listener = listener;
        this.prefs = context.getSharedPreferences("EventPrefs", Context.MODE_PRIVATE);
    }

    public void setEvents(List<Event> newEvents) {
        this.events = newEvents;
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

        // Thumbnail with Placeholder
        Glide.with(holder.itemView.getContext())
                .load(event.getImageUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.stat_notify_error)
                .centerCrop()
                .into(holder.thumbnail);

        boolean isFav = prefs.getBoolean("fav_" + event.getId(), false);
        holder.favBtn.setImageResource(isFav ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);

        holder.favBtn.setOnClickListener(v -> {
            boolean current = prefs.getBoolean("fav_" + event.getId(), false);
            prefs.edit().putBoolean("fav_" + event.getId(), !current).apply();
            notifyItemChanged(position);
            if (listener != null) listener.onFavoriteToggle();
        });

        holder.itemView.setOnClickListener(v -> listener.onEventClick(event));
    }

    @Override
    public int getItemCount() { return events.size(); }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, dateText, locationText;
        ImageView thumbnail;
        ImageButton favBtn;

        EventViewHolder(@NonNull View v) {
            super(v);
            titleText = v.findViewById(R.id.eventTitle);
            dateText = v.findViewById(R.id.eventDate);
            locationText = v.findViewById(R.id.eventLocation);
            thumbnail = v.findViewById(R.id.eventThumbnail);
            favBtn = v.findViewById(R.id.favoriteButton);
        }
    }
}