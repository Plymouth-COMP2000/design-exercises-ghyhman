package com.example.resaurantapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class StaffBookingPage extends AppCompatActivity {

    private RecyclerView bookingsList;
    private StaffBookingsAdapter bookingsAdapter;
    private DB dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.staff_booking_page);
        dbHelper = new DB(this);
        bookingsList = findViewById(R.id.staff_bookings_recycler);
        ImageView btnBack = findViewById(R.id.back_arrow);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        bookingsList.setLayoutManager(new LinearLayoutManager(this));
        loadBookings();
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadBookings();
    }
    private void loadBookings() {
        try {
            ArrayList<Bookings> list = dbHelper.fetchBookings();
            bookingsAdapter = new StaffBookingsAdapter(list, this, dbHelper);
            bookingsList.setAdapter(bookingsAdapter);
        } catch (Exception e) {
            Log.e("STAFF_BOOKINGS", "Error loading bookings", e);
        }
    }
}
