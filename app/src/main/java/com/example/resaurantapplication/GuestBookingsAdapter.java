package com.example.resaurantapplication;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class GuestBookingsAdapter extends RecyclerView.Adapter<GuestBookingsAdapter.BookingViewHolder> {

    private Context ctx;
    private ArrayList<Bookings> bookingsData;
    private DB dbHelper;
    private SharedPreferences prefs;

    public GuestBookingsAdapter(Context context, ArrayList<Bookings> bookingsList) {
        this.ctx = context;
        this.bookingsData = bookingsList;
        this.dbHelper = new DB(context);
        this.prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.item_guest_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Bookings booking = bookingsData.get(position);
        holder.dateText.setText(booking.bookedDate);
        holder.timeText.setText(booking.bookedTime);

        holder.dateText.setOnClickListener(v -> showDatePicker(holder));
        holder.timeText.setOnClickListener(v -> showTimePicker(holder));

        holder.amendBtn.setOnClickListener(v -> {
            String newDate = holder.dateText.getText().toString();
            String newTime = holder.timeText.getText().toString();
            int rows = dbHelper.updateBookingDateTime(booking.bookingId, newDate, newTime);
            if (rows > 0) {
                booking.bookedDate = newDate;
                booking.bookedTime = newTime;
                Toast.makeText(ctx, "Booking amended", Toast.LENGTH_SHORT).show();
            }
        });

        holder.cancelBtn.setOnClickListener(v -> {
            int rows = dbHelper.cancelBooking(booking.bookingId);
            if (rows > 0) {
                if (prefs.getBoolean(GuestSettings.PREF_GUEST_NOTIFY_SELF_CANCEL, true)) {
                    NotificationHelper.showGuestSelfCancelNotification(ctx, booking.bookedDate, booking.bookedTime);
                }

                int currentPos = holder.getAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    bookingsData.remove(currentPos);
                    notifyItemRemoved(currentPos);
                    notifyItemRangeChanged(currentPos, bookingsData.size());
                }
                Toast.makeText(ctx, "Booking cancelled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePicker(BookingViewHolder holder) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(ctx, (view, year, month, dayOfMonth) -> {
            String selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
            holder.dateText.setText(selectedDate);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker(BookingViewHolder holder) {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(ctx, (view, hourOfDay, minute) -> {
            String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            holder.timeText.setText(selectedTime);
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    @Override
    public int getItemCount() {
        return bookingsData.size();
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView dateText, timeText;
        Button amendBtn, cancelBtn;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.booking_date);
            timeText = itemView.findViewById(R.id.booking_time);
            amendBtn = itemView.findViewById(R.id.amend_btn);
            cancelBtn = itemView.findViewById(R.id.cancel_btm);
        }
    }
}
