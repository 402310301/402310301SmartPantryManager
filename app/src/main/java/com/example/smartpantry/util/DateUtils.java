package com.example.smartpantry.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Small helper for working with expiry dates stored as "yyyy-MM-dd". */
public class DateUtils {

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    // Returned when an item has no expiry date
    public static final int NO_DATE = Integer.MAX_VALUE;

    /**
     * Returns how many days are left until the given date.
     * 0 = today, negative = already expired, NO_DATE = no (valid) date.
     */
    public static int daysUntil(String date) {
        if (date == null || date.trim().isEmpty()) {
            return NO_DATE;
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.US);
            format.setLenient(false);
            Date expiry = format.parse(date);

            // Today at midnight, so the time of day does not matter
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            long difference = expiry.getTime() - today.getTimeInMillis();
            return (int) Math.round(difference / (1000.0 * 60 * 60 * 24));
        } catch (ParseException e) {
            return NO_DATE;
        }
    }
}
