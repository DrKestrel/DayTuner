package com.netherbench.daytuner;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import static com.netherbench.daytuner.R.xml.preferences;

/**
 * Created by USER on 6/17/2016.
 */

public class SettingsActivity extends PreferenceActivity {

    public static float sleep_duration;
    public static boolean use_percents;
    public static boolean allow_notifications;
    public static SwitchPreference use_percents_switcher;
    public static SwitchPreference allow_notifications_switcher;
    public static EditTextPreference sleep_duration_editor;
    public static EditTextPreference notification_time_preference;
    public static final String SHARED_PREFERENCES = "PreferencesFile";
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(preferences);
        allow_notifications_switcher = (SwitchPreference) findPreference("allow_notifications");
        allow_notifications_switcher.setOnPreferenceClickListener(onPreferenceClickListener);
        if(allow_notifications_switcher.isChecked())
            CreateAppList();
        else
        DeleteAppList();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onPause() {
        super.onPause();
        use_percents_switcher = (SwitchPreference) findPreference("use_percents_preference");
        sleep_duration_editor = (EditTextPreference) findPreference("sleep_duration_preference");
        use_percents = use_percents_switcher.isChecked();
        sleep_duration = Float.parseFloat(sleep_duration_editor.getText());
        allow_notifications_switcher = (SwitchPreference) findPreference("allow_notifications");
        allow_notifications = allow_notifications_switcher.isChecked();
        notification_time_preference = (EditTextPreference) findPreference("notification_time_preference");
        SharedPreferences settings = getSharedPreferences(SHARED_PREFERENCES, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat("notification_time_preference", Float.parseFloat(notification_time_preference.getText()));
        editor.putBoolean("allow_notifications_boolean", allow_notifications);
        editor.putBoolean("use_percents_boolean", use_percents);
        editor.putFloat("sleep_duration_float", Float.parseFloat(sleep_duration_editor.getText()));
        if(allow_notifications_switcher.isChecked()){
        for (int i = 0; i <= (settings.getInt("NumberOfApps", 1)); i++) {
            if (settings.getString("appname" + Integer.toString(i), null) != null) {
                SwitchPreference appswitcher = (SwitchPreference) findPreference(settings.getString("appname" + Integer.toString(i), null));
                editor.putBoolean("appnameboolean" + Integer.toString(i), appswitcher.isChecked());
            }
        }}

        editor.commit();
        Log.d("Debug", "Settings Activity paused");
    }
    Preference.OnPreferenceClickListener onPreferenceClickListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            allow_notifications_switcher = (SwitchPreference) findPreference("allow_notifications");
            if(allow_notifications_switcher.isChecked())
                CreateAppList();
            else
                DeleteAppList();
            return false;
        }
    };
    public void CreateAppList(){
        SharedPreferences settings = getSharedPreferences(SHARED_PREFERENCES, 0);
        PreferenceCategory NotificationCategory = (PreferenceCategory) findPreference("Notifications");
        //Log.d("Debug", Integer.toString(settings.getInt("NumberOfApps", 999)));
        for (int i = 0; i <= (settings.getInt("NumberOfApps", 1)); i++) {
            if (settings.getString("appname" + Integer.toString(i), null) != null) {
                SwitchPreference switchPreference = new SwitchPreference(this);
                switchPreference.setTitle(settings.getString("appname" + Integer.toString(i), null));
                switchPreference.setChecked(true);
                switchPreference.setKey(settings.getString("appname" + Integer.toString(i), null));
                NotificationCategory.addPreference(switchPreference);
            }
        }
    }
    public void DeleteAppList(){
        SharedPreferences settings = getSharedPreferences(SHARED_PREFERENCES, 0);
        PreferenceCategory NotificationCategory = (PreferenceCategory) findPreference("Notifications");
        try{
        for (int i = 0; i <= (settings.getInt("NumberOfApps", 1)); i++) {
            if (settings.getString("appname" + Integer.toString(i), null) != null) {
                SwitchPreference appswitcher = (SwitchPreference) findPreference(settings.getString("appname" + Integer.toString(i), null));
                NotificationCategory.removePreference(appswitcher);
            }
        }}catch (Throwable t){

        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Debug", "Settings Activity finished");
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Settings Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.netherbench.daytuner/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Settings Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.netherbench.daytuner/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
