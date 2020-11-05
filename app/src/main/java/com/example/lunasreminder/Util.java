package com.example.lunasreminder;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import java.util.Date;

public class Util {
    public static long scheduleJob(Context context) {
        SettingsDBManager settingsDbManager = new SettingsDBManager(context);
        settingsDbManager.open();
        String startTime = settingsDbManager.getStartTime();
        String endTime = settingsDbManager.getEndTime();
        Long interval = settingsDbManager.getInterval();
        settingsDbManager.close();
        ComponentName serviceComponent = new ComponentName(context, ReminderJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        Date now = new Date();long untilNext = getInterval(startTime, interval);
        if (!checkInActiveTime(startTime, endTime)) {
            untilNext = getNextTimestamp(startTime) - now.getTime();
        }
        builder.setMinimumLatency(untilNext); // wait at least
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.schedule(builder.build());
        return now.getTime() + untilNext;
    }

    public static void cancelJobs(Context context) {
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.cancelAll();
    }

    private static long getInterval(String startTime, long interval) {
        String startTimeSplit[] = startTime.split(":");
        int startHours = Integer.parseInt(startTimeSplit[0]);
        int startMinutes = Integer.parseInt(startTimeSplit[1]);
        Date now = new Date();
        long nextAlarmTime = new Date(now.getYear(), now.getMonth(), now.getDate(), startHours, startMinutes).getTime();
        while (nextAlarmTime < now.getTime()) {
            nextAlarmTime += interval;
        }
        return nextAlarmTime - now.getTime();
    }

    private static long getNextTimestamp(String time) {
        String timeSplit[] = time.split(":");
        int hours = Integer.parseInt(timeSplit[0]);
        int minutes = Integer.parseInt(timeSplit[1]);
        Date now = new Date();
        Date timeToday = new Date(now.getYear(), now.getMonth(), now.getDate(), hours, minutes);
        long timeStamp = timeToday.getTime();
        if (now.after(timeToday)) {
            timeStamp += 24*60*60*1000;
        }
        return timeStamp;
    }

    private static long getLastTimestamp(String time) {
        String timeSplit[] = time.split(":");
        int hours = Integer.parseInt(timeSplit[0]);
        int minutes = Integer.parseInt(timeSplit[1]);
        Date now = new Date();
        Date timeToday = new Date(now.getYear(), now.getMonth(), now.getDate(), hours, minutes);
        long timeStamp = timeToday.getTime();
        if (now.before(timeToday)) {
            timeStamp -= 24*60*60*1000;
        }

        return timeStamp;
    }

    private static Boolean checkInActiveTime(String startTime, String endTime) {
        long lastStart = getLastTimestamp(startTime);
        long lastEnd = getLastTimestamp(endTime);
        if (lastEnd > lastStart) {
            return Boolean.FALSE;
        } else {
            return Boolean.TRUE;
        }
    }

    public static long setToNextStart(Context context) {
        SettingsDBManager settingsDbManager = new SettingsDBManager(context);
        settingsDbManager.open();
        cancelJobs(context);
        String startTime = settingsDbManager.getStartTime();
        Long interval = settingsDbManager.getInterval();
        Date now = new Date();long untilNext = getInterval(startTime, interval);
        ComponentName serviceComponent = new ComponentName(context, ReminderJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        builder.setMinimumLatency(getNextTimestamp(startTime) - now.getTime()); // wait at least
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.schedule(builder.build());
        settingsDbManager.close();
        return getNextTimestamp(startTime);
    }
}
