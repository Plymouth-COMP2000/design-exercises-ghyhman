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

public class GuestSettings extends AppCompatActivity {

    private static final String TAG = "GuestSettings";

    private Switch cancelNotifySwitch, statusChangeSwitch;
    private Button signOutBtn, deleteAccountBtn;
    private TextView errorText;
    private SharedPreferences prefs;
    private RequestQueue requestQueue;

    public static final String PREF_GUEST_NOTIFY_STATUS_CHANGE = "PREF_GUEST_NOTIFY_STATUS_CHANGE";
    public static final String PREF_GUEST_NOTIFY_SELF_CANCEL = "PREF_GUEST_NOTIFY_SELF_CANCEL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guest_settings);

        prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        requestQueue = Volley.newRequestQueue(this);

        cancelNotifySwitch = findViewById(R.id.cancel_notify_switch1); 
        statusChangeSwitch = findViewById(R.id.notification_status_switch1);
        signOutBtn = findViewById(R.id.sign_out_button);
        deleteAccountBtn = findViewById(R.id.delete_account_button);
        errorText = findViewById(R.id.delete_error_text);

        if (cancelNotifySwitch == null || statusChangeSwitch == null) {
            Log.e(TAG, "Switch view(s) not found. Check guest_settings.xml IDs.");
            finish();
            return;
        }

        cancelNotifySwitch.setChecked(prefs.getBoolean(PREF_GUEST_NOTIFY_STATUS_CHANGE, true));
        statusChangeSwitch.setChecked(prefs.getBoolean(PREF_GUEST_NOTIFY_SELF_CANCEL, true));

        cancelNotifySwitch.setOnCheckedChangeListener((v, isChecked) -> 
            prefs.edit().putBoolean(PREF_GUEST_NOTIFY_STATUS_CHANGE, isChecked).apply());

        statusChangeSwitch.setOnCheckedChangeListener((v, isChecked) -> 
            prefs.edit().putBoolean(PREF_GUEST_NOTIFY_SELF_CANCEL, isChecked).apply());

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
                .setTitle(R.string.delete_confirmation_title)
                .setMessage(R.string.delete_confirmation_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> deleteAccount())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void deleteAccount() {
        String username = prefs.getString("session_username", null);
        
        if (username == null) {
            showError(getString(R.string.error_session));
            return;
        }

        deleteAccountBtn.setEnabled(false);
        errorText.setVisibility(View.GONE);

        try {
            String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8.toString());
            String deleteUrl = "http://10.240.72.69/comp2000/coursework/delete_user/10894247/" + encodedUsername;
            
            JsonObjectRequest deleteRequest = new JsonObjectRequest(Request.Method.DELETE, deleteUrl, null,
                    response -> {
                        Toast.makeText(this, getString(R.string.account_deleted), Toast.LENGTH_SHORT).show();
                        handleSignOut();
                    },
                    error -> {
                        deleteAccountBtn.setEnabled(true);
                        if (error.networkResponse != null && error.networkResponse.statusCode == 200) {
                            Toast.makeText(this, getString(R.string.account_deleted), Toast.LENGTH_SHORT).show();
                            handleSignOut();
                        } else {
                            showError(getString(R.string.delete_failed));
                        }
                    });

            requestQueue.add(deleteRequest);

        } catch (Exception e) {
            deleteAccountBtn.setEnabled(true);
            showError(getString(R.string.booking_failed));
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
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, getString(R.string.notifications_disabled_msg), Toast.LENGTH_LONG).show();
        }
    }
}
