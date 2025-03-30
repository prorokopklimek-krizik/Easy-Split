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

    // Tabulka pro nastavení (např. barva aplikace)
    public static final String TABLE_USER_SETTINGS = "user_settings";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_APP_COLOR = "app_color";

    // Tabulka pro skupiny
    public static final String TABLE_GROUPS = "groups_table";
    public static final String COLUMN_GROUP_ID = "group_id";
    public static final String COLUMN_GROUP_NAME = "group_name";
    public static final String COLUMN_GROUP_TOTAL = "group_total";

    // Nová tabulka pro členy jednotlivých skupin
    public static final String TABLE_GROUP_MEMBERS = "group_members";
    public static final String COLUMN_GM_ID = "gm_id";
    public static final String COLUMN_GM_GROUP_ID = "group_id";
    public static final String COLUMN_MEMBER_NAME = "member_name";
    public static final String COLUMN_MULTIPLIER = "multiplier";

    // Nová tabulka pro utraty
    public static final String TABLE_EXPENSES = "expenses";
    public static final String COLUMN_EXPENSE_ID = "expense_id";
    public static final String COLUMN_EXPENSE_MEMBER_ID = "member_id"; // odkazuje na gm_id v group_members
    public static final String COLUMN_EXPENSE_NAME = "expense_name";
    public static final String COLUMN_EXPENSE_AMOUNT = "expense_amount";
    public static final String COLUMN_EXPENSE_DESCRIPTION = "expense_description";
    public static final String COLUMN_EXPENSE_PHOTO = "expense_photo";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Vytvoření všech tabulek
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tabulka pro nastavení
        String CREATE_USER_SETTINGS_TABLE = "CREATE TABLE " + TABLE_USER_SETTINGS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_APP_COLOR + " TEXT"
                + ")";
        db.execSQL(CREATE_USER_SETTINGS_TABLE);

        // Tabulka pro skupiny
        String CREATE_GROUPS_TABLE = "CREATE TABLE " + TABLE_GROUPS + "("
                + COLUMN_GROUP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_GROUP_NAME + " TEXT,"
                + COLUMN_GROUP_TOTAL + " REAL DEFAULT 0"
                + ")";
        db.execSQL(CREATE_GROUPS_TABLE);

        // Tabulka pro členy skupin
        String CREATE_GROUP_MEMBERS_TABLE = "CREATE TABLE " + TABLE_GROUP_MEMBERS + "("
                + COLUMN_GM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_GM_GROUP_ID + " INTEGER,"
                + COLUMN_MEMBER_NAME + " TEXT,"
                + COLUMN_MULTIPLIER + " INTEGER"
                + ")";
        db.execSQL(CREATE_GROUP_MEMBERS_TABLE);

        // Tabulka pro utraty
        String CREATE_EXPENSES_TABLE = "CREATE TABLE " + TABLE_EXPENSES + "("
                + COLUMN_EXPENSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_EXPENSE_MEMBER_ID + " INTEGER,"  // odkazuje na gm_id
                + COLUMN_EXPENSE_NAME + " TEXT,"
                + COLUMN_EXPENSE_AMOUNT + " REAL,"
                + COLUMN_EXPENSE_DESCRIPTION + " TEXT,"
                + COLUMN_EXPENSE_PHOTO + " TEXT"
                + ")";
        db.execSQL(CREATE_EXPENSES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_SETTINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUP_MEMBERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        onCreate(db);
    }

    // --- Metody pro nastavení (např. barvu aplikace) ---
    public void saveUserSettings(String appColor, String selectedColor) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_APP_COLOR, appColor);

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USER_SETTINGS + " WHERE " + COLUMN_USER_ID + "=1", null);
        if (cursor != null && cursor.moveToFirst()) {
            db.update(TABLE_USER_SETTINGS, values, COLUMN_USER_ID + "=?", new String[]{"1"});
        } else {
            values.put(COLUMN_USER_ID, 1);
            db.insert(TABLE_USER_SETTINGS, null, values);
        }
        if (cursor != null) cursor.close();
    }

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

    // --- Metody pro skupiny ---
    public long insertGroup(String groupName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GROUP_NAME, groupName);
        values.put(COLUMN_GROUP_TOTAL, 0);
        return db.insert(TABLE_GROUPS, null, values);
    }

    public ArrayList<Group> getGroups() {
        ArrayList<Group> groups = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_GROUP_ID + ", " + COLUMN_GROUP_NAME + ", " + COLUMN_GROUP_TOTAL +
                " FROM " + TABLE_GROUPS, null);
        if (cursor != null && cursor.moveToFirst()){
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                double total = cursor.getDouble(2);
                groups.add(new Group(id, name, total));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return groups;
    }

    public static class Group {
        public int id;
        public String name;
        public double total;
        public Group(int id, String name, double total) {
            this.id = id;
            this.name = name;
            this.total = total;
        }
    }

    // --- Metody pro členy skupiny ---
    public long insertGroupMember(int groupId, String memberName, int multiplier) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GM_GROUP_ID, groupId);
        values.put(COLUMN_MEMBER_NAME, memberName);
        values.put(COLUMN_MULTIPLIER, multiplier);
        return db.insert(TABLE_GROUP_MEMBERS, null, values);
    }

    public void updateGroupMember(int gmId, String memberName, int multiplier) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MEMBER_NAME, memberName);
        values.put(COLUMN_MULTIPLIER, multiplier);
        db.update(TABLE_GROUP_MEMBERS, values, COLUMN_GM_ID + "=?", new String[]{String.valueOf(gmId)});
    }

    public void deleteGroupMember(int gmId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_GROUP_MEMBERS, COLUMN_GM_ID + "=?", new String[]{String.valueOf(gmId)});
    }

    public ArrayList<GroupMember> getGroupMembers(int groupId) {
        ArrayList<GroupMember> members = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_GM_ID + ", " + COLUMN_MEMBER_NAME + ", " + COLUMN_MULTIPLIER +
                " FROM " + TABLE_GROUP_MEMBERS + " WHERE " + COLUMN_GM_GROUP_ID + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(groupId)});
        if (cursor != null && cursor.moveToFirst()){
            do {
                int gmId = cursor.getInt(0);
                String name = cursor.getString(1);
                int multiplier = cursor.getInt(2);
                members.add(new GroupMember(gmId, groupId, name, multiplier));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return members;
    }

    public static class GroupMember {
        public int gmId;
        public int groupId;
        public String name;
        public int multiplier;
        public GroupMember(int gmId, int groupId, String name, int multiplier) {
            this.gmId = gmId;
            this.groupId = groupId;
            this.name = name;
            this.multiplier = multiplier;
        }
    }

    // --- Metody pro utraty ---
    public long insertExpense(int memberId, String expenseName, double expenseAmount, String expenseDescription, String expensePhoto) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EXPENSE_MEMBER_ID, memberId);
        values.put(COLUMN_EXPENSE_NAME, expenseName);
        values.put(COLUMN_EXPENSE_AMOUNT, expenseAmount);
        values.put(COLUMN_EXPENSE_DESCRIPTION, expenseDescription);
        values.put(COLUMN_EXPENSE_PHOTO, expensePhoto);
        return db.insert(TABLE_EXPENSES, null, values);
    }

    public void updateExpense(int expenseId, String expenseName, double expenseAmount, String expenseDescription, String expensePhoto) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EXPENSE_NAME, expenseName);
        values.put(COLUMN_EXPENSE_AMOUNT, expenseAmount);
        values.put(COLUMN_EXPENSE_DESCRIPTION, expenseDescription);
        values.put(COLUMN_EXPENSE_PHOTO, expensePhoto);
        db.update(TABLE_EXPENSES, values, COLUMN_EXPENSE_ID + "=?", new String[]{String.valueOf(expenseId)});
    }

    public void deleteExpense(int expenseId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EXPENSES, COLUMN_EXPENSE_ID + "=?", new String[]{String.valueOf(expenseId)});
    }

    public ArrayList<Expense> getExpensesForMember(int memberId) {
        ArrayList<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_EXPENSE_ID + ", " + COLUMN_EXPENSE_NAME + ", " + COLUMN_EXPENSE_AMOUNT + ", "
                + COLUMN_EXPENSE_DESCRIPTION + ", " + COLUMN_EXPENSE_PHOTO +
                " FROM " + TABLE_EXPENSES + " WHERE " + COLUMN_EXPENSE_MEMBER_ID + "=?", new String[]{String.valueOf(memberId)});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(1);
                double amount = cursor.getDouble(2);
                String description = cursor.getString(3);
                String photo = cursor.getString(4);
                expenses.add(new Expense(id, memberId, name, amount, description, photo));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return expenses;
    }

    public static class Expense {
        public int id;
        public int memberId;
        public String name;
        public double amount;
        public String description;
        public String photo;
        public Expense(int id, int memberId, String name, double amount, String description, String photo) {
            this.id = id;
            this.memberId = memberId;
            this.name = name;
            this.amount = amount;
            this.description = description;
            this.photo = photo;
        }
    }
}
