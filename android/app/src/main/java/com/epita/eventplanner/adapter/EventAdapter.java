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
import com.epita.eventplanner.util.FavoritesManager;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter that displays a list of {@link Event} objects
 * using the item_event card layout.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final OnEventClickListener listener;
    private final FavoritesManager favoritesManager;
    private List<Event> events = new ArrayList<>();

    public EventAdapter(OnEventClickListener listener, FavoritesManager favoritesManager) {
        this.listener = listener;
        this.favoritesManager = favoritesManager;
    }

    /**
     * Replace the current dataset and refresh the list.
     */
    public void setEvents(List<Event> events) {
        this.events = new ArrayList<>(events);
        notifyDataSetChanged();
    }

    /**
     * Append new events to the current dataset.
     */
    public void addEvents(List<Event> newEvents) {
        int startPosition = this.events.size();
        this.events.addAll(newEvents);
        notifyItemRangeInserted(startPosition, newEvents.size());
    }

    public void clear() {
        this.events.clear();
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

        updateStatusLabel(b, event);
        updateCapacityIndicator(b, event);
        updateFavoriteIcon(b, event, false);
        setupShareButton(b, event);

        // Default state (Light)
        resetToDefaultState(holder);

        String imageUrl = event.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            b.eventImageInclude.eventImage.setVisibility(View.VISIBLE);
            b.eventImageInclude.eventImageMask.setVisibility(View.VISIBLE);

            Glide.with(holder.itemView.getContext())
                    .asBitmap()
                    .load(imageUrl)
                    .placeholder(R.drawable.bg_abstract_placeholder)
                    .error(R.drawable.bg_abstract_placeholder)
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

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            b.eventImageInclude.eventImage.setImageDrawable(errorDrawable);
                        }
                    });
        } else {
            b.eventImageInclude.eventImage.setVisibility(View.VISIBLE);
            b.eventImageInclude.eventImageMask.setVisibility(View.VISIBLE);
            b.eventImageInclude.eventImage.setImageResource(R.drawable.bg_abstract_placeholder);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEventClick(event);
            }
        });
    }

    private void updateStatusLabel(ItemEventBinding b, Event event) {
        String date = event.getDate();
        android.widget.TextView textView = b.eventTextInclude.eventStatusLabel;

        if (DateUtils.isPast(date)) {
            textView.setVisibility(View.VISIBLE);
            textView.setText(R.string.status_passed);
            textView.setBackgroundColor(Color.parseColor("#757575")); // Gray
        } else if (DateUtils.isToday(date)) {
            textView.setVisibility(View.VISIBLE);
            textView.setText(R.string.status_today);
            textView.setBackgroundColor(Color.parseColor("#F44336")); // Red
        } else if (DateUtils.isThisWeek(date)) {
            textView.setVisibility(View.VISIBLE);
            int days = DateUtils.getDaysUntil(date);
            if (days == 1) {
                textView.setText("IN 1 DAY");
            } else {
                textView.setText("IN " + days + " DAYS");
            }
            textView.setBackgroundColor(Color.parseColor("#2196F3")); // Blue
        } else if (DateUtils.isThisMonth(date)) {
            textView.setVisibility(View.VISIBLE);
            textView.setText(R.string.status_month);
            textView.setBackgroundColor(Color.parseColor("#4CAF50")); // Green
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    private void updateFavoriteIcon(ItemEventBinding b, Event event, boolean isDark) {
        boolean isFav = favoritesManager.isFavorite(event.getId());
        b.eventTextInclude.favoriteIcon.setImageResource(
                isFav ? R.drawable.ic_heart_pink_filled : R.drawable.ic_heart_outline
        );
        
        if (isFav) {
            b.eventTextInclude.favoriteIcon.clearColorFilter();
        } else {
            b.eventTextInclude.favoriteIcon.setColorFilter(isDark ? Color.WHITE : Color.parseColor("#757575"));
        }
        
        b.eventTextInclude.favoriteIcon.setOnClickListener(v -> {
            favoritesManager.toggleFavorite(event.getId());
            updateFavoriteIcon(b, event, isDark);
        });
    }

    private void setupShareButton(ItemEventBinding b, Event event) {
        b.eventTextInclude.shareIcon.setOnClickListener(v -> {
            String shareText = event.getTitle() + "\n" +
                    DateUtils.formatToHuman(event.getDate()) + "\n" +
                    event.getLocation();

            android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                    v.getContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Event Details", shareText);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                android.widget.Toast.makeText(v.getContext(), "Event details copied to clipboard!", android.widget.Toast.LENGTH_SHORT).show();
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
        b.eventTextInclude.shareIcon.setColorFilter(Color.parseColor("#757575"));
        
        int pos = holder.getBindingAdapterPosition();
        if (pos != RecyclerView.NO_POSITION && pos < events.size()) {
            updateFavoriteIcon(b, events.get(pos), false);
        }
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
        b.eventTextInclude.shareIcon.setColorFilter(Color.WHITE);
        
        int pos = holder.getBindingAdapterPosition();
        if (pos != RecyclerView.NO_POSITION && pos < events.size()) {
            updateFavoriteIcon(b, events.get(pos), true);
        }
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
