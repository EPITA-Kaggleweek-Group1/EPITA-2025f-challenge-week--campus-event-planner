package com.epita.eventplanner;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.epita.eventplanner.api.ApiClient;
import com.epita.eventplanner.databinding.ActivityEventDetailBinding;
import com.epita.eventplanner.databinding.DialogRegisterBinding;
import com.epita.eventplanner.model.Event;
import com.epita.eventplanner.util.DateUtils;

import org.json.JSONObject;

public class EventDetailActivity extends AppCompatActivity {
    private static final String TAG = "EventDetailActivity";
    private ActivityEventDetailBinding binding;
    private int eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEventDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        eventId = getIntent().getIntExtra("event_id", -1);

        binding.errorLayout.errorMessage.setText(getString(
                R.string.error_load_failed, getString(R.string.type_details)));
        binding.swipeRefreshLayout.setOnRefreshListener(
                () -> loadEventDetails(eventId));
        binding.errorLayout.retryButton.setOnClickListener(
                v -> loadEventDetails(eventId));

        binding.registerButton.setOnClickListener(v -> showRegisterDialog());

        if (eventId != -1) {
            loadEventDetails(eventId);
        } else {
            Toast.makeText(this, getString(R.string.error_event_not_found), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadEventDetails(int id) {
        if (!binding.swipeRefreshLayout.isRefreshing()) {
            showLoading();
        }

        new Thread(() -> {
            try {
                String jsonResponse = ApiClient.fetchJson("/events/" + id);
                JSONObject jsonObject = new JSONObject(jsonResponse);
                Event event = Event.fromJson(jsonObject);

                runOnUiThread(() -> {
                    populateUI(event);
                    showContent();
                    binding.swipeRefreshLayout.setRefreshing(false);
                });
            } catch (Exception e) {
                Log.e(TAG, "Failed to load event details", e);
                runOnUiThread(() -> {
                    showError();
                    binding.swipeRefreshLayout.setRefreshing(false);
                });
            }
        }).start();
    }

    private void showRegisterDialog() {
        DialogRegisterBinding dialogBinding = DialogRegisterBinding.inflate(getLayoutInflater());

        new AlertDialog.Builder(this)
                .setTitle(R.string.register_title)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.submit, (dialog, which) -> {
                    String name = dialogBinding.editName.getText().toString();
                    String email = dialogBinding.editEmail.getText().toString();
                    if (!name.isEmpty() && !email.isEmpty()) {
                        submitRegistration(name, email);
                    } else {
                        Toast.makeText(this, R.string.error_fill_fields, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void submitRegistration(String name, String email) {
        new Thread(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("user_name", name);
                json.put("email", email);

                ApiClient.postJson("/events/" + eventId + "/register", json.toString());

                runOnUiThread(() -> Toast.makeText(this, R.string.registration_success, Toast.LENGTH_LONG).show());
            } catch (ApiClient.ApiException e) {
                Log.e(TAG, "Registration failed", e);
                String message = e.getResponseBody();
                try {
                    JSONObject errorJson = new JSONObject(message);
                    if (errorJson.has("error")) {
                        message = errorJson.getString("error");
                    }
                } catch (Exception ignored) {}
                
                final String displayMessage = message;
                runOnUiThread(() -> Toast.makeText(this, getString(R.string.registration_failed) + ": " + displayMessage, Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                Log.e(TAG, "Registration failed", e);
                runOnUiThread(() -> Toast.makeText(this, R.string.registration_failed, Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void showLoading() {
        binding.loadingSpinner.setVisibility(View.VISIBLE);
        binding.errorLayout.errorView.setVisibility(View.GONE);
        binding.actualContent.setVisibility(View.GONE);
    }

    private void showContent() {
        binding.loadingSpinner.setVisibility(View.GONE);
        binding.errorLayout.errorView.setVisibility(View.GONE);
        binding.actualContent.setVisibility(View.VISIBLE);
    }

    private void showError() {
        binding.loadingSpinner.setVisibility(View.GONE);
        binding.errorLayout.errorView.setVisibility(View.VISIBLE);
        binding.actualContent.setVisibility(View.GONE);
    }

    private void populateUI(Event event) {
        binding.eventDetailContent.detailTitle.setText(event.getTitle());
        binding.eventDetailContent.detailLocation.setText(event.getLocation());
        binding.eventDetailContent.detailDescription.setText(
                event.getDescription());
        binding.eventDetailContent.detailCapacity.setText(
                getString(R.string.capacity_format, event.getCapacity()));
        binding.eventDetailContent.detailDate.setText(
                DateUtils.formatToHuman(event.getDate()));

        String imageUrl = event.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            binding.detailImage.setVisibility(android.view.View.VISIBLE);
            Glide.with(this).load(imageUrl).centerCrop().into(binding.detailImage);
        } else {
            binding.detailImage.setVisibility(android.view.View.GONE);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(event.getTitle());
        }
    }
}
