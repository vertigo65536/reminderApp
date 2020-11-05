package com.example.lunasreminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class SingleDBManager {

    private DatabaseHelper dbHelper;

    private Context context;

    private SQLiteDatabase database;

    public SingleDBManager(Context c) {
        context = c;
    }

    public SingleDBManager open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public void insert(String name, String desc, String date, Boolean completed) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper.SINGLE_NAME, name);
        contentValue.put(DatabaseHelper.SINGLE_DESC, desc);
        contentValue.put(DatabaseHelper.SINGLE_DATE, date);
        contentValue.put(DatabaseHelper.SINGLE_COMPLETED, completed);
        database.insert(DatabaseHelper.SINGLE_TABLE_NAME, null, contentValue);
    }

    public Cursor fetch() {
        String[] columns = new String[] { DatabaseHelper.SINGLE__ID, DatabaseHelper.SINGLE_NAME,
                DatabaseHelper.SINGLE_DESC, DatabaseHelper.SINGLE_DATE, DatabaseHelper.SINGLE_COMPLETED };
        Cursor cursor = database.query(DatabaseHelper.SINGLE_TABLE_NAME, columns, null, null, null, null, DatabaseHelper.SINGLE_DATE+" ASC");
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public int update(long _id, String name, String desc, String date, Boolean completed) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.SINGLE_NAME, name);
        contentValues.put(DatabaseHelper.SINGLE_DESC, desc);
        contentValues.put(DatabaseHelper.SINGLE_DATE, date);
        contentValues.put(DatabaseHelper.SINGLE_COMPLETED, completed);
        int i = database.update(DatabaseHelper.SINGLE_TABLE_NAME, contentValues, DatabaseHelper.SINGLE__ID + " = " + _id, null);
        return i;
    }

    public int updateCompleted(long _id,  Boolean completed) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.SINGLE_COMPLETED, completed);
        int i = database.update(DatabaseHelper.SINGLE_TABLE_NAME, contentValues, DatabaseHelper.SINGLE__ID + " = " + _id, null);
        return i;
    }

    public void delete(long _id) {
        database.delete(DatabaseHelper.SINGLE_TABLE_NAME, DatabaseHelper.SINGLE__ID + "=" + _id, null);
    }

}
