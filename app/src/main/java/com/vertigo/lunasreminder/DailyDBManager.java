package com.vertigo.lunasreminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DailyDBManager {

    private DatabaseHelper dbHelper;

    private Context context;

    private SQLiteDatabase database;

    public DailyDBManager(Context c) {
        context = c;
    }

    public DailyDBManager open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public void insert(String name, String desc, String completed) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper.DAILY_NAME, name);
        contentValue.put(DatabaseHelper.DAILY_DESC, desc);
        contentValue.put(DatabaseHelper.DAILY_COMPLETED, completed);
        database.insert(DatabaseHelper.DAILY_TABLE_NAME, null, contentValue);
    }

    public Cursor fetch() {
        String[] columns = new String[] { DatabaseHelper.DAILY__ID, DatabaseHelper.DAILY_NAME,
                DatabaseHelper.DAILY_DESC, DatabaseHelper.DAILY_COMPLETED };
        Cursor cursor = database.query(DatabaseHelper.DAILY_TABLE_NAME, columns, null, null, null, null, DatabaseHelper.DAILY_COMPLETED+" ASC");
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public int update(long _id, String name, String desc, String completed) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.DAILY_NAME, name);
        contentValues.put(DatabaseHelper.DAILY_DESC, desc);
        contentValues.put(DatabaseHelper.DAILY_COMPLETED, completed);
        int i = database.update(DatabaseHelper.DAILY_TABLE_NAME, contentValues, DatabaseHelper.DAILY__ID + " = " + _id, null);
        return i;
    }

    public int updateCompleted(long _id,  String completed) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.DAILY_COMPLETED, completed);
        int i = database.update(DatabaseHelper.DAILY_TABLE_NAME, contentValues, DatabaseHelper.DAILY__ID + " = " + _id, null);
        return i;
    }

    public void delete(long _id) {
        database.delete(DatabaseHelper.DAILY_TABLE_NAME, DatabaseHelper.DAILY__ID + "=" + _id, null);
    }

}
