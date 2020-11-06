package com.example.lunasreminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Table Name
    public static final String DAILY_TABLE_NAME = "DAILY";
    public static final String REPEAT_TABLE_NAME = "REPEAT";
    public static final String SINGLE_TABLE_NAME = "SINGLE";
    public static final String SETTINGS_TABLE_NAME = "SETTINGS";

    // Daily Table columns
    public static final String DAILY__ID = "_id";
    public static final String DAILY_NAME = "name";
    public static final String DAILY_DESC = "description";
    public static final String DAILY_COMPLETED = "lastCompleted";

    // Repeat Table columns
    public static final String REPEAT__ID = "_id";
    public static final String REPEAT_NAME = "name";
    public static final String REPEAT_DESC = "description";
    public static final String REPEAT_TYPE = "type";
    public static final String REPEAT_DATE = "date";
    public static final String REPEAT_COMPLETED = "completed";

    // Single Table columns
    public static final String SINGLE__ID = "_id";
    public static final String SINGLE_NAME = "name";
    public static final String SINGLE_DESC = "description";
    public static final String SINGLE_DATE = "date";
    public static final String SINGLE_COMPLETED = "completed";

    // Settings Table columns
    public static final String SETTINGS__ID = "_id";
    public static final String SETTINGS_NAME = "name";
    public static final String SETTINGS_VALUE = "value";
    public static final String SETTINGS_TYPE = "type";
    public static final String SETTINGS_DESC = "description";

    // Database Information
    static final String DB_NAME = "JOURNALDEV_REMINDERS.DB";

    // database version
    static final int DB_VERSION = 1;

    // Creating daily table query
    private static final String DAILY_CREATE_TABLE = "create table " + DAILY_TABLE_NAME + "(" + DAILY__ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, " + DAILY_NAME + " TEXT NOT NULL, " + DAILY_DESC + " TEXT, "
            + DAILY_COMPLETED + " DATE);";

    // Creating daily table query
    private static final String REPEAT_CREATE_TABLE = "create table " + REPEAT_TABLE_NAME + "(" + REPEAT__ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, " + REPEAT_NAME + " TEXT NOT NULL, " + REPEAT_DESC + " TEXT,"
            + REPEAT_DATE + " DATE NOT NULL," + REPEAT_TYPE + " TEXT NOT NULL," + REPEAT_COMPLETED + " DATE);";

    // Creating single table query
    private static final String SINGLE_CREATE_TABLE = "create table " + SINGLE_TABLE_NAME + "(" + SINGLE__ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, " + SINGLE_NAME + " TEXT NOT NULL, " + SINGLE_DESC + " TEXT"
            + ", " + SINGLE_DATE + " DATE, " + SINGLE_COMPLETED + " BIT);";

    // Creating settings table query
    private static final String SETTINGS_CREATE_TABLE = "create table " + SETTINGS_TABLE_NAME + "(" + SETTINGS__ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, " + SETTINGS_NAME + " TEXT NOT NULL, " + SETTINGS_VALUE + " TEXT NOT NULL," +
            SETTINGS_TYPE + " TEXT NOT NULL," + SETTINGS_DESC + " TEXT NOT NULL);";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DAILY_CREATE_TABLE);
        db.execSQL(REPEAT_CREATE_TABLE);
        db.execSQL(SINGLE_CREATE_TABLE);
        db.execSQL(SETTINGS_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DAILY_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + REPEAT_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SINGLE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SETTINGS_TABLE_NAME);
        onCreate(db);
    }
}
