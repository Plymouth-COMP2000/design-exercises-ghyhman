package com.example.resaurantapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
public class StaffBookingsAdapter extends RecyclerView.Adapter<StaffBookingsAdapter.StaffViewHolder> {

    private ArrayList<Bookings> bookingsData;
    private Context ctx;
    private DB dbHelper;

    public StaffBookingsAdapter(ArrayList<Bookings> bookingsList, Context context, DB db) {
        this.bookingsData = bookingsList;
        this.ctx = context;
        this.dbHelper = db;
    }
    @NonNull
    @Override
    public StaffViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.item_guest_booking_staff, parent, false);
        return new StaffViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StaffViewHolder holder, int position) {
        Bookings booking = bookingsData.get(position);
        holder.guestNameText.setText(booking.guestNameTxt);
        holder.dateTimeText.setText(booking.bookedDate + "  " + booking.bookedTime);
        holder.confirmSwitch.setChecked(false);
        holder.cancelBtn.setEnabled(false);
        holder.confirmSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            holder.cancelBtn.setEnabled(isChecked);
        });

        holder.cancelBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(ctx)
                    .setTitle("Cancel this booking?")
                    .setMessage("Are you sure you want to cancel the booking for " + booking.guestNameTxt + "?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        int rows = dbHelper.cancelBookingByStaff(booking.bookingId);
                        if (rows > 0) {
                            int currentPos = holder.getAdapterPosition();
                            if (currentPos != RecyclerView.NO_POSITION) {
                                bookingsData.remove(currentPos);
                                notifyItemRemoved(currentPos);
                                notifyItemRangeChanged(currentPos, bookingsData.size());
                                Toast.makeText(ctx, "Booking cancelled. Guest will be notified on next login.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ctx, "Failed to cancel", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return bookingsData.size();
    }

    public static class StaffViewHolder extends RecyclerView.ViewHolder {
        TextView guestNameText, dateTimeText;
        Switch confirmSwitch;
        ImageButton cancelBtn;

        public StaffViewHolder(@NonNull View itemView) {
            super(itemView);
            guestNameText = itemView.findViewById(R.id.staff_guest_name);
            dateTimeText = itemView.findViewById(R.id.staff_date_time);
            confirmSwitch = itemView.findViewById(R.id.confirm_cancel_switch);
            cancelBtn = itemView.findViewById(R.id.staff_cancel_btn);
        }
    }
}
