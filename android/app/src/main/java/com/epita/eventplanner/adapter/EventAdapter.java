package com.epita.eventplanner.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.epita.eventplanner.R;
import com.epita.eventplanner.model.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter that displays a list of {@link Event} objects
 * using the item_event card layout.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> events = new ArrayList<>();
    private OnEventClickListener listener;

    /**
     * Callback interface for item clicks.
     */
    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public EventAdapter(OnEventClickListener listener) {
        this.listener = listener;
    }

    /**
     * Replace the current dataset and refresh the list.
     */
    public void setEvents(List<Event> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.titleText.setText(event.getTitle());
        holder.dateText.setText(event.getDate());
        holder.locationText.setText(event.getLocation());
        holder.capacityText.setText("Capacity: " + event.getCapacity());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEventClick(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    /**
     * ViewHolder for a single event card.
     */
    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView dateText;
        TextView locationText;
        TextView capacityText;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.eventTitle);
            dateText = itemView.findViewById(R.id.eventDate);
            locationText = itemView.findViewById(R.id.eventLocation);
            capacityText = itemView.findViewById(R.id.eventCapacity);
        }
    }
}
