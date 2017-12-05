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


    public DatenbankHelper(Context context) {
        super(context, "Spieledatenbank", null, 1);
        Log.d(LOG_TAG, "DatenbankHelper hat die Datenbank: " + getDatabaseName() + " erzeugt.");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
