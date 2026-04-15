package com.epita.eventplanner.adapter;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
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

        // Default state (Light)
        resetToDefaultState(holder);

        String imageUrl = event.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            holder.eventImage.setVisibility(View.VISIBLE);
            holder.eventImageMask.setVisibility(View.VISIBLE);
            
            Glide.with(holder.itemView.getContext())
                    .asBitmap()
                    .load(imageUrl)
                    .centerCrop()
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            holder.eventImage.setImageBitmap(resource);
                            analyzeImageColor(resource, holder);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            holder.eventImage.setImageDrawable(placeholder);
                        }
                    });
        } else {
            holder.eventImage.setVisibility(View.GONE);
            holder.eventImageMask.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEventClick(event);
            }
        });
    }

    private void resetToDefaultState(EventViewHolder holder) {
        holder.cardView.setCardBackgroundColor(Color.WHITE);
        holder.titleText.setTextColor(Color.parseColor("#212121"));
        holder.dateText.setTextColor(Color.parseColor("#757575"));
        holder.locationText.setTextColor(Color.parseColor("#757575"));
        holder.capacityText.setTextColor(Color.parseColor("#9E9E9E"));
        holder.eventImageMask.setBackgroundResource(R.drawable.fade_mask);
    }

    private void analyzeImageColor(Bitmap bitmap, EventViewHolder holder) {
        Palette.from(bitmap).generate(palette -> {
            if (palette != null) {
                // Determine if the image is dark overall
                Palette.Swatch swatch = palette.getVibrantSwatch();
                if (swatch == null) swatch = palette.getMutedSwatch();
                if (swatch == null) swatch = palette.getDominantSwatch();

                if (swatch != null) {
                    float[] hsl = swatch.getHsl();
                    boolean isDark = hsl[2] < 0.5f; // Lightness < 50%

                    if (isDark) {
                        applyDarkState(holder);
                    }
                }
            }
        });
    }

    private void applyDarkState(EventViewHolder holder) {
        holder.cardView.setCardBackgroundColor(Color.parseColor("#121212"));
        holder.titleText.setTextColor(Color.WHITE);
        holder.dateText.setTextColor(Color.parseColor("#BDBDBD"));
        holder.locationText.setTextColor(Color.parseColor("#BDBDBD"));
        holder.capacityText.setTextColor(Color.parseColor("#9E9E9E"));
        holder.eventImageMask.setBackgroundResource(R.drawable.fade_mask_dark);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    /**
     * ViewHolder for a single event card.
     */
    static class EventViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView titleText;
        TextView dateText;
        TextView locationText;
        TextView capacityText;
        ImageView eventImage;
        View eventImageMask;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            titleText = itemView.findViewById(R.id.eventTitle);
            dateText = itemView.findViewById(R.id.eventDate);
            locationText = itemView.findViewById(R.id.eventLocation);
            capacityText = itemView.findViewById(R.id.eventCapacity);
            eventImage = itemView.findViewById(R.id.eventImage);
            eventImageMask = itemView.findViewById(R.id.eventImageMask);
        }
    }
}
