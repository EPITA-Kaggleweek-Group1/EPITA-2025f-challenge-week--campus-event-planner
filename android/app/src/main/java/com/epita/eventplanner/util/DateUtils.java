package com.epita.eventplanner.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {

    private static final SimpleDateFormat HUMAN_FORMAT = new SimpleDateFormat("EEE, MMM d, yyyy 'at' h:mm a", Locale.getDefault());

    /**
     * Formats an API date string into a human-readable format.
     * Handles formats like:
     * - 2026-04-28T19:00:00 (ISO-8601 without offset)
     * - 2026-04-28T19:00:00Z (ISO-8601 UTC)
     * - 2026-04-28 19:00:00 (Simple space-separated)
     */
    public static String formatToHuman(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return "";

        // Try primary ISO format (without 'Z')
        try {
            SimpleDateFormat isoNoZ = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            return HUMAN_FORMAT.format(isoNoZ.parse(dateStr));
        } catch (ParseException ignored) {}

        // Try ISO format with 'Z'
        try {
            SimpleDateFormat isoWithZ = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            isoWithZ.setTimeZone(TimeZone.getTimeZone("UTC"));
            return HUMAN_FORMAT.format(isoWithZ.parse(dateStr));
        } catch (ParseException ignored) {}

        // Try simple space format
        try {
            SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            return HUMAN_FORMAT.format(simple.parse(dateStr));
        } catch (ParseException ignored) {}

        return dateStr; // Return raw string if all parsing fails
    }
}
