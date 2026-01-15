package com.example.resaurantapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
public class NotificationHelper {

    private static final String CHANNEL_ID = "restaurant_notifications";

    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notif_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    private static void showNotification(Context context, String title, String message) {
        createNotificationChannel(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }


    public static void showGuestStatusChangedNotification(Context context, String date, String time, String status) {
        String title = "Booking " + status;
        String message = context.getString(R.string.notif_guest_status_msg, date, time, status);
        showNotification(context, title, message);
    }

    public static void showGuestBookingConfirmedNotification(Context context, String date, String time) {
        showNotification(context, context.getString(R.string.notif_booking_confirmed_title), 
                context.getString(R.string.notif_booking_confirmed_msg, date, time));
    }

    public static void showGuestSelfCancelNotification(Context context, String date, String time) {
        showNotification(context, context.getString(R.string.notif_booking_cancelled_title), 
                context.getString(R.string.notif_booking_cancelled_msg, date, time));
    }

    public static void showStaffNewBookingNotification(Context context, String guestName, String date, String time) {
        showNotification(context, context.getString(R.string.notif_staff_alert_title), 
                context.getString(R.string.notif_staff_alert_msg, guestName, date, time));
    }

    public static void showStaffGuestCancelNotification(Context context, String guestName, String date, String time) {
        showNotification(context, context.getString(R.string.notif_booking_cancelled_title), 
                context.getString(R.string.notif_staff_cancel_msg, guestName, date, time));
    }
}
