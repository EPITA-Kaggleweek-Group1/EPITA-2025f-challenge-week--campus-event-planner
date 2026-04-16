package com.epita.eventplanner.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class FavoritesManager {
    private static final String PREF_NAME = "event_planner_prefs";
    private static final String KEY_FAVORITES = "favorite_event_ids";
    private final SharedPreferences prefs;

    public FavoritesManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isFavorite(int eventId) {
        Set<String> favorites = prefs.getStringSet(KEY_FAVORITES, new HashSet<>());
        return favorites.contains(String.valueOf(eventId));
    }

    public void toggleFavorite(int eventId) {
        Set<String> favorites = new HashSet<>(prefs.getStringSet(KEY_FAVORITES, new HashSet<>()));
        String idStr = String.valueOf(eventId);
        if (favorites.contains(idStr)) {
            favorites.remove(idStr);
        } else {
            favorites.add(idStr);
        }
        prefs.edit().putStringSet(KEY_FAVORITES, favorites).apply();
    }
}
