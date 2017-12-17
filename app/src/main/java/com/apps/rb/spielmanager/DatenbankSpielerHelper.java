package com.apps.rb.spielmanager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Anka on 17.12.2017.
 */

public class DatenbankSpielerHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = DatenbankSpielerHelper.class.getSimpleName();

    public static final String DB_NAME = "spieler_list.db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_SPIELER = "spieler_list";

    public static final String COLUMN_SPIELER_ID = "_id_spieler";
    public static final String COLUMN_SPIELER_SHORT_NAME = "_shortName_spieler";
    public static final String COLUMN_SPIELER_FIRSTNAME = "_firstname_spieler";
    public static final String COLUMN_SPIELER_SURNAME = "_surname_spieler";

    public static final String SQL_CREATE = "CREATE TABLE " + TABLE_SPIELER +
            "(" + COLUMN_SPIELER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_SPIELER_SHORT_NAME + " TEXT NOT NULL UNIQUE, " +
            COLUMN_SPIELER_FIRSTNAME + " TEXT, " +
            COLUMN_SPIELER_SURNAME + " TEXT);";


    public DatenbankSpielerHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        Log.d(LOG_TAG, "DatenbankSpielerHelper hat die Datenbank: " + getDatabaseName() + " erzeugt.");
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
