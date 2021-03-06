package com.vertigo.lunasreminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class SettingsDBManager {

    private static final String TAG = "lunasreminder.SettingsDBManager";

    private DatabaseHelper dbHelper;

    private Context context;

    private SQLiteDatabase database;

    public SettingsDBManager(Context c) {
        context = c;
    }

    public SettingsDBManager open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        populateSettings();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public void insert(String name, String value, int id, String type, String desc) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper.SETTINGS__ID, id);
        contentValue.put(DatabaseHelper.SETTINGS_NAME, name);
        contentValue.put(DatabaseHelper.SETTINGS_VALUE, value);
        contentValue.put(DatabaseHelper.SETTINGS_TYPE, type);
        contentValue.put(DatabaseHelper.SETTINGS_DESC, desc);
        database.insert(DatabaseHelper.SETTINGS_TABLE_NAME, null, contentValue);
    }

    public Cursor fetch() {
        String[] columns = new String[] { DatabaseHelper.SETTINGS__ID, DatabaseHelper.SETTINGS_NAME,
                DatabaseHelper.SETTINGS_VALUE, DatabaseHelper.SETTINGS_TYPE, DatabaseHelper.SETTINGS_DESC};
        Cursor cursor = database.query(DatabaseHelper.SETTINGS_TABLE_NAME, columns, null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public int updateValue(long _id,  String value) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.SETTINGS_VALUE, value);
        int i = database.update(DatabaseHelper.SETTINGS_TABLE_NAME, contentValues, DatabaseHelper.SETTINGS__ID + " = " + _id, null);
        return i;
    }

    public void delete(long _id) {
        database.delete(DatabaseHelper.SETTINGS_TABLE_NAME, DatabaseHelper.SETTINGS__ID + "=" + _id, null);
    }

    public String getValue(int id) {
        Cursor cursor = database.query(
                DatabaseHelper.SETTINGS_TABLE_NAME,
                null,
                DatabaseHelper.SETTINGS__ID + " = " + id,
                null,
                null,
                null,
                null
        );
        cursor.moveToFirst();
        return cursor.getString(cursor.getColumnIndex(DatabaseHelper.SETTINGS_VALUE));

    }

    public Boolean getNotifications() { return Boolean.parseBoolean(getValue(0)); }

    public long getInterval() {
        return Long.parseLong(getValue(1));
    }

    public String getStartTime() {
        return getValue(2);
    }

    public String getEndTime() {
        return getValue(3);
    }

    public void populateSettings() {
        try {
            insert("Turn Notifications On", "false", 0, "bool",
                    "Turn on reminder notifications which fire on a given interval");
            insert("Interval", "3600000", 1, "timer",
                    "Interval between notifications. Given in minutes");
            insert("Active Start Time", "10:00", 2, "time",
                    "Time beginning period of the notification fire time");
            insert("Active End Time", "23:00", 3, "time",
                    "The end period of the notification fire time");
        } catch (Exception e) {
            Log.d(TAG, "Attempted to populate SETTINGS table, but it was already full");
        }
    }

}
