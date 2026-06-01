package com.example.login;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class FonosDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "fonos.db";
    private static final int DB_VERSION = 1;

    public FonosDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsers = "CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "fullname TEXT, " +
                "username TEXT UNIQUE, " +
                "email TEXT, " +
                "password TEXT)";

        String createContents = "CREATE TABLE contents (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, " +
                "author TEXT, " +
                "genre TEXT, " +
                "year INTEGER, " +
                "type TEXT, " +
                "topic TEXT, " +
                "duration INTEGER, " +
                "progress INTEGER, " +
                "favorite INTEGER, " +
                "downloaded INTEGER)";

        db.execSQL(createUsers);
        db.execSQL(createContents);
        insertDefaultContents(db);
    }

    private void insertDefaultContents(SQLiteDatabase db) {
        insertContent(db, "Nhà Giả Kim", "Paulo Coelho", "Tiểu thuyết", 2002, "Audiobook", "Life", 3600);
        insertContent(db, "Sapiens", "Yuval Noah Harari", "Lịch sử", 2011, "Book Summary", "History", 1800);
        insertContent(db, "Đắc Nhân Tâm", "Dale Carnegie", "Kỹ năng", 1936, "Audiobook", "Self-help", 4200);
        insertContent(db, "1984", "George Orwell", "Tiểu thuyết", 1949, "Audiobook", "Fiction", 3900);
        insertContent(db, "Podcast Sống Tích Cực", "Fonos Podcast", "Podcast", 2024, "Podcast", "Mindset", 1500);
        insertContent(db, "Thiền Buổi Sáng", "Fonos Meditation", "Thiền định", 2024, "Meditation", "Relax", 900);
    }

    private void insertContent(SQLiteDatabase db, String title, String author, String genre,
                               int year, String type, String topic, int duration) {
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("author", author);
        values.put("genre", genre);
        values.put("year", year);
        values.put("type", type);
        values.put("topic", topic);
        values.put("duration", duration);
        values.put("progress", 0);
        values.put("favorite", 0);
        values.put("downloaded", 0);
        db.insert("contents", null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS contents");
        onCreate(db);
    }

    public boolean registerUser(String fullname, String username, String email, String password) {
        SQLiteDatabase db = getWritableDatabase();

        if (isUsernameExists(username)) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put("fullname", fullname);
        values.put("username", username);
        values.put("email", email);
        values.put("password", password);

        long result = db.insert("users", null, values);
        return result != -1;
    }

    public boolean loginUser(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM users WHERE username = ? AND password = ?",
                new String[]{username, password}
        );

        boolean success = cursor.getCount() > 0;
        cursor.close();
        return success;
    }

    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM users WHERE username = ?",
                new String[]{username}
        );

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public List<Book> getAllContents() {
        List<Book> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM contents ORDER BY id DESC", null);

        while (cursor.moveToNext()) {
            Book book = new Book(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("title")),
                    cursor.getString(cursor.getColumnIndexOrThrow("author")),
                    cursor.getString(cursor.getColumnIndexOrThrow("genre")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("year")),
                    cursor.getString(cursor.getColumnIndexOrThrow("type")),
                    cursor.getString(cursor.getColumnIndexOrThrow("topic")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("duration")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("progress")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("favorite")) == 1,
                    cursor.getInt(cursor.getColumnIndexOrThrow("downloaded")) == 1
            );
            list.add(book);
        }

        cursor.close();
        return list;
    }

    public void addContent(Book book) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("title", book.getTitle());
        values.put("author", book.getAuthor());
        values.put("genre", book.getGenre());
        values.put("year", book.getYear());
        values.put("type", book.getType());
        values.put("topic", book.getTopic());
        values.put("duration", book.getDuration());
        values.put("progress", book.getProgress());
        values.put("favorite", book.isFavorite() ? 1 : 0);
        values.put("downloaded", book.isDownloaded() ? 1 : 0);

        db.insert("contents", null, values);
    }

    public void updateContent(Book book) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("title", book.getTitle());
        values.put("author", book.getAuthor());
        values.put("genre", book.getGenre());
        values.put("year", book.getYear());
        values.put("type", book.getType());
        values.put("topic", book.getTopic());
        values.put("duration", book.getDuration());
        values.put("progress", book.getProgress());
        values.put("favorite", book.isFavorite() ? 1 : 0);
        values.put("downloaded", book.isDownloaded() ? 1 : 0);

        db.update("contents", values, "id = ?", new String[]{String.valueOf(book.getId())});
    }

    public void deleteContent(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("contents", "id = ?", new String[]{String.valueOf(id)});
    }

    public void updateFavorite(int id, boolean favorite) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("favorite", favorite ? 1 : 0);

        db.update("contents", values, "id = ?", new String[]{String.valueOf(id)});
    }

    public void updateDownloaded(int id, boolean downloaded) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("downloaded", downloaded ? 1 : 0);

        db.update("contents", values, "id = ?", new String[]{String.valueOf(id)});
    }

    public void updateProgress(int id, int progress) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("progress", progress);

        db.update("contents", values, "id = ?", new String[]{String.valueOf(id)});
    }
}