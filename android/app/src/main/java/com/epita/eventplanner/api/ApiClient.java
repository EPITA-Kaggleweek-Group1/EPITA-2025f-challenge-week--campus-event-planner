package com.epita.eventplanner.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Lightweight HTTP helper for communicating with the Flask backend.
 *
 * The base URL defaults to the Android emulator's alias for localhost.
 * Change it to your machine's LAN IP if testing on a physical device.
 */
public class ApiClient {

    // 10.0.2.2 is the Android emulator alias for the host machine's localhost
    private static final String BASE_URL = "http://10.0.2.2:5000";

    /**
     * Perform a GET request and return the response body as a String.
     *
     * @param path API path, e.g. "/events" or "/events/1"
     * @return JSON response body
     * @throws Exception on network or HTTP error
     */
    public static String fetchJson(String path) throws Exception {
        URL url = new URL(BASE_URL + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        int status = conn.getResponseCode();
        if (status != HttpURLConnection.HTTP_OK) {
            throw new Exception("HTTP error: " + status);
        }

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        conn.disconnect();

        return sb.toString();
    }

    /**
     * Return the base URL (useful for building full URLs elsewhere).
     */
    public static String getBaseUrl() {
        return BASE_URL;
    }
}
