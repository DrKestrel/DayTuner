package com.netherbench.daytuner;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static com.netherbench.daytuner.SettingsActivity.SHARED_PREFERENCES;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private GoogleApiClient client;
    public static float SummaryUsage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        fab.hide();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        //CheckUsageAccess();
        SharedPreferences settings = getSharedPreferences(SHARED_PREFERENCES, 0);
        if(settings.getBoolean("allow_notifications_boolean", true)){
            launchNotificationService();
            Log.d("Service", "Service launched");
        }
    }

    public void CreatePieChart(){
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
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<String>();
        appvalues = new float[queryUsageStats.size()];
        appnames = new String[queryUsageStats.size()];
        SharedPreferences settings = getSharedPreferences(SHARED_PREFERENCES, 0);
        SharedPreferences.Editor editor = settings.edit();
        SleepDuration = settings.getFloat("sleep_duration_float", 8);
        try {for (UsageStats us : queryUsageStats) {

            if (LongMS_To_FH(us.getTotalTimeInForeground()) >= 0.2f) {
                try {
                    ai = pm.getApplicationInfo(us.getPackageName(), 0);
                } catch (final PackageManager.NameNotFoundException e) {
                    ai = null;
                }
                if(!(us.getPackageName().contains("com.android")|| (us.getPackageName().contains("daytuner")) || (us.getPackageName().contains("googlequicksearchbox")) || (us.getPackageName().contains("deskclock")) || (us.getPackageName().contains("calculator")) || (us.getPackageName().contains("android.dialer")))){
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
            if (appnames[i]!=null) {
                entries.add(new Entry(appvalues[i], i));
                labels.add(appnames[i]);
                SummaryUsage = SummaryUsage + appvalues[i];
                editor.putString("appname"+Integer.toString(i), appnames[i]);
                editor.putFloat("appvalue"+Integer.toString(i), appvalues[i]);
            }
        }}
        catch (Throwable t){
            CheckUsageAccess();
        }
        StandByMode = 24-SummaryUsage - SleepDuration;
        entries.add(new Entry(StandByMode, k+1));
        labels.add("Standby mode");

        PieChart pieChart = (PieChart) findViewById(R.id.chart);
        if(settings.getBoolean("use_percents_boolean", false))
            pieChart.setUsePercentValues(true);
        else
            pieChart.setUsePercentValues(false);
        PieDataSet dataset = new PieDataSet(entries, "");
        PieData data = new PieData(labels, dataset);
        pieChart.setData(data);
        dataset.setColors(ColorTemplate.LIBERTY_COLORS);
        pieChart.animateY(5000);
        pieChart.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        pieChart.setCenterText("Daily Usage");
        pieChart.setCenterTextColor(getResources().getColor(R.color.colorPrimaryDark));
        pieChart.setDescription("");
        pieChart.setNoDataText("No data available. Restart the app and grant the necessary permissions.");
        pieChart.setFocusable(true);
        editor.putInt("NumberOfApps", k);
        editor.commit();
    }
    public void launchNotificationService() {
        // Construct our Intent specifying the Service

        Intent i = new Intent(this, RSSPullService.class);
        // Start the service
        startService(i);
    }

    public void CheckUsageAccess()
    {
        Toast.makeText(this, "Please make sure you grant the necessary permissions for the application.", Toast.LENGTH_LONG).show();
        try {
            PackageManager packageManager = this.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(this.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) this.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
            if (mode != AppOpsManager.MODE_ALLOWED)
                getpermission();

        } catch (PackageManager.NameNotFoundException e) {
        }
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.daily_usage) {

        } else if (id == R.id.reminder) {
            /*getWindow().setExitTransition(new Explode());
            Intent intent = new Intent(this,ReminderActivity.class);
            startActivity(intent,
                    ActivityOptions
                            .makeSceneTransitionAnimation(this).toBundle());*/
            //Toast.makeText(this, "This feature is coming in next updates", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, ReminderActivity.class);
            startActivity(intent);
        } else if (id == R.id.settings_nav_bar) {
            /*PieChart pieChart = (PieChart) findViewById(R.id.chart);
            pieChart.setVisibility(View.INVISIBLE);*/
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static float LongMS_To_FH(long l) {
        l = l / 100000;
        float f = (float) l;
        f = f/36;
        return f;
    }

    void getpermission() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onResume(){
        super.onResume();
        CreatePieChart();
        SharedPreferences settings = getSharedPreferences(SHARED_PREFERENCES, 0);
        if(settings.getBoolean("allow_notifications_boolean", true)){
                launchNotificationService();
                Log.d("Service", "Service launched");
        }
        else{
            launchNotificationService();
            ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();

            Iterator<ActivityManager.RunningAppProcessInfo> iter = runningAppProcesses.iterator();

            while(iter.hasNext()){
                ActivityManager.RunningAppProcessInfo next = iter.next();

                String pricessName = getPackageName() + ":service";

                if(next.processName.equals(pricessName)){
                    android.os.Process.killProcess(next.pid);
                    break;
                }
            }
            Log.d("Service", "Service was executed");
        }
        Log.d("Debug", "PieChart was created");
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());

        Log.d("Debug", "Activity restarted");
    }

    @Override
    public void onStop() {
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        Log.d("Debug", "Activity stopped");
        client.disconnect();
    }
    @Override
    public void onDestroy(){
        super.onDestroy();

    }
}
