package com.vertigo.lunasreminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartupReceiver extends BroadcastReceiver {
    public static final String TAG_NOTIFICATION = "NOTIFICATION_MESSAGE";
    public static final String CHANNEL_ID = "channel_1111";
    public static final int NOTIFICATION_ID = 111111;
    private static final String TAG = "Receiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Util.scheduleJob(context);
        //showNotification(context, "Test", "Message", intent, 1);
        //Intent i = new Intent(context.getApplicationContext(), ReminderService.class);
        //context.getApplicationContext().startService(i);
        /*if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
        }*/
    }
}