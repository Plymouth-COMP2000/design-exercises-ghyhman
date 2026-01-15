package com.example.resaurantapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class GuestBookings extends AppCompatActivity {

    private RecyclerView bookingsList;
    private GuestBookingsAdapter bookingsAdapter;
    private DB dbHelper;
    private String guestUser;
    private SharedPreferences prefsStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guest_bookings);

        dbHelper = new DB(this);
        prefsStore = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        guestUser = prefsStore.getString("session_username", null);
        
        ImageView btnBack = findViewById(R.id.back_arrow);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        bookingsList = findViewById(R.id.bookings_recycler_view);
        bookingsList.setLayoutManager(new LinearLayoutManager(this));

        loadMyBookings();
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadMyBookings();
    }
    private void loadMyBookings() {
        if (guestUser != null) {
            ArrayList<Bookings> bookingsData = dbHelper.getBookingsForUser(guestUser);
            bookingsAdapter = new GuestBookingsAdapter(this, bookingsData);
            bookingsList.setAdapter(bookingsAdapter);
        }
    }
}
