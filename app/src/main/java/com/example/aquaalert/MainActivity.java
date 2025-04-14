package com.example.aquaalert;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private EditText intervalEditText;
    private ProgressBar progressBar;
    private Handler handler;
    private Runnable progressRunnable;
    private long intervalMillis;
    private long startTime;
    private boolean isRunning = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intervalEditText = findViewById(R.id.reminder_time_input);
        progressBar = findViewById(R.id.progress_bar);
        handler = new Handler();
        Button setReminderButton = findViewById(R.id.set_reminder_button);
        Button cancelReminderButton = findViewById(R.id.cancel_reminder_button);

        checkPermissionForNotifications();
        requestBatteryUnrestricted(this);

        setReminderButton.setOnClickListener(view -> {
            int selectedInterval = getSelectedInterval();
            if (selectedInterval > 0) {
                createNotificationChannel();
                setRepeatingNotification(selectedInterval);
                intervalMillis = selectedInterval * 60 * 1000L;
            } else {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
            }
        });

        cancelReminderButton.setOnClickListener(view -> stopNotifications());
    }

    private int getSelectedInterval() {
        String input = intervalEditText.getText().toString().trim();
        if (!input.isEmpty()) {
            try {
                int interval = Integer.parseInt(input);
                return Math.max(interval, 0);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    public void checkPermissionForNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        1);
            }
        }
    }

    public void requestBatteryUnrestricted(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager == null) return;

        String packageName = context.getPackageName();
        @SuppressLint("BatteryLife") Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        intent.setData(Uri.parse("package:" + packageName));

        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "The device does not support this settings", Toast.LENGTH_SHORT).show();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "channel_id",
                    "AquaAlert Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void setRepeatingNotification(int intervalMinutes) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);

        intent.putExtra("intervalMinutes", intervalMinutes);

        int requestCode = 1;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intentScheduleAlarms = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intentScheduleAlarms);
                return;
            }
        }

        if (alarmManager != null) {
            NotificationReceiver.setStartTime(this, System.currentTimeMillis());

            long triggerTime = System.currentTimeMillis() + (intervalMinutes * 60 * 1000L);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            startProgressLoop();
            Toast.makeText(this, "Reminder set for every " + intervalMinutes + (intervalMinutes == 1 ? " minute" : " minutes"), Toast.LENGTH_SHORT).show();
        }
    }

    private void startProgressLoop() {
        if (isRunning) return;
        isRunning = true;

        progressRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsedTime = System.currentTimeMillis() - startTime;
                int progress = (int) ((elapsedTime * 100) / intervalMillis);
                progressBar.setProgress(progress);

                if (elapsedTime >= intervalMillis) {
                    startTime = System.currentTimeMillis();
                    progressBar.setProgress(0);
                }

                handler.postDelayed(this, 1000);
            }
        };

        handler.post(progressRunnable);
    }

    private void stopNotifications() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }

        pendingIntent.cancel();
        handler.removeCallbacks(progressRunnable);
        progressBar.setProgress(0);
        isRunning = false;
        startTime = 0;
        Toast.makeText(this, "Reminder stopped", Toast.LENGTH_SHORT).show();
    }
}