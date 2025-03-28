package com.example.aquaalert;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String PREFS_NAME = "AquaAlertPrefs";
    private static final String KEY_START_TIME = "startTime";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        int intervalMinutes = intent.getIntExtra("intervalMinutes", 1);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channel_id")
                .setSmallIcon(R.drawable.water_drop_favicon)
                .setContentTitle("AquaAlert")
                .setContentText("Time for a water break?! 💧")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, builder.build());

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intentSettings = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                context.startActivity(intentSettings);
                return;
            }
        }

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long startTime = prefs.getLong(KEY_START_TIME, System.currentTimeMillis());
        long currentTime = System.currentTimeMillis();
        long elapsedMinutes = (currentTime - startTime) / (60 * 1000);
        long nextTriggerMinutes = ((elapsedMinutes / intervalMinutes) + 1) * intervalMinutes;
        long nextTriggerTime = startTime + (nextTriggerMinutes * 60 * 1000);

        Intent newIntent = new Intent(context, NotificationReceiver.class);
        newIntent.putExtra("intervalMinutes", intervalMinutes);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, newIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTriggerTime, pendingIntent);
        }
    }

    public static void setStartTime(Context context, long startTime) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putLong(KEY_START_TIME, startTime).apply();
    }
}