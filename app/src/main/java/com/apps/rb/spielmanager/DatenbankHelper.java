package com.apps.rb.spielmanager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Anka on 05.12.2017.
 */

public class DatenbankHelper extends SQLiteOpenHelper{

    private static final String LOG_TAG = DatenbankHelper.class.getSimpleName();


    public static final String DB_NAME = "spiele_list.db";
    public static final int DB_VERSION = 2;

    public static final String TABLE_SPIELE = "spiele_list";

    public static final String COLUMN_SPIEL_ID = "_id";
    public static final String COLUMN_TITLE = "titel";
    public static final String COLUMN_MIN_NUM_PLAYERS = "minSpieler";
    public static final String COLUMN_MAX_NUM_PLAYERS = "maxSpieler";
    public static final String COLUMN_AVG_RATING = "avgRating";
    public static final String COLUMN_YEAR = "Jahr";
    public static final String COLUMN_DURATION = "Dauer";
    public static final String COLUMN_FIRST_PLAY_DATE = "ErstSpielDatum";
    public static final String COLUMN_OWNER = "Owner";
    public static final String COLUMN_PLACE = "Lagerort";
    public static final String COLUMN_COVER = "Cover";


    public static final String SQL_CREATE = "CREATE TABLE " + TABLE_SPIELE +
            "(" + COLUMN_SPIEL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_TITLE + " TEXT NOT NULL, " +
            COLUMN_MIN_NUM_PLAYERS + " INTEGER, " +
            COLUMN_MAX_NUM_PLAYERS + " INTEGER, " +
            COLUMN_AVG_RATING + " REAL, " +
            COLUMN_YEAR + " INTEGER, " +
            COLUMN_DURATION + " INTEGER, " +
            COLUMN_COVER + " TEXT, " +
            COLUMN_FIRST_PLAY_DATE + " TEXT, " +
            COLUMN_OWNER + " INTEGER, " +
            COLUMN_PLACE + " INTEGER );";

    public DatenbankHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        Log.d(LOG_TAG, "DatenbankHelper hat die Datenbank: " + getDatabaseName() + " erzeugt.");
    }

    //Attention: The following creation-statement is performed only once,
    //if there is no other database with this name.
    //To create it again, the app has to be uninstalled.
    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            Log.d(LOG_TAG, "Die Tabelle wird mit SQL-Befehl: " + SQL_CREATE + " angelegt.");
            db.execSQL(SQL_CREATE);
        }
        catch (Exception ex) {
            Log.e(LOG_TAG, "Fehler beim Anlegen der Tabelle: " + ex.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            Log.d(LOG_TAG, "onUpgrade: Die Tabelle wird gelöscht und mit SQL-Befehl: " + SQL_CREATE + " neu angelegt.");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SPIELE);
            db.execSQL(SQL_CREATE);
        }
        catch (Exception ex) {
            Log.e(LOG_TAG, "Fehler beim Anlegen der Tabelle: " + ex.getMessage());
        }
    }
}
