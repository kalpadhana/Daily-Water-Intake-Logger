package com.example.water_logger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.mindrot.jbcrypt.BCrypt;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "WaterApp.db";
    private static final int DATABASE_VERSION = 14;

    // Users table
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_TARGET_ML = "target_ml";

    // Water records table
    private static final String TABLE_WATER_RECORDS = "water_records";
    private static final String COLUMN_RECORD_ID = "id";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_RECORD_DATE = "date";
    private static final String COLUMN_RECORD_TARGET = "target";

    // Daily Summary table
    private static final String TABLE_DAILY_SUMMARY = "daily_summary";
    private static final String COLUMN_SUMMARY_ID = "id";
    private static final String COLUMN_SUMMARY_USER_ID = "user_id";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_IS_COMPLETED = "is_completed";
    private static final String COLUMN_SUMMARY_TOTAL = "total";
    private static final String COLUMN_SUMMARY_REMAINING = "remaining";

    // Reminders table
    private static final String TABLE_REMINDERS = "reminders";
    private static final String COLUMN_REMINDER_ID = "id";
    private static final String COLUMN_REMINDER_USER_ID = "user_id";
    private static final String COLUMN_REMINDER_NAME = "name";
    private static final String COLUMN_REMINDER_TIME = "time";
    private static final String COLUMN_REMINDER_IS_ENABLED = "is_enabled";

    private static final SimpleDateFormat YMD =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

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
                + COLUMN_PASSWORD + " TEXT, "
                + COLUMN_TARGET_ML + " INTEGER DEFAULT 2000" + ")";
        db.execSQL(CREATE_USERS_TABLE);

        String CREATE_WATER_RECORDS_TABLE = "CREATE TABLE " + TABLE_WATER_RECORDS + " ("
                + COLUMN_RECORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USER_ID + " INTEGER, "
                + COLUMN_TIMESTAMP + " INTEGER, "
                + COLUMN_AMOUNT + " INTEGER, "
                + COLUMN_RECORD_DATE + " TEXT, "
                + COLUMN_RECORD_TARGET + " INTEGER DEFAULT 2000)";
        db.execSQL(CREATE_WATER_RECORDS_TABLE);

        String CREATE_DAILY_SUMMARY_TABLE = "CREATE TABLE " + TABLE_DAILY_SUMMARY + " ("
                + COLUMN_SUMMARY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_SUMMARY_USER_ID + " INTEGER, "
                + COLUMN_DATE + " TEXT, "
                + COLUMN_SUMMARY_TOTAL + " INTEGER DEFAULT 0, "
                + COLUMN_SUMMARY_REMAINING + " INTEGER DEFAULT 0, "
                + COLUMN_IS_COMPLETED + " INTEGER DEFAULT 0, "
                + "UNIQUE(" + COLUMN_SUMMARY_USER_ID + ", " + COLUMN_DATE + "))";
        db.execSQL(CREATE_DAILY_SUMMARY_TABLE);

        createRemindersTable(db);

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_water_records_user_date ON " + TABLE_WATER_RECORDS + "(" + COLUMN_USER_ID + ", " + COLUMN_RECORD_DATE + ")");
    }

    private void createRemindersTable(SQLiteDatabase db) {
        String CREATE_REMINDERS_TABLE = "CREATE TABLE " + TABLE_REMINDERS + " ("
                + COLUMN_REMINDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_REMINDER_USER_ID + " INTEGER, "
                + COLUMN_REMINDER_NAME + " TEXT, "
                + COLUMN_REMINDER_TIME + " TEXT, "
                + COLUMN_REMINDER_IS_ENABLED + " INTEGER DEFAULT 1, "
                + "UNIQUE(" + COLUMN_REMINDER_USER_ID + ", " + COLUMN_REMINDER_NAME + "))";
        db.execSQL(CREATE_REMINDERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 14) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_REMINDERS);
            createRemindersTable(db);
        }
    }

    // ---------- Users ----------
    public boolean insertUser(String name, String email, String phone, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PHONE, phone);
        
        // Hash the password before storing it
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        values.put(COLUMN_PASSWORD, hashedPassword);
        
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public int getUserId(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_ID, COLUMN_PASSWORD},
                COLUMN_EMAIL + "=?",
                new String[]{email},
                null, null, null);

        int userId = -1;
        if (cursor.moveToFirst()) {
            String storedHashedPassword = cursor.getString(1);
            // Verify the entered password against the stored hash
            if (BCrypt.checkpw(password, storedHashedPassword)) {
                userId = cursor.getInt(0);
            }
        }
        cursor.close();
        return userId;
    }

    public Cursor getUserDetails(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT " + COLUMN_NAME + ", " + COLUMN_EMAIL + " FROM " + TABLE_USERS + " WHERE " + COLUMN_ID + "=?", new String[]{String.valueOf(userId)});
    }

    public int getUserTarget(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_TARGET_ML + " FROM " + TABLE_USERS + " WHERE " + COLUMN_ID + "=?", new String[]{String.valueOf(userId)});
        int target = 2000;
        if (cursor.moveToFirst()) {
            try {
                int val = cursor.isNull(0) ? 0 : cursor.getInt(0);
                if (val > 0) target = val;
            } catch (Exception ignored) {
            }
        }
        cursor.close();
        return target;
    }

    public void setUserTarget(int userId, int targetMl) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TARGET_ML, targetMl);
        db.update(TABLE_USERS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(userId)});

        String today = getTodayDateString();
        int totalToday = getTotalForDate(userId, today);
        int remaining = Math.max(0, targetMl - totalToday);
        int completed = (totalToday >= targetMl) ? 1 : 0;

        ContentValues summaryValues = new ContentValues();
        summaryValues.put(COLUMN_SUMMARY_USER_ID, userId);
        summaryValues.put(COLUMN_DATE, today);
        summaryValues.put(COLUMN_SUMMARY_TOTAL, totalToday);
        summaryValues.put(COLUMN_SUMMARY_REMAINING, remaining);
        summaryValues.put(COLUMN_IS_COMPLETED, completed);

        db.insertWithOnConflict(TABLE_DAILY_SUMMARY, null, summaryValues, SQLiteDatabase.CONFLICT_REPLACE);
    }

    // ---------- Reminders ----------
    public void saveUserReminders(int userId, List<Reminder> reminders) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            for (Reminder reminder : reminders) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_REMINDER_USER_ID, userId);
                values.put(COLUMN_REMINDER_NAME, reminder.getName());
                values.put(COLUMN_REMINDER_TIME, reminder.getTime());
                values.put(COLUMN_REMINDER_IS_ENABLED, reminder.isEnabled() ? 1 : 0);
                
                db.insertWithOnConflict(TABLE_REMINDERS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public List<Reminder> getUserReminders(int userId) {
        List<Reminder> reminders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_REMINDERS, 
                new String[]{COLUMN_REMINDER_NAME, COLUMN_REMINDER_TIME, COLUMN_REMINDER_IS_ENABLED},
                COLUMN_REMINDER_USER_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(0);
                String time = cursor.getString(1);
                boolean isEnabled = cursor.getInt(2) == 1;
                reminders.add(new Reminder(name, time, isEnabled));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return reminders;
    }

    // ---------- Water Records ----------
    public void addWaterRecord(int userId, int amount) {
        if (isDayCompleted(userId)) return;

        SQLiteDatabase db = this.getWritableDatabase();
        long ts = System.currentTimeMillis();
        String dateStr = YMD.format(new Date(ts));

        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_TIMESTAMP, ts);
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_RECORD_DATE, dateStr);
        int userTarget = getUserTarget(userId);
        values.put(COLUMN_RECORD_TARGET, userTarget);
        db.insert(TABLE_WATER_RECORDS, null, values);
    }

    public int getTodayTotalIntake(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String today = getTodayDateString();

        Cursor cursor = db.rawQuery(
                "SELECT COALESCE(SUM(" + COLUMN_AMOUNT + "), 0) " +
                        "FROM " + TABLE_WATER_RECORDS + " WHERE " + COLUMN_RECORD_DATE + " = ? AND " + COLUMN_USER_ID + " = ?",
                new String[]{today, String.valueOf(userId)}
        );

        int total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }
        cursor.close();
        return total;
    }

    public int getTotalForDate(int userId, String yyyyMmDd) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COALESCE(SUM(" + COLUMN_AMOUNT + "), 0) " +
                        "FROM " + TABLE_WATER_RECORDS + " WHERE " + COLUMN_RECORD_DATE + " = ? AND " + COLUMN_USER_ID + " = ?",
                new String[]{yyyyMmDd, String.valueOf(userId)}
        );
        int total = 0;
        if (cursor.moveToFirst()) total = cursor.getInt(0);
        cursor.close();
        return total;
    }

    public int[] getHourlyIntakeForDay(int userId, long dateMillis) {
        int[] hourlyIntake = new int[24];
        SQLiteDatabase db = this.getReadableDatabase();
        String dayStr = YMD.format(new Date(dateMillis));

        Cursor cursor = db.query(
                TABLE_WATER_RECORDS,
                new String[]{COLUMN_TIMESTAMP, COLUMN_AMOUNT},
                COLUMN_RECORD_DATE + " = ? AND " + COLUMN_USER_ID + " = ?",
                new String[]{dayStr, String.valueOf(userId)},
                null, null, null
        );

        Calendar cal = Calendar.getInstance();
        if (cursor.moveToFirst()) {
            do {
                long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP));
                int amount = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT));
                cal.setTimeInMillis(timestamp);
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                if (hour >= 0 && hour < 24) {
                    hourlyIntake[hour] += amount;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return hourlyIntake;
    }

    public int[] getDailyIntakeForWeek(int userId, long dateMillis) {
        int[] dailyIntake = new int[7];
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dateMillis);
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        for (int i = 0; i < 7; i++) {
            String dayStr = YMD.format(cal.getTime());
            dailyIntake[i] = getTotalForDate(userId, dayStr);
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        return dailyIntake;
    }

    // ---------- Daily Summary ----------
    private String getTodayDateString() {
        return YMD.format(new Date());
    }

    public void markDayAsCompleted(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String today = getTodayDateString();

        ContentValues values = new ContentValues();
        values.put(COLUMN_SUMMARY_USER_ID, userId);
        values.put(COLUMN_DATE, today);
        values.put(COLUMN_IS_COMPLETED, 1);

        int totalIntake = getTodayTotalIntake(userId);
        values.put(COLUMN_SUMMARY_TOTAL, totalIntake);

        int target = getUserTarget(userId);
        int remaining = Math.max(0, target - totalIntake);
        values.put(COLUMN_SUMMARY_REMAINING, remaining);

        db.insertWithOnConflict(TABLE_DAILY_SUMMARY, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public boolean isDayCompleted(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String today = getTodayDateString();

        Cursor cursor = db.rawQuery(
                "SELECT " + COLUMN_IS_COMPLETED +
                        " FROM " + TABLE_DAILY_SUMMARY +
                        " WHERE " + COLUMN_DATE + " = ? AND " + COLUMN_SUMMARY_USER_ID + " = ?",
                new String[]{today, String.valueOf(userId)}
        );

        boolean completed = false;
        if (cursor.moveToFirst()) {
            completed = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_COMPLETED)) == 1;
        }
        cursor.close();
        return completed;
    }

    public void ensureDailySummaryForDate(int userId, String yyyyMmDd) {
        if (summaryExistsForDate(userId, yyyyMmDd)) return;

        int total = getTotalForDate(userId, yyyyMmDd);
        int target = getUserTarget(userId);
        int remaining = Math.max(0, target - total);
        int completed = total >= target ? 1 : 0;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SUMMARY_USER_ID, userId);
        values.put(COLUMN_DATE, yyyyMmDd);
        values.put(COLUMN_SUMMARY_TOTAL, total);
        values.put(COLUMN_SUMMARY_REMAINING, remaining);
        values.put(COLUMN_IS_COMPLETED, completed);

        db.insertWithOnConflict(TABLE_DAILY_SUMMARY, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public boolean summaryExistsForDate(int userId, String yyyyMmDd) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + TABLE_DAILY_SUMMARY + " WHERE " + COLUMN_SUMMARY_USER_ID + " = ? AND " + COLUMN_DATE + " = ? LIMIT 1",
                new String[]{String.valueOf(userId), yyyyMmDd}
        );
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public void archiveYesterdayIfMissing(int userId) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        String yesterday = YMD.format(cal.getTime());
        ensureDailySummaryForDate(userId, yesterday);
    }
}