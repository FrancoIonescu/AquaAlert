package com.example.aquaalert;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private Spinner intervalSpinner;
    private Button setReminderButton;
    private Button cancelReminderButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intervalSpinner = findViewById(R.id.reminder_time_spinner);
        setReminderButton = findViewById(R.id.set_reminder_button);
        cancelReminderButton = findViewById(R.id.cancel_reminder_button);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        1);
            }
        }

        requestBatteryUnrestricted(this);

        setReminderButton.setOnClickListener(view -> {
            int selectedInterval = getSelectedInterval();
            setRepeatingNotification(selectedInterval);
        });

        cancelReminderButton.setOnClickListener(view -> stopNotifications());

    }

    private int getSelectedInterval() {
        String selectedItem = intervalSpinner.getSelectedItem().toString();
        switch (selectedItem) {
            case "1 minut":
                return 1;
            case "5 minute":
                return 5;
            case "10 minute":
                return 10;
            case "15 minute":
                return 15;
            case "30 minute":
                return 30;
            case "45 minute":
                return 45;
            case "60 minute":
                return 60;
            default:
                return 2;
        }
    }

    public void requestBatteryUnrestricted(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager == null) return;

        String packageName = context.getPackageName();

        Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        intent.setData(Uri.parse("package:" + packageName));

        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "Dispozitivul nu suportă această setare!", Toast.LENGTH_SHORT).show();
        }
    }

    private void setRepeatingNotification(int intervalMinutes) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);

        intent.putExtra("intervalMinutes", intervalMinutes);

        int requestCode = 1;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intentNou = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intentNou);
                return;
            }
        }

        if (alarmManager != null) {
            long triggerTime = System.currentTimeMillis() + (intervalMinutes * 60 * 1000L);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            Toast.makeText(this, "Reminder set for every " + intervalMinutes + " minutes", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopNotifications() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }

        pendingIntent.cancel();
    }
}
