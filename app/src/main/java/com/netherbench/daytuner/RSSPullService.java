package com.netherbench.daytuner;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import static com.netherbench.daytuner.SettingsActivity.SHARED_PREFERENCES;

/**
 * Created by USER on 05.07.2016.
 */
public class RSSPullService extends IntentService {

    public static float SummaryUsage;
    public static final String USAGE_INFO =  "UsageInfo";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent,flags,startId);
        SharedPreferences settings = getSharedPreferences(SHARED_PREFERENCES, 0);
        if(settings.getBoolean("allow_notifications_boolean", true)){
            Log.d("Service", "Service was started sticky");
        return START_STICKY;}
        else{
            Log.d("Service", "Service was started not sticky");
            return START_NOT_STICKY;}
    }
    public RSSPullService(){
        super("RSSPullService");
    }
    @Override
    protected void onHandleIntent(Intent intent){
        Log.d("Debug", "RSSPullService started");
        //SendNotification("--debugging notification 1--");
        while(true){
            try {
                Thread.sleep(120000);
                Log.d("Service", "Service running");
            } catch (InterruptedException e) {
                //Thread.currentThread().interrupt();
                Log.d("Service", "Service awake", e);
            }
            CheckForNotification();
            //SendNotification("--debugging notification 2--");
        }
    }
    public static float LongMS_To_FH(long l) {
        l = l / 100000;
        float f = (float) l;
        f = f/36;
        return f;
    }
    public void CheckForNotification(){
        SharedPreferences usage_info = getSharedPreferences(USAGE_INFO, 0);
        SharedPreferences settings = getSharedPreferences(SHARED_PREFERENCES, 0);
        SharedPreferences.Editor editor = usage_info.edit();
        float[] appvalues;
        String[] appnames;
        SummaryUsage = 0;
        int k=0;
        float StandByMode = 0;
        float SleepDuration;
        final Calendar begDate = Calendar.getInstance();
        begDate.set(Calendar.DATE, begDate.get(Calendar.DAY_OF_MONTH)-1);
        begDate.set(Calendar.MONTH, begDate.get(Calendar.MONTH));
        begDate.set(Calendar.YEAR, begDate.get(Calendar.YEAR));
        final Calendar endDate = Calendar.getInstance();
        endDate.set(Calendar.DATE, endDate.get(Calendar.DAY_OF_MONTH));
        endDate.set(Calendar.MONTH, endDate.get(Calendar.MONTH));
        endDate.set(Calendar.YEAR, endDate.get(Calendar.YEAR));
        final PackageManager pm = getApplicationContext().getPackageManager();
        ApplicationInfo ai;
        final UsageStatsManager usageStatsManager = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);// Context.USAGE_STATS_SERVICE);
        final List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, begDate.getTimeInMillis(), endDate.getTimeInMillis());
        appvalues = new float[queryUsageStats.size()];
        appnames = new String[queryUsageStats.size()];
        SleepDuration = settings.getFloat("sleep_duration_float", 8);
        for (UsageStats us : queryUsageStats) {

            if (LongMS_To_FH(us.getTotalTimeInForeground()) >= (settings.getFloat("notification_time_preference", 30) / 60)) {
                try {
                    ai = pm.getApplicationInfo(us.getPackageName(), 0);
                } catch (final PackageManager.NameNotFoundException e) {
                    ai = null;
                }
                if(!(us.getPackageName().contains("com.android") || (us.getPackageName().contains("googlequicksearchbox")) || (us.getPackageName().contains("deskclock")) || (us.getPackageName().contains("calculator")) || (us.getPackageName().contains("android.dialer")))){
                    final String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
                    appvalues[k] = LongMS_To_FH(us.getTotalTimeInForeground());
                    appnames[k] = applicationName;
                    //appnames[k] = us.getPackageName();
                    k++;
                }}
        }

        for (int j = 0; j <= k; j++) {
            for (int l = 0; l <= k; l++)
                if (Objects.equals(appnames[j], appnames[l]) && l!=j) {
                    appvalues[j] = appvalues[j] + appvalues[l];
                    appnames[l] = null;
                }
        }
        for (int i = 0; i <= k; i++) {
            if ((appnames[i]!=null) && settings.getBoolean("appnameboolean" + Integer.toString(i), false)) {
                SummaryUsage = SummaryUsage + appvalues[i];
                if(appvalues[i]<usage_info.getFloat(appnames[i], 25)){
                    editor.putFloat(appnames[i], appvalues[i]);
                }
                else if((appvalues[i]-usage_info.getFloat(appnames[i], 25)) >0.25f)
                {
                    editor.putFloat(appnames[i], appvalues[i]);
                    if(settings.getBoolean("allow_notifications_boolean", true))
                    SendNotification(appnames[i]);
                }
            }
        }
        StandByMode = 24-SummaryUsage - SleepDuration;

        editor.commit();
    }
    public void SendNotification(String appname){
        Log.d("Service", "Notification send by " + appname);
        NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_notification)
                        .setContentTitle("You waste your time.")
                        .setContentText("Stop wasting your time on " + appname + ", it's not worth it.")
                        .setVibrate(new long[] { 100, 100, 100, 100, 100 })
                        .setAutoCancel(true);
                        //.setSound(Uri.parse("uri://sadfasdfasdf.mp3"));
        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();
        String[] events = new String[2];
        events[0] = "Stop wasting your time on " + appname + ",";
        events[1] = "it's not worth it.";
// Sets a title for the Inbox in expanded layout
        inboxStyle.setBigContentTitle(("You waste your time."));
// Moves events into the expanded layout
        for (int i=0; i < events.length; i++) {

            inboxStyle.addLine(events[i]);
        }
// Moves the expanded layout object into the notification object.
        mBuilder.setStyle(inboxStyle);
// Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(1, mBuilder.build());
    }
   }
