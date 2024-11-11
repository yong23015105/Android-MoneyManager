package com.example.moneymanager;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class RecordDatabaseHelper extends SQLiteOpenHelper {
    private static final String TABLE_USER = "User_table";
    private static final String COLUMN_ID = "User_ID";
    private static final String COLUMN_QUESTION = "Question";
    private static final String COLUMN_ANSWER = "Answer";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_SALT = "salt";
    private static final String COLUMN_ANSWER_SALT = "answer_salt";
    private static final String TAG = "DatabaseHelper";
    private static final String Database_name = "moneyManagerDatabase.db";
    private static final int Database_version = 7;
    public static final String Table_name = "TransactionTable";


    public RecordDatabaseHelper(Context context) {
        super(context, Database_name, null, Database_version);
    }

    private static final SecureRandom random = new SecureRandom();

    public static String generateSalt() {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.encodeToString(salt, Base64.NO_WRAP);
    }

    public static String hashPassword(String password, String salt) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(salt.getBytes());
        byte[] hashedPassword = digest.digest(password.getBytes());
        return Base64.encodeToString(hashedPassword, Base64.NO_WRAP);
    }

    public static boolean verifyPassAndAns(String inputPassword, String storedHash, String storedSalt) throws NoSuchAlgorithmException {
        String hashedInputPassword = hashPassword(inputPassword, storedSalt);
        return hashedInputPassword.equals(storedHash);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE IF NOT EXISTS " + Table_name + " (" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "User_ID INTEGER, " +
                "Name TEXT NOT NULL, " +
                "Type TEXT NOT NULL, " +
                "Amount REAL NOT NULL, " +
                "Date TEXT NOT NULL, " +
                "State TEXT NOT NULL," +
                "Description TEXT, " +
                "FOREIGN KEY(User_ID) REFERENCES User_table(User_ID) ON DELETE CASCADE" +
                ")";
        db.execSQL(createTable);

        String CREATE_USER_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_USER + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT NOT NULL, " +
                COLUMN_EMAIL + " TEXT NOT NULL, " +
                COLUMN_QUESTION + " TEXT NOT NULL, " +
                COLUMN_ANSWER + " TEXT NOT NULL, " +
                COLUMN_SALT + " TEXT NOT NULL, " +
                COLUMN_ANSWER_SALT + " TEXT NOT NULL, " +
                COLUMN_PASSWORD + " TEXT NOT NULL)";
        db.execSQL(CREATE_USER_TABLE);
        Log.d(TAG, "Table created: " + TABLE_USER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 7) {
            db.execSQL("DROP TABLE IF EXISTS " + Table_name);
            db.execSQL("DROP TABLE IF EXISTS user_accounts");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
            onCreate(db);
        }
    }

    public boolean checkUserExists(String username, String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USER + " WHERE " + COLUMN_USERNAME + "=? OR " + COLUMN_EMAIL + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{username, email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public boolean insertUser(String username, String email, String password, String Question, String Answer) {
        SQLiteDatabase db = this.getWritableDatabase();
        try{
            String salt = generateSalt();
            String ans_salt = generateSalt();

            String hashedPassword = hashPassword(password, salt);
            String hashedAnswer = hashPassword(Answer, ans_salt);

            ContentValues values = new ContentValues();
            values.put(COLUMN_USERNAME, username);
            values.put(COLUMN_EMAIL, email);
            values.put(COLUMN_PASSWORD, hashedPassword);
            values.put(COLUMN_SALT, salt);
            values.put(COLUMN_ANSWER_SALT, ans_salt);
            values.put(COLUMN_QUESTION, Question);
            values.put(COLUMN_ANSWER, hashedAnswer);
            long result = db.insert(TABLE_USER, null, values);
            return result != -1;

        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }finally{
            db.close();
        }
        return false;
    }

    @SuppressLint("Range")
    public String checkUserCredentials(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String User_ID = null;
        String query = "SELECT * FROM " + TABLE_USER + " WHERE " + COLUMN_USERNAME + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{username});

        try{
            if (cursor != null && cursor.moveToFirst()) {
                String user_salt = cursor.getString(cursor.getColumnIndex(COLUMN_SALT));
                String user_hashPass = cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD));
                if(verifyPassAndAns(password, user_hashPass, user_salt)){
                    User_ID = cursor.getString(cursor.getColumnIndex(COLUMN_ID));
                }
            }
            db.close();
            return User_ID;
        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
            User_ID = null;
        }finally{
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return User_ID;
    }

    @SuppressLint("Range")
    public String checkUserExistsByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USER + " WHERE " + COLUMN_EMAIL + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{email});
        String question = null;

        if (cursor != null && cursor.moveToFirst()) {
            question = cursor.getString(cursor.getColumnIndex(COLUMN_QUESTION));
        }

        if (cursor != null){
            cursor.close();
        }

        return question;
    }

    @SuppressLint("Range")
    public boolean checkUserAnswer(String email,String Answer) {
        if(Answer == null){
            return false;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        String query = " SELECT * FROM " + TABLE_USER + " WHERE " + COLUMN_EMAIL + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{email});

        try{
            if (cursor != null && cursor.moveToFirst()) {
                String True_Answer = cursor.getString(cursor.getColumnIndex(COLUMN_ANSWER));
                String answerSalt = cursor.getString(cursor.getColumnIndex(COLUMN_ANSWER_SALT));

                if(verifyPassAndAns(Answer, True_Answer, answerSalt)){
                    return true;
                }
            }
        }catch(NoSuchAlgorithmException e ){
            e.printStackTrace();
            return false;
        }finally{
            if (cursor != null){
                cursor.close();
            }
            db.close();
        }
        return false;
    }

    public boolean updatePassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        boolean updateSuccess = false;

        try{
            String new_salt = generateSalt();

            String hashedPassword = hashPassword(newPassword, new_salt);

            values.put(COLUMN_PASSWORD, hashedPassword);
            values.put(COLUMN_SALT, new_salt);
            int rowsAffected = db.update(TABLE_USER, values, COLUMN_EMAIL + "=?", new String[]{email});
            db.close();
            return rowsAffected > 0;
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
            updateSuccess = false;
        }finally{
            db.close();
        }
        return updateSuccess;
    }

    @SuppressLint("Range")
    public Cursor getAllData(String User_ID) {
        SQLiteDatabase db = this.getReadableDatabase();
        Calendar calendar = Calendar.getInstance();
        long currentDateMillis = calendar.getTimeInMillis();
        int userIdInt = Integer.parseInt(User_ID);

        Cursor cursor = db.rawQuery(" SELECT * FROM " + Table_name + " Where User_ID = ? ", new String[]{String.valueOf(userIdInt)});
        String currentDateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(currentDateMillis));

        try {
            if (cursor.moveToFirst()) {
                do {
                    String itemDateStr = cursor.getString(cursor.getColumnIndex("Date"));

                    Date itemDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(itemDateStr);
                    Date currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(currentDateStr);

                    String state;

                    if (itemDate.after(currentDate)) {
                        state = "Not Achieved";
                    } else {
                        state = "Achieved";
                    }

                    String updateQuery = "UPDATE " + Table_name + " SET State = ? WHERE ID = ?";
                    db.execSQL(updateQuery, new Object[]{state, cursor.getLong(cursor.getColumnIndex("ID"))});

                } while (cursor.moveToNext());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
        return db.rawQuery(" SELECT * FROM " + Table_name + " Where User_ID = ? ", new String[]{String.valueOf(userIdInt)});
    }

    @SuppressLint("Range")
    public String findUsername(int User_ID){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT username FROM " + TABLE_USER + " WHERE " + COLUMN_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(User_ID)});
        String username = null;

        if (cursor != null && cursor.moveToFirst()) {
            username = cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME));
        }
        else{
            username = "no name";
        }

        if (cursor != null) {
            cursor.close();
        }

        return username;
    }

    public boolean AddTransaction(int userId, String name, String type, double amount, String date, String description) {
        // get a writable database by using function getWritableDatabase from SqLiteOpenHelper
        SQLiteDatabase db = this.getWritableDatabase();
        //create content value for storing record
        ContentValues Transaction_Record = new ContentValues();
        Transaction_Record.put("User_ID", userId);
        Transaction_Record.put("Name", name);
        Transaction_Record.put("Type", type);
        Transaction_Record.put("Amount", amount);
        Transaction_Record.put("Date", date);
        Transaction_Record.put("State", "Pending");
        Transaction_Record.put("Description", description);

        //insert record into database with content value
        long result = db.insert(Table_name, null, Transaction_Record);
        db.close();
        Log.d("Database", "Insert result: " + result);
        return result != -1;
    }

    public void deleteTransaction(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Table_name, "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public boolean updateTransaction(long id, String name, String type, double amount, String date, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("name", name);
        values.put("type", type);
        values.put("amount", amount);
        values.put("date", date);
        values.put("description", description);

        int result = db.update(Table_name, values, "id = ?", new String[]{String.valueOf(id)});
        db.close();
        return result > 0;
    }

}