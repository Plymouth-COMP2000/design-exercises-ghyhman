package com.example.resaurantapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
public class StaffSettings extends AppCompatActivity {

    private static final String TAG = "StaffSettings";

    private Switch newBookingSwitch, guestCancelSwitch;
    private Button signOutBtn, deleteAccountBtn;
    private TextView errorText;
    private SharedPreferences prefs;
    private RequestQueue requestQueue;

    public static final String PREF_STAFF_NOTIFY_NEW_BOOKING = "PREF_STAFF_NOTIFY_NEW_BOOKING";
    public static final String PREF_STAFF_NOTIFY_GUEST_CANCEL = "PREF_STAFF_NOTIFY_GUEST_CANCEL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.staff_settings);

        prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        requestQueue = Volley.newRequestQueue(this);

        newBookingSwitch = findViewById(R.id.cancel_notify_switch2);
        guestCancelSwitch = findViewById(R.id.notification_status_switch2);
        signOutBtn = findViewById(R.id.sign_out_button);
        deleteAccountBtn = findViewById(R.id.delete_account_button);
        errorText = findViewById(R.id.delete_error_text);

        if (newBookingSwitch == null || guestCancelSwitch == null) {
            Log.e(TAG, "Switch view(s) not found. Check staff_settings.xml IDs.");
            finish();
            return;
        }

        newBookingSwitch.setChecked(prefs.getBoolean(PREF_STAFF_NOTIFY_NEW_BOOKING, true));
        guestCancelSwitch.setChecked(prefs.getBoolean(PREF_STAFF_NOTIFY_GUEST_CANCEL, true));

        newBookingSwitch.setOnCheckedChangeListener((v, isChecked) ->
                prefs.edit().putBoolean(PREF_STAFF_NOTIFY_NEW_BOOKING, isChecked).apply());

        guestCancelSwitch.setOnCheckedChangeListener((v, isChecked) ->
                prefs.edit().putBoolean(PREF_STAFF_NOTIFY_GUEST_CANCEL, isChecked).apply());

        signOutBtn.setOnClickListener(v -> handleSignOut());
        deleteAccountBtn.setOnClickListener(v -> showDeleteConfirmation());

        if (findViewById(R.id.back_arrow) != null) {
            findViewById(R.id.back_arrow).setOnClickListener(v -> finish());
        }

        requestNotificationPermission();
    }
    private void handleSignOut() {
        prefs.edit().clear().apply();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account?")
                .setPositiveButton("Yes", (dialog, which) -> deleteAccount())
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteAccount() {
        String username = prefs.getString("session_username", null);
        
        if (username == null) {
            showError("Error: No active session found.");
            return;
        }

        deleteAccountBtn.setEnabled(false);
        errorText.setVisibility(View.GONE);

        try {
            String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8.toString());
            String deleteUrl = "http://10.240.72.69/comp2000/coursework/delete_user/10894247/" + encodedUsername;
            
            Log.d(TAG, "Attempting staff account deletion for: " + username);

            JsonObjectRequest deleteRequest = new JsonObjectRequest(Request.Method.DELETE, deleteUrl, null,
                    response -> {
                        Log.d(TAG, "Delete Success: " + response.toString());
                        Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                        handleSignOut();
                    },
                    error -> {
                        deleteAccountBtn.setEnabled(true);
                        if (error.networkResponse != null && error.networkResponse.statusCode == 200) {
                            Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                            handleSignOut();
                        } else {
                            Log.e(TAG, "Delete failed. Error: " + error.getMessage());
                            showError("Deletion failed. Please try again later.");
                        }
                    });

            requestQueue.add(deleteRequest);

        } catch (Exception e) {
            deleteAccountBtn.setEnabled(true);
            Log.e(TAG, "Encoding error", e);
            showError("An internal error occurred.");
        }
    }

    private void showError(String message) {
        if (errorText != null) {
            errorText.setText(message);
            errorText.setVisibility(View.VISIBLE);
        }
    }
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        101
                );
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Notifications are disabled for Staff.", Toast.LENGTH_LONG).show();
        }
    }
}
