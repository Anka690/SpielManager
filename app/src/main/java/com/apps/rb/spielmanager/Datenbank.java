package com.apps.rb.spielmanager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
/**
 * Created by Anka on 05.12.2017.
 */

public class Datenbank {
    private static final String LOG_TAG = Datenbank.class.getSimpleName();

    private SQLiteDatabase database;
    private DatenbankHelper dbHelper;


    public Datenbank(Context context) {
        Log.d(LOG_TAG, "Unsere DataSource erzeugt jetzt den DatenbankHelper.");
        dbHelper = new DatenbankHelper(context);
    }
}
