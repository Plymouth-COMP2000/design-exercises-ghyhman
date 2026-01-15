package com.example.resaurantapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DB extends SQLiteOpenHelper {

    private static final String DB_NAME = "restaurant.db";
    private static final int DB_VER = 8;
    private static final String TBL_MENU = "menu_items";
    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_PRICE = "price";
    private static final String COL_DESC = "description";
    private static final String COL_IMG = "image";

    private static final String TBL_BOOKINGS = "bookings";
    private static final String COL_BOOKING_ID = "id";
    private static final String COL_GUEST_USER = "guest_username";
    private static final String COL_GUEST_NAME = "guest_name";
    private static final String COL_DATE = "date";
    private static final String COL_TIME = "time";
    private static final String COL_STATUS = "status";
    private static final String COL_NOTIF_PENDING = "notification_pending";
    private static final String COL_STAFF_NOTIF = "staff_notification_pending";

    public DB(Context context) {
        super(context, DB_NAME, null, DB_VER);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_MENU_TABLE = "CREATE TABLE IF NOT EXISTS " + TBL_MENU + "("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_NAME + " TEXT NOT NULL,"
                + COL_PRICE + " REAL NOT NULL,"
                + COL_DESC + " TEXT,"
                + COL_IMG + " TEXT" + ")";
        db.execSQL(CREATE_MENU_TABLE);

        String CREATE_BOOKINGS_TABLE = "CREATE TABLE IF NOT EXISTS " + TBL_BOOKINGS + "("
                + COL_BOOKING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_GUEST_USER + " TEXT NOT NULL,"
                + COL_GUEST_NAME + " TEXT NOT NULL,"
                + COL_DATE + " TEXT NOT NULL,"
                + COL_TIME + " TEXT NOT NULL,"
                + COL_STATUS + " TEXT NOT NULL DEFAULT 'active',"
                + COL_NOTIF_PENDING + " INTEGER NOT NULL DEFAULT 0,"
                + COL_STAFF_NOTIF + " INTEGER NOT NULL DEFAULT 0" + ")";
        db.execSQL(CREATE_BOOKINGS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 8) {
            db.execSQL("DROP TABLE IF EXISTS " + TBL_BOOKINGS);
            onCreate(db);
        }
    }
    public long addBooking(String username, String fullName, String date, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_GUEST_USER, username);
        values.put(COL_GUEST_NAME, fullName);
        values.put(COL_DATE, date);
        values.put(COL_TIME, time);
        values.put(COL_STATUS, "active");
        values.put(COL_NOTIF_PENDING, 0);
        values.put(COL_STAFF_NOTIF, 1); // Notify staff if there is a new booking
        return db.insert(TBL_BOOKINGS, null, values);
    }

    public int updateBookingDateTime(long id, String newDate, String newTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DATE, newDate);
        values.put(COL_TIME, newTime);
        values.put(COL_STAFF_NOTIF, 1); // Notify staff if the booking is amended
        return db.update(TBL_BOOKINGS, values, COL_BOOKING_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public int cancelBooking(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STATUS, "cancelled");
        values.put(COL_STAFF_NOTIF, 1); // Notify staff if the guest cancels their reservation
        return db.update(TBL_BOOKINGS, values, COL_BOOKING_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public int cancelBookingByStaff(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STATUS, "cancelled");
        values.put(COL_NOTIF_PENDING, 1); // Notify guest if there booking is cancelled
        return db.update(TBL_BOOKINGS, values, COL_BOOKING_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public ArrayList<Bookings> getGuestNotifs(String username) {
        ArrayList<Bookings> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TBL_BOOKINGS, null, 
                COL_GUEST_USER + "=? AND " + COL_NOTIF_PENDING + "=1", 
                new String[]{username}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Bookings b = cursorToBooking(cursor);
                list.add(b);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public ArrayList<Bookings> getStaffNotifs() {
        ArrayList<Bookings> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TBL_BOOKINGS, null, 
                COL_STAFF_NOTIF + "=1", 
                null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Bookings b = cursorToBooking(cursor);
                list.add(b);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public void clearGuestNotif(long bookingId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NOTIF_PENDING, 0);
        db.update(TBL_BOOKINGS, values, COL_BOOKING_ID + " = ?", new String[]{String.valueOf(bookingId)});
    }

    public void clearStaffNotif(long bookingId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STAFF_NOTIF, 0);
        db.update(TBL_BOOKINGS, values, COL_BOOKING_ID + " = ?", new String[]{String.valueOf(bookingId)});
    }

    public ArrayList<Bookings> getBookingsForUser(String username) {
        ArrayList<Bookings> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TBL_BOOKINGS, null, 
                COL_GUEST_USER + "=? AND " + COL_STATUS + "='active'", 
                new String[]{username}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToBooking(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public ArrayList<Bookings> fetchBookings() {
        ArrayList<Bookings> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TBL_BOOKINGS + " WHERE " + COL_STATUS + "='active'", null);
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToBooking(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    private Bookings cursorToBooking(Cursor cursor) {
        Bookings b = new Bookings();
        b.bookingId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_BOOKING_ID));
        b.guestUser = cursor.getString(cursor.getColumnIndexOrThrow(COL_GUEST_USER));
        b.guestNameTxt = cursor.getString(cursor.getColumnIndexOrThrow(COL_GUEST_NAME));
        b.bookedDate = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE));
        b.bookedTime = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIME));
        b.statusTxt = cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS));
        b.notifPending = cursor.getInt(cursor.getColumnIndexOrThrow(COL_NOTIF_PENDING));
        return b;
    }

    public boolean bookingExists(String username, String date, String time) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TBL_BOOKINGS, null, 
                COL_GUEST_USER + "=? AND " + COL_DATE + "=? AND " + COL_TIME + "=?",
                new String[]{username, date, time}, null, null, null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public long addItemToMenu(String name, double price, String description, String image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_PRICE, price);
        values.put(COL_DESC, description);
        values.put(COL_IMG, image);
        return db.insert(TBL_MENU, null, values);
    }
    public int saveMenuEdit(long id, String name, double price, String description, String image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_PRICE, price);
        values.put(COL_DESC, description);
        values.put(COL_IMG, image);
        return db.update(TBL_MENU, values, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public int removeMenuItem(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TBL_MENU, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public ArrayList<MenuItem> fetchMenu() {
        ArrayList<MenuItem> menuList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TBL_MENU, null);
        if (cursor.moveToFirst()) {
            do {
                MenuItem item = new MenuItem();
                item.itemId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID));
                item.itemName = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME));
                item.itemPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PRICE));
                item.itemDesc = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESC));
                item.imgPath = cursor.getString(cursor.getColumnIndexOrThrow(COL_IMG));
                menuList.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return menuList;
    }

    public MenuItem getMenuItemByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TBL_MENU, null, COL_NAME + " = ?", new String[]{name}, null, null, null);
        MenuItem item = null;
        if (cursor != null && cursor.moveToFirst()) {
            item = new MenuItem();
            item.itemId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID));
            item.itemName = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME));
            item.itemPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PRICE));
            item.itemDesc = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESC));
            item.imgPath = cursor.getString(cursor.getColumnIndexOrThrow(COL_IMG));
            cursor.close();
        }
        return item;
    }
}
