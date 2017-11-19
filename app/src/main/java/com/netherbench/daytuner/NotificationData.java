package com.netherbench.daytuner;

import android.content.SharedPreferences;
import android.text.format.Time;

/**
 * Created by USER on 6/26/2016.
 */

class NotificationData{
    static String notification_text;
    String notification_time;
    boolean notification_active;


    NotificationData(String notification_text, String notification_time, boolean notification_active) {
        this.notification_text = notification_text;
        this.notification_time = notification_time;
        this.notification_active = notification_active;
}
}
