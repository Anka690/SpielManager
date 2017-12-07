package com.apps.rb.spielmanager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.content.ContentValues;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;
/**
 * Created by Anka on 05.12.2017.
 */

public class Datenbank {
    private static final String LOG_TAG = Datenbank.class.getSimpleName();

    private SQLiteDatabase database;
    private DatenbankHelper dbHelper;

    private String[] columns = {
            DatenbankHelper.COLUMN_ID,
            DatenbankHelper.COLUMN_TITLE,
            DatenbankHelper.COLUMN_MIN_NUM_PLAYERS,
            DatenbankHelper.COLUMN_MAX_NUM_PLAYERS,
            DatenbankHelper.COLUMN_AVG_RATING,
            DatenbankHelper.COLUMN_YEAR,
            DatenbankHelper.COLUMN_DURATION,
            DatenbankHelper.COLUMN_FIRST_PLAY_DATE,
            DatenbankHelper.COLUMN_PLACE,
            DatenbankHelper.COLUMN_COVER
    };


    public Datenbank(Context context) {
        Log.d(LOG_TAG, "Unsere DataSource erzeugt jetzt den DatenbankHelper.");
        dbHelper = new DatenbankHelper(context);
    }

    public void open() {
        Log.d(LOG_TAG, "Eine Referenz auf die Datenbank wird jetzt angefragt.");
        database = dbHelper.getWritableDatabase();
        Log.d(LOG_TAG, "Datenbank-Referenz erhalten. Pfad zur Datenbank: " + database.getPath());
    }

    public void close() {
        dbHelper.close();
        Log.d(LOG_TAG, "Datenbank mit Hilfe des DbHelpers geschlossen.");
    }

    public Spiel createSpiel(String title) {
        ContentValues values = new ContentValues();
        values.put(DatenbankHelper.COLUMN_TITLE, title);

        long insertId = database.insert(DatenbankHelper.TABLE_SPIELE, null, values);

        Cursor cursor = database.query(DatenbankHelper.TABLE_SPIELE,
                columns, DatenbankHelper.COLUMN_ID + "=" + insertId,
                null, null, null, null);

        cursor.moveToFirst();
        Spiel game = cursorToSpiel(cursor);
        cursor.close();

        return game;
    }

    public void deleteGame(Spiel game) {
        long id = game.getId();

        database.delete(DatenbankHelper.TABLE_SPIELE,
                DatenbankHelper.COLUMN_ID + "=" + id,
                null);

        Log.d(LOG_TAG, "Eintrag gelöscht! ID: " + id + " Inhalt: " + game.toString());
    }

    public Spiel updateGame(long id, String newTitle) {
        ContentValues values = new ContentValues();
        values.put(DatenbankHelper.COLUMN_TITLE, newTitle);

        database.update(DatenbankHelper.TABLE_SPIELE,
                values,
                DatenbankHelper.COLUMN_ID + "=" + id,
                null);

        Cursor cursor = database.query(DatenbankHelper.TABLE_SPIELE,
                columns, DatenbankHelper.COLUMN_ID + "=" + id,
                null, null, null, null);

        cursor.moveToFirst();
        Spiel game = cursorToSpiel(cursor);
        cursor.close();

        return game;
    }

    private Spiel cursorToSpiel(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(DatenbankHelper.COLUMN_ID);
        int idTitle = cursor.getColumnIndex(DatenbankHelper.COLUMN_TITLE);

        String title = cursor.getString(idTitle);
        long id = cursor.getLong(idIndex);

        Spiel game = new Spiel(id, title);

        return game;
    }

    public List<Spiel> getAllSpiele() {
        List<Spiel> gameList = new ArrayList<>();

        Cursor cursor = database.query(DatenbankHelper.TABLE_SPIELE,
                columns, null, null, null, null, null);

        cursor.moveToFirst();
        Spiel game;

        while(!cursor.isAfterLast()) {
            game = cursorToSpiel(cursor);
            gameList.add(game);
            Log.d(LOG_TAG, "ID: " + game.getId() + ", Inhalt: " + game.toString());
            cursor.moveToNext();
        }

        cursor.close();

        return gameList;
    }
}
