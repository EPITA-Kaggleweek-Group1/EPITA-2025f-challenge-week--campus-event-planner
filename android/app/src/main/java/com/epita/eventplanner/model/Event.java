package com.epita.eventplanner.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Plain data class representing a campus event.
 *
 * Mirrors the JSON object returned by GET /events.
 */
public class Event {

    private int id;
    private String title;
    private String description;
    private String date;
    private String location;
    private int capacity;
    private String imageUrl;
    private String createdAt;

    // ------------------------------------------------------------------ //
    //  Constructors
    // ------------------------------------------------------------------ //

    public Event() {}

    /**
     * Convenience factory that parses a JSONObject into an Event.
     */
    public static Event fromJson(JSONObject json) throws JSONException {
        Event e = new Event();
        e.id = json.optInt("id", 0);
        e.title = json.optString("title", "");
        e.description = json.optString("description", "");
        e.date = json.optString("date", "");
        e.location = json.optString("location", "");
        e.capacity = json.optInt("capacity", 50);
        e.imageUrl = json.optString("image_url", "");
        e.createdAt = json.optString("created_at", "");
        return e;
    }

    // ------------------------------------------------------------------ //
    //  Getters & setters
    // ------------------------------------------------------------------ //

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
