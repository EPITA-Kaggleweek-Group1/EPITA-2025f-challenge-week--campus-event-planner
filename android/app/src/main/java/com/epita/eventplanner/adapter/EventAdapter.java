package com.epita.eventplanner.adapter;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.epita.eventplanner.R;
import com.epita.eventplanner.databinding.ItemEventBinding;
import com.epita.eventplanner.model.Event;
import com.epita.eventplanner.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter that displays a list of {@link Event} objects
 * using the item_event card layout.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final OnEventClickListener listener;
    private List<Event> events = new ArrayList<>();

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
        ItemEventBinding binding = ItemEventBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new EventViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        ItemEventBinding b = holder.binding;

        b.eventTextInclude.eventTitle.setText(event.getTitle());
        b.eventTextInclude.eventDate.setText(DateUtils.formatToHuman(event.getDate()));
        b.eventTextInclude.eventLocation.setText(event.getLocation());
        b.eventTextInclude.eventCapacity.setText(
                holder.itemView.getContext().getString(R.string.capacity_format_main, event.getRegistrationCount(), event.getCapacity())
        );

        updateCapacityIndicator(b, event);

        // Default state (Light)
        resetToDefaultState(holder);

        String imageUrl = event.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            b.eventImageInclude.eventImage.setVisibility(View.VISIBLE);
            b.eventImageInclude.eventImageMask.setVisibility(View.VISIBLE);

            Glide.with(holder.itemView.getContext())
                    .asBitmap()
                    .load(imageUrl)
                    .centerCrop()
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            b.eventImageInclude.eventImage.setImageBitmap(resource);
                            analyzeImageColor(resource, holder);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            b.eventImageInclude.eventImage.setImageDrawable(placeholder);
                        }
                    });
        } else {
            b.eventImageInclude.eventImage.setVisibility(View.GONE);
            b.eventImageInclude.eventImageMask.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEventClick(event);
            }
        });
    }

    private void updateCapacityIndicator(ItemEventBinding b, Event event) {
        int capacity = event.getCapacity();
        int count = event.getRegistrationCount();
        int remaining = capacity - count;

        int color;
        if (remaining <= 0) {
            color = Color.parseColor("#9E9E9E"); // Gray (Full)
        } else if (remaining <= 5) {
            color = Color.parseColor("#F44336"); // Red (Almost Full)
        } else if (remaining <= 20) {
            color = Color.parseColor("#FF9800"); // Orange (Filling Up)
        } else {
            color = Color.parseColor("#4CAF50"); // Green (Plenty of spots)
        }

        b.eventTextInclude.capacityIndicator.getBackground().setTint(color);
    }

    private void resetToDefaultState(EventViewHolder holder) {
        ItemEventBinding b = holder.binding;
        b.getRoot().setCardBackgroundColor(Color.WHITE);
        b.eventTextInclude.eventTitle.setTextColor(Color.parseColor("#212121"));
        b.eventTextInclude.eventDate.setTextColor(Color.parseColor("#757575"));
        b.eventTextInclude.eventLocation.setTextColor(Color.parseColor("#757575"));
        b.eventTextInclude.eventCapacity.setTextColor(Color.parseColor("#9E9E9E"));
        b.eventImageInclude.eventImageMask.setBackgroundResource(R.drawable.fade_mask);
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
        ItemEventBinding b = holder.binding;
        b.getRoot().setCardBackgroundColor(Color.parseColor("#121212"));
        b.eventTextInclude.eventTitle.setTextColor(Color.WHITE);
        b.eventTextInclude.eventDate.setTextColor(Color.parseColor("#BDBDBD"));
        b.eventTextInclude.eventLocation.setTextColor(Color.parseColor("#BDBDBD"));
        b.eventTextInclude.eventCapacity.setTextColor(Color.parseColor("#9E9E9E"));
        b.eventImageInclude.eventImageMask.setBackgroundResource(R.drawable.fade_mask_dark);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    /**
     * Callback interface for item clicks.
     */
    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    /**
     * ViewHolder for a single event card.
     */
    static class EventViewHolder extends RecyclerView.ViewHolder {
        ItemEventBinding binding;

        EventViewHolder(ItemEventBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
