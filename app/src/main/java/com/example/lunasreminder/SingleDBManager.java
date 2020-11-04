package com.example.lunasreminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class SingleDBManager {

    private SingleDatabaseHelper dbHelper;

    private Context context;

    private SQLiteDatabase database;

    public SingleDBManager(Context c) {
        context = c;
    }

    public SingleDBManager open() throws SQLException {
        dbHelper = new SingleDatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public void insert(String name, String desc, String date, Boolean completed) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(SingleDatabaseHelper.NAME, name);
        contentValue.put(SingleDatabaseHelper.DESC, desc);
        contentValue.put(SingleDatabaseHelper.DATE, date);
        contentValue.put(SingleDatabaseHelper.COMPLETED, completed);
        database.insert(SingleDatabaseHelper.TABLE_NAME, null, contentValue);
    }

    public Cursor fetch() {
        String[] columns = new String[] { SingleDatabaseHelper._ID, SingleDatabaseHelper.NAME,
                SingleDatabaseHelper.DESC, SingleDatabaseHelper.DATE, SingleDatabaseHelper.COMPLETED };
        Cursor cursor = database.query(SingleDatabaseHelper.TABLE_NAME, columns, null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public int update(long _id, String name, String desc, String date) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(SingleDatabaseHelper.NAME, name);
        contentValues.put(SingleDatabaseHelper.DESC, desc);
        contentValues.put(SingleDatabaseHelper.DATE, date);
        int i = database.update(SingleDatabaseHelper.TABLE_NAME, contentValues, SingleDatabaseHelper._ID + " = " + _id, null);
        return i;
    }

    public void delete(long _id) {
        database.delete(SingleDatabaseHelper.TABLE_NAME, SingleDatabaseHelper._ID + "=" + _id, null);
    }

}
