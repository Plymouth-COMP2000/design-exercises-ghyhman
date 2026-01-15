package com.example.resaurantapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
public class StaffHomePage extends AppCompatActivity {

    private DB dbHelper;
    private SharedPreferences prefsStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.staff_home_page);

        dbHelper = new DB(this);
        prefsStore = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        ImageButton addBtn = findViewById(R.id.add_menu_items);
        ImageButton deleteBtn = findViewById(R.id.delete_menu_items);
        ImageButton editBtn = findViewById(R.id.edit_menu_items);
        ImageButton bookingBtn = findViewById(R.id.staff_booking_page);
        ImageButton settingsBtn = findViewById(R.id.staff_settings);

        if (addBtn != null) {
            addBtn.setOnClickListener(v -> {
                Intent intent = new Intent(this, AddMenuItems.class);
                startActivity(intent);
            });
        }

        if (deleteBtn != null) {
            deleteBtn.setOnClickListener(v -> {
                Intent intent = new Intent(this, DeleteMenuItems.class);
                startActivity(intent);
            });
        }

        if (editBtn != null) {
            editBtn.setOnClickListener(v -> {
                Intent intent = new Intent(this, EditMenuItems.class);
                startActivity(intent);
            });
        }

        if (bookingBtn != null) {
            bookingBtn.setOnClickListener(v -> {
                Intent intent = new Intent(this, StaffBookingPage.class);
                startActivity(intent);
            });
        }

        if (settingsBtn != null) {
            settingsBtn.setOnClickListener(v -> {
                Intent intent = new Intent(this, StaffSettings.class);
                startActivity(intent);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        pollNotifs();
    }

    private void pollNotifs() {
        boolean notifyNew = prefsStore.getBoolean(StaffSettings.PREF_STAFF_NOTIFY_NEW_BOOKING, true);
        boolean notifyGuestAction = prefsStore.getBoolean(StaffSettings.PREF_STAFF_NOTIFY_GUEST_CANCEL, true);

        ArrayList<Bookings> pending = dbHelper.getStaffNotifs();
        for (Bookings b : pending) {
            if ("active".equals(b.statusTxt) && notifyNew) {
                NotificationHelper.showStaffNewBookingNotification(this, b.guestNameTxt, b.bookedDate, b.bookedTime);
            } else if ("cancelled".equals(b.statusTxt) && notifyGuestAction) {
                NotificationHelper.showStaffGuestCancelNotification(this, b.guestNameTxt, b.bookedDate, b.bookedTime);
            }
            dbHelper.clearStaffNotif(b.bookingId);
        }
    }
}
