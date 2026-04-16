package com.epita.eventplanner.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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

        Date date = parse(dateStr);
        if (date != null) {
            return HUMAN_FORMAT.format(date);
        }

        return dateStr; // Return raw string if all parsing fails
    }

    public static boolean isPast(String dateStr) {
        Date date = parse(dateStr);
        // It's past if the current time is after the event date
        return date != null && new Date().after(date);
    }

    public static boolean isToday(String dateStr) {
        Date date = parse(dateStr);
        if (date == null) return false;
        Calendar today = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.setTime(date);
        return today.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isThisWeek(String dateStr) {
        Date date = parse(dateStr);
        if (date == null) return false;
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.setTime(date);

        Calendar weekLater = Calendar.getInstance();
        weekLater.add(Calendar.DAY_OF_YEAR, 7);

        return date.after(now.getTime()) && date.before(weekLater.getTime());
    }

    public static boolean isThisMonth(String dateStr) {
        Date date = parse(dateStr);
        if (date == null) return false;
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.setTime(date);

        Calendar monthLater = Calendar.getInstance();
        monthLater.add(Calendar.MONTH, 1);

        return date.after(now.getTime()) && date.before(monthLater.getTime());
    }

    public static boolean isInRange(String dateStr, String from, String to) {
        Date eventDate = parse(dateStr);
        if (eventDate == null) return false;

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try {
            if (from != null && !from.isEmpty()) {
                Date fromDate = df.parse(from);
                if (fromDate != null && eventDate.before(fromDate)) return false;
            }
            if (to != null && !to.isEmpty()) {
                Date toDate = df.parse(to);
                if (toDate != null && eventDate.after(toDate)) return false;
            }
        } catch (ParseException e) {
            return true; // If we can't parse filters, don't exclude the event
        }
        return true;
    }

    private static Date parse(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;

        // Try RFC 1123 / SQL format: Thu, 16 Apr 2026 18:16:30 GMT
        try {
            SimpleDateFormat rfc1123 = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
            rfc1123.setTimeZone(TimeZone.getTimeZone("GMT"));
            return rfc1123.parse(dateStr);
        } catch (ParseException ignored) {
        }

        try {
            SimpleDateFormat isoNoZ = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            return isoNoZ.parse(dateStr);
        } catch (ParseException ignored) {
        }

        try {
            SimpleDateFormat isoWithZ = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            isoWithZ.setTimeZone(TimeZone.getTimeZone("UTC"));
            return isoWithZ.parse(dateStr);
        } catch (ParseException ignored) {
        }

        try {
            SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            return simple.parse(dateStr);
        } catch (ParseException ignored) {
        }
        return null;
    }
}
