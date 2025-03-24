package com.example.easysplit;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "easysplit.db";
    private static final int DATABASE_VERSION = 1;

    // Názvy tabulek
    public static final String TABLE_USER_SETTINGS = "user_settings";
    public static final String TABLE_PERMANENT_MEMBERS = "permanent_members";

    // Sloupce pro tabulku user_settings (budeme mít pouze jeden řádek s id = 1)
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USER_NAME = "user_name";
    public static final String COLUMN_APP_COLOR = "app_color";

    // Sloupce pro tabulku permanent_members
    public static final String COLUMN_MEMBER_ID = "id";
    public static final String COLUMN_MEMBER_NAME = "member_name";
    public static final String COLUMN_MULTIPLIER = "multiplier";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Vytvoření tabulek při prvním spuštění databáze
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tabulka pro nastavení uživatele – vždy jen jeden řádek (id = 1)
        String CREATE_USER_SETTINGS_TABLE = "CREATE TABLE " + TABLE_USER_SETTINGS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_USER_NAME + " TEXT,"
                + COLUMN_APP_COLOR + " TEXT"
                + ")";
        db.execSQL(CREATE_USER_SETTINGS_TABLE);

        // Tabulka pro permanentní členy
        String CREATE_PERMANENT_MEMBERS_TABLE = "CREATE TABLE " + TABLE_PERMANENT_MEMBERS + "("
                + COLUMN_MEMBER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_MEMBER_NAME + " TEXT,"
                + COLUMN_MULTIPLIER + " INTEGER"
                + ")";
        db.execSQL(CREATE_PERMANENT_MEMBERS_TABLE);
    }

    // Metoda volaná při změně verze databáze
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_SETTINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PERMANENT_MEMBERS);
        onCreate(db);
    }

    // Uloží nebo aktualizuje nastavení uživatele (vloží nebo aktualizuje řádek s id = 1)
    public void saveUserSettings(String userName, String appColor) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, userName);
        values.put(COLUMN_APP_COLOR, appColor);

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USER_SETTINGS + " WHERE " + COLUMN_USER_ID + "=1", null);
        if (cursor != null && cursor.moveToFirst()) {
            db.update(TABLE_USER_SETTINGS, values, COLUMN_USER_ID + "=?", new String[]{"1"});
        } else {
            values.put(COLUMN_USER_ID, 1);
            db.insert(TABLE_USER_SETTINGS, null, values);
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    // Načte uložené uživatelské jméno
    public String getUserName() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_USER_NAME + " FROM " + TABLE_USER_SETTINGS + " WHERE " + COLUMN_USER_ID + "=1", null);
        String userName = "";
        if (cursor != null && cursor.moveToFirst()) {
            userName = cursor.getString(0);
            cursor.close();
        }
        return userName;
    }

    // Načte uloženou barvu aplikace
    public String getAppColor() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_APP_COLOR + " FROM " + TABLE_USER_SETTINGS + " WHERE " + COLUMN_USER_ID + "=1", null);
        String appColor = "";
        if (cursor != null && cursor.moveToFirst()) {
            appColor = cursor.getString(0);
            cursor.close();
        }
        return appColor;
    }

    // Vloží nového permanentního člena do databáze
    public void insertPermanentMember(String memberName, int multiplier) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MEMBER_NAME, memberName);
        values.put(COLUMN_MULTIPLIER, multiplier);
        db.insert(TABLE_PERMANENT_MEMBERS, null, values);
    }

    // Načte seznam permanentních členů z databáze
    public ArrayList<PermanentMember> getPermanentMembers() {
        ArrayList<PermanentMember> members = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_MEMBER_NAME + ", " + COLUMN_MULTIPLIER + " FROM " + TABLE_PERMANENT_MEMBERS, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(0);
                int multiplier = cursor.getInt(1);
                members.add(new PermanentMember(name, multiplier));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return members;
    }

    // Jednoduchá vnitřní třída pro reprezentaci permanentního člena
    public static class PermanentMember {
        public String name;
        public int multiplier;

        public PermanentMember(String name, int multiplier) {
            this.name = name;
            this.multiplier = multiplier;
        }
    }
}
