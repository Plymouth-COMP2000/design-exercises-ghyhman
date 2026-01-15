package com.example.resaurantapplication;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import java.util.Locale;

public class MakeBookingGuest extends AppCompatActivity {

    private EditText dateBox, timeBox;
    private Button btnBook;
    private DB dbHelper;
    private String username, userName;
    private SharedPreferences prefsStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.make_booking_guest);

        dbHelper = new DB(this);
        prefsStore = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        username = prefsStore.getString("session_username", null);
        userName = prefsStore.getString("session_name", "Guest");

        dateBox = findViewById(R.id.date_input);
        timeBox = findViewById(R.id.time_input);
        btnBook = findViewById(R.id.confirm_btn);

        dateBox.setOnClickListener(v -> showDatePicker());
        timeBox.setOnClickListener(v -> showTimePicker());

        if (findViewById(R.id.back_arrow) != null) {
            findViewById(R.id.back_arrow).setOnClickListener(v -> finish());
        }

        btnBook.setOnClickListener(v -> {
            if (username == null || username.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_session), Toast.LENGTH_LONG).show();
                return;
            }

            String date = dateBox.getText().toString().trim();
            String time = timeBox.getText().toString().trim();

            if (date.isEmpty() || time.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_select_date_time), Toast.LENGTH_SHORT).show();
                return;
            }

            if (dbHelper.bookingExists(username, date, time)) {
                Toast.makeText(this, getString(R.string.error_booking_exists), Toast.LENGTH_SHORT).show();
            } else {
                long id = dbHelper.addBooking(username, userName, date, time);
                if (id != -1) {
                    if (prefsStore.getBoolean(GuestSettings.PREF_GUEST_NOTIFY_SELF_CANCEL, true)) {
                        NotificationHelper.showGuestBookingConfirmedNotification(this, date, time);
                    }
                    Toast.makeText(this, getString(R.string.booking_confirmed), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, getString(R.string.booking_failed), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
                    dateBox.setText(selectedDate);
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    timeBox.setText(selectedTime);
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        timePickerDialog.show();
    }
}
