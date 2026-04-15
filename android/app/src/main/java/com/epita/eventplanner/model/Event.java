package com.epita.eventplanner.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Event {
    private int id;
    private String title;
    private String description;
    private String location;
    private String date;
    private int capacity;
    private String imageUrl;

    public Event(int id, String title, String description, String location, String date, int capacity, String imageUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.date = date;
        this.capacity = capacity;
        this.imageUrl = imageUrl;
    }

    public static Event fromJson(JSONObject json) throws JSONException {
        return new Event(
                json.getInt("id"),
                json.getString("title"),
                json.getString("description"),
                json.getString("location"),
                json.getString("date"),
                json.getInt("capacity"),
                json.optString("image_url", "")
        );
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public String getDate() { return date; }
    public int getCapacity() { return capacity; }
    public String getImageUrl() { return imageUrl; }
}