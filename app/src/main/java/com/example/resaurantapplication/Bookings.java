package com.example.resaurantapplication;

public class Bookings {
    public long bookingId;
    public String guestUser;
    public String guestNameTxt;
    public String bookedDate;
    public String bookedTime;
    public String statusTxt;
    public int notifPending;

    public Bookings() {
    }

    public Bookings(long bookingId, String guestUser, String guestNameTxt, String bookedDate, String bookedTime, String statusTxt, int notifPending) {
        this.bookingId = bookingId;
        this.guestUser = guestUser;
        this.guestNameTxt = guestNameTxt;
        this.bookedDate = bookedDate;
        this.bookedTime = bookedTime;
        this.statusTxt = statusTxt;
        this.notifPending = notifPending;
    }
}
