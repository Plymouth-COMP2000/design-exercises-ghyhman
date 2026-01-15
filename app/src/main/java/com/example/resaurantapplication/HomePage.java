package com.example.resaurantapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class HomePage extends AppCompatActivity {

    private DB dbHelper;
    private SharedPreferences prefsStore;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        dbHelper = new DB(this);
        prefsStore = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        username = prefsStore.getString("session_username", null);

        String userEmail = getIntent().getStringExtra("user_email");
        String userName = getIntent().getStringExtra("user_name");

        ImageButton amendBtn = findViewById(R.id.guest_amend_cancel);
        ImageButton menuBtn = findViewById(R.id.guest_menu);
        ImageButton bookingsBtn = findViewById(R.id.guest_bookings);
        ImageButton settingsBtn = findViewById(R.id.guest_settings);

        if (amendBtn != null) {
            amendBtn.setOnClickListener(v -> {
                Intent intent = new Intent(this, GuestBookings.class);
                intent.putExtra("user_email", userEmail);
                intent.putExtra("user_name", userName);
                startActivity(intent);
            });
        }
        if (menuBtn != null) {
            menuBtn.setOnClickListener(v -> {
                Intent intent = new Intent(this, MenuActivity.class);
                intent.putExtra("user_email", userEmail);
                intent.putExtra("user_name", userName);
                startActivity(intent);
            });
        }
        if (bookingsBtn != null) {
            bookingsBtn.setOnClickListener(v -> {
                Intent intent = new Intent(this, MakeBookingGuest.class);
                intent.putExtra("user_email", userEmail);
                intent.putExtra("user_name", userName);
                startActivity(intent);
            });
        }
        if (settingsBtn != null) {
            settingsBtn.setOnClickListener(v -> {
                Intent intent = new Intent(this, GuestSettings.class);
                intent.putExtra("user_email", userEmail);
                intent.putExtra("user_name", userName);
                startActivity(intent);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkForNotifications();
    }

    private void checkForNotifications() {
        if (username != null) {
            boolean notifyEnabled = prefsStore.getBoolean(GuestSettings.PREF_GUEST_NOTIFY_STATUS_CHANGE, true);
            
            ArrayList<Bookings> pending = dbHelper.getGuestNotifs(username);
            for (Bookings b : pending) {
                if (notifyEnabled) {
                    NotificationHelper.showGuestStatusChangedNotification(this, b.bookedDate, b.bookedTime, b.statusTxt);
                }
                dbHelper.clearGuestNotif(b.bookingId);
            }
        }
    }
}
