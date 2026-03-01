package com.example.water_logger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Calendar;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "WaterApp.db";
    private static final int DATABASE_VERSION = 4; // Incremented to trigger onUpgrade

    // Users table
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_PASSWORD = "password";

    // Water records table
    private static final String TABLE_WATER_RECORDS = "water_records";
    private static final String COLUMN_RECORD_ID = "id";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String COLUMN_AMOUNT = "amount";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " ("        
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT, "
                + COLUMN_EMAIL + " TEXT UNIQUE, "
                + COLUMN_PHONE + " TEXT, "
                + COLUMN_PASSWORD + " TEXT)";
        db.execSQL(CREATE_USERS_TABLE);

        String CREATE_WATER_RECORDS_TABLE = "CREATE TABLE " + TABLE_WATER_RECORDS + " ("
                + COLUMN_RECORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TIMESTAMP + " INTEGER, "
                + COLUMN_AMOUNT + " INTEGER)";
        db.execSQL(CREATE_WATER_RECORDS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS water_intake"); // Drop old table
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WATER_RECORDS);
        onCreate(db);
    }

    public boolean insertUser(String name, String email, String phone, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PHONE, phone);
        values.put(COLUMN_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS +
                        " WHERE " + COLUMN_EMAIL + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{email, password}
        );
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public void addWaterRecord(int amount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TIMESTAMP, System.currentTimeMillis());
        values.put(COLUMN_AMOUNT, amount);
        db.insert(TABLE_WATER_RECORDS, null, values);
    }

    public int[] getHourlyIntakeForDay(long dateMillis) {
        int[] hourlyIntake = new int[24];
        SQLiteDatabase db = this.getReadableDatabase();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateMillis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startTime = calendar.getTimeInMillis();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        long endTime = calendar.getTimeInMillis();

        Cursor cursor = db.query(TABLE_WATER_RECORDS,
                new String[]{COLUMN_TIMESTAMP, COLUMN_AMOUNT},
                COLUMN_TIMESTAMP + " >= ? AND " + COLUMN_TIMESTAMP + " < ?",
                new String[]{String.valueOf(startTime), String.valueOf(endTime)},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP));
                int amount = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT));
                calendar.setTimeInMillis(timestamp);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                hourlyIntake[hour] += amount;
            } while (cursor.moveToNext());
        }
        cursor.close();
        return hourlyIntake;
    }

    public int[] getDailyIntakeForWeek(long dateMillis) {
        int[] dailyIntake = new int[7];
        SQLiteDatabase db = this.getReadableDatabase();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateMillis);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        for (int i = 0; i < 7; i++) {
            long startTime = calendar.getTimeInMillis();
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            long endTime = calendar.getTimeInMillis();

            Cursor cursor = db.query(TABLE_WATER_RECORDS,
                    new String[]{"SUM(" + COLUMN_AMOUNT + ")"},
                    COLUMN_TIMESTAMP + " >= ? AND " + COLUMN_TIMESTAMP + " < ?",
                    new String[]{String.valueOf(startTime), String.valueOf(endTime)},
                    null, null, null);

            if (cursor.moveToFirst()) {
                dailyIntake[i] = cursor.getInt(0);
            }
            cursor.close();
        }
        return dailyIntake;
    }
}