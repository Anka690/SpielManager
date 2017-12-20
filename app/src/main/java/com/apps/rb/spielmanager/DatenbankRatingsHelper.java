package com.apps.rb.spielmanager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Anka on 17.12.2017.
 */

public class DatenbankRatingsHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = DatenbankRatingsHelper.class.getSimpleName();

    public static final String DB_NAME = "ratings_list.db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_RATINGS = "ratings_list";

    public static final String COLUMN_SPIEL_ID = "_id_spiel";
    public static final String COLUMN_SPIELER_ID = "_id_spieler";
    public static final String COLUMN_RATING = "_rating";

    public static final String SQL_CREATE = "CREATE TABLE " + TABLE_RATINGS +
            "(" + COLUMN_SPIEL_ID + " INTEGER NOT NULL, " +
            COLUMN_SPIELER_ID + " INTEGER NOT NULL, " +
            COLUMN_RATING + " INTEGER, " +
            "PRIMARY KEY ( " + COLUMN_SPIEL_ID + ", " + COLUMN_SPIELER_ID + " ));";

    public DatenbankRatingsHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        Log.d(LOG_TAG, "DatenbankRatingsHelper hat die Datenbank: " + getDatabaseName() + " erzeugt.");
    }

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

    }
}
