package com.vertigo.lunasreminder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.os.Build;

import com.vertigo.lunasreminder.DailyDBManager;
import com.vertigo.lunasreminder.DatabaseHelper;
import com.vertigo.lunasreminder.RepeatDBManager;
import com.vertigo.lunasreminder.SingleDBManager;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ReminderJobService extends JobService {
    Cursor cursor;
    private SingleDBManager singleDbManager;
    private RepeatDBManager repeatDbManager;
    private DailyDBManager dailyDbManager;
    private static final String TAG = "SyncService";
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-M-d", Locale.ENGLISH);
    final String[] daysOfWeek = new String[] {
            "Sunday",
            "Monday",
            "Tuesday",
            "Wednesday",
            "Thursday",
            "Friday",
            "Saturday"
    };

    @Override
    public boolean onStartJob(JobParameters params) {
        Intent intents = new Intent(getApplicationContext(), MainActivity.class);
        singleDbManager = new SingleDBManager(this);
        repeatDbManager = new RepeatDBManager(this);
        dailyDbManager = new DailyDBManager(this);
        String message = "Still to do today:\n";
        dailyDbManager.open();
        cursor = dailyDbManager.fetch();
        if (cursor.moveToFirst()){
            do{
                String date = cursor.getString(cursor.getColumnIndex(DatabaseHelper.DAILY_COMPLETED));
                if (daysSince(date) > 0) {
                    message = message + cursor.getString(cursor.getColumnIndex(DatabaseHelper.DAILY_NAME)) + "\n";
                }
            }while(cursor.moveToNext());
        }
        cursor.close();
        repeatDbManager.open();
        cursor = repeatDbManager.fetch();
        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(cursor.getColumnIndex(DatabaseHelper.REPEAT_DATE));
                String type = cursor.getString(cursor.getColumnIndex(DatabaseHelper.REPEAT_TYPE));
                String completed = cursor.getString(cursor.getColumnIndex(DatabaseHelper.REPEAT_COMPLETED));
                int intervalSize = 1;
                if (today().equals(completed)) {
                    continue;
                }
                if (type.equals("Monthly")) {
                    String[] completedSplit = completed.split("-");
                    YearMonth yearMonthObject = YearMonth.of(
                            Integer.parseInt(completedSplit[0]),
                            Integer.parseInt(completedSplit[1]));
                    intervalSize = yearMonthObject.lengthOfMonth()-1;
                    if ((daysBetween(completed, today()) > intervalSize) | (date.split("-")[2].equals(today().split("-")[2]))) {
                        message = message + cursor.getString(cursor.getColumnIndex(DatabaseHelper.REPEAT_NAME)) + "\n";
                    }
                } else if (type.equals("Weekly")) {cursor.getString(cursor.getColumnIndex(DatabaseHelper.REPEAT_NAME));
                    intervalSize = 6;
                    if ((daysBetween(completed, today()) > intervalSize) | (getDayOfWeek(today()).equals(getDayOfWeek(date)))) {
                        message = message + cursor.getString(cursor.getColumnIndex(DatabaseHelper.REPEAT_NAME)) + "\n";
                    }
                }
            }while(cursor.moveToNext());
        }
        cursor.close();
        singleDbManager.open();
        cursor = singleDbManager.fetch();
        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(cursor.getColumnIndex(DatabaseHelper.SINGLE_DATE));
                Boolean completed = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.SINGLE_COMPLETED)) > 0;
                if (!isFuture(date)) {
                    if (completed == Boolean.FALSE) {
                        message = message + cursor.getString(cursor.getColumnIndex(DatabaseHelper.SINGLE_NAME)) + "\n";
                    }
                }
            }while(cursor.moveToNext());
        }
        cursor.close();
        showNotification(this, "Todo Reminder",
                message.trim(),
                intents,
                1
        );
        Util.scheduleJob(getApplicationContext());
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
    /**
     *
     * @param context
     * @param title  --> title to show
     * @param message --> details to show
     * @param intent --> What should happen on clicking the notification
     * @param reqCode --> unique code for the notification
     */

    public void showNotification (Context context, String title, String message, Intent intent,
                                  int reqCode){
        PendingIntent pendingIntent = PendingIntent.getActivity(context, reqCode, intent, PendingIntent.FLAG_ONE_SHOT);
        String CHANNEL_ID = "channel_name";// The id of the channel.
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel Name";// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            notificationManager.createNotificationChannel(mChannel);
        }
        notificationManager.notify(reqCode, notificationBuilder.build()); // 0 is the request code, it should be unique id
    }

    public int daysSince(String day) {
        long date;
        try {
            date = dateFormat.parse(day).getTime()/1000/60/60/24;
        } catch (Exception e) {
            return 0;
        }
        long today = new Date().getTime()/1000/60/60/24;
        return (int)(today - date);
    }

    public int daysBetween(String day1, String day2) {
        try {
            long dayStamp1 = dateFormat.parse(day1).getTime() / (24 * 60 * 60 * 1000);
            long dayStamp2 = dateFormat.parse(day2).getTime() / (24 * 60 * 60 * 1000);
            return (int)(dayStamp2 - dayStamp1);
        } catch (Exception e) {
            return -1;
        }
    }

    private String today() {
        return dateFormat.format(new Date());
    }

    private String getDayOfWeek(String date) {
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(dateFormat.parse(date));
        } catch (Exception e) {
            return "Send string in form yyyy-M-d";
        }
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        return daysOfWeek[dayOfWeek-1];
    }

    public boolean isFuture(String date) {
        date = date.replace("-", "");
        SimpleDateFormat df = new SimpleDateFormat("yyyyMd");
        String today = df.format(new Date());
        if (Integer.parseInt(date) - Integer.parseInt(today) > 0) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }
}
