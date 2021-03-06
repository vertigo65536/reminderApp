package com.vertigo.lunasreminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class RepeatDBManager {

    private DatabaseHelper dbHelper;

    private Context context;

    private SQLiteDatabase database;

    public RepeatDBManager(Context c) {
        context = c;
    }

    public RepeatDBManager open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public void insert(String name, String desc, String type, String date, String completed) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelper.REPEAT_NAME, name);
        contentValue.put(DatabaseHelper.REPEAT_DESC, desc);
        contentValue.put(DatabaseHelper.REPEAT_DATE, date);
        contentValue.put(DatabaseHelper.REPEAT_TYPE, type);
        contentValue.put(DatabaseHelper.REPEAT_COMPLETED, completed);
        database.insert(DatabaseHelper.REPEAT_TABLE_NAME, null, contentValue);
    }

    public Cursor fetch() {
        String[] columns = new String[] { DatabaseHelper.REPEAT__ID, DatabaseHelper.REPEAT_NAME,
                DatabaseHelper.REPEAT_DESC, DatabaseHelper.REPEAT_DATE, DatabaseHelper.REPEAT_TYPE,
                DatabaseHelper.REPEAT_COMPLETED };
        Cursor cursor = database.query(
                DatabaseHelper.REPEAT_TABLE_NAME,
                columns,
                null,
                null,
                null,
                null,
                DatabaseHelper.REPEAT_COMPLETED+" ASC");
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public int update(long _id, String name, String desc, String completed) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.REPEAT_NAME, name);
        contentValues.put(DatabaseHelper.REPEAT_DESC, desc);
        contentValues.put(DatabaseHelper.REPEAT_COMPLETED, completed);
        int i = database.update(DatabaseHelper.REPEAT_TABLE_NAME, contentValues, DatabaseHelper.REPEAT__ID + " = " + _id, null);
        return i;
    }

    public int updateCompleted(long _id,  String completed) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.REPEAT_COMPLETED, completed);
        int i = database.update(DatabaseHelper.REPEAT_TABLE_NAME, contentValues, DatabaseHelper.REPEAT__ID + " = " + _id, null);
        return i;
    }

    public void delete(long _id) {
        database.delete(DatabaseHelper.REPEAT_TABLE_NAME, DatabaseHelper.REPEAT__ID + "=" + _id, null);
    }

}
