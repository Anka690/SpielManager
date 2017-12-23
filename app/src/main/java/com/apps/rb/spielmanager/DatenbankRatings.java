package com.apps.rb.spielmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.Pair;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Anka on 17.12.2017.
 */

public class DatenbankRatings {
    private static final String LOG_TAG = DatenbankRatings.class.getSimpleName();

    private SQLiteDatabase databaseRatings;
    private DatenbankRatingsHelper dbRatingsHelper;

    private String[] columns = {
            DatenbankRatingsHelper.COLUMN_SPIELER_ID,
            DatenbankRatingsHelper.COLUMN_SPIEL_ID,
            DatenbankRatingsHelper.COLUMN_RATING
    };

    private String[] intColumns = {
            DatenbankRatingsHelper.COLUMN_SPIELER_ID,
            DatenbankRatingsHelper.COLUMN_SPIEL_ID,
            DatenbankRatingsHelper.COLUMN_RATING
    };

    public DatenbankRatings(Context context) {
        Log.d(LOG_TAG, "Unsere DataSource erzeugt jetzt den DatenbankRatingsHelper.");

        dbRatingsHelper = new DatenbankRatingsHelper(context);
    }

    public void open() {
        Log.d(LOG_TAG, "Eine Referenz auf die RatingsDatenbank wird jetzt angefragt.");
        databaseRatings = dbRatingsHelper.getWritableDatabase();

        Log.d(LOG_TAG, "RatingsDatenbank-Referenz erhalten. Pfad zur Datenbank: " + databaseRatings.getPath());
    }

    public void close() {
        dbRatingsHelper.close();
        Log.d(LOG_TAG, "RatingsDatenbank mit Hilfe des DbRatingsHelpers geschlossen.");
    }

    public void addRating( long spielerId, long spielId, int rating){
        ContentValues values = new ContentValues();
        values.put(DatenbankRatingsHelper.COLUMN_SPIELER_ID, spielerId);
        values.put(DatenbankRatingsHelper.COLUMN_SPIEL_ID, spielId);
        values.put(DatenbankRatingsHelper.COLUMN_RATING, rating);

        databaseRatings.execSQL("INSERT OR REPLACE INTO " + DatenbankRatingsHelper.TABLE_RATINGS +
            "(" + DatenbankRatingsHelper.COLUMN_SPIELER_ID + ", " + DatenbankRatingsHelper.COLUMN_SPIEL_ID + ", " + DatenbankRatingsHelper.COLUMN_RATING + ")" +
            " VALUES (" + spielerId + ", " + spielId + ", " + rating + ")");
/*        INSERT OR REPLACE INTO Employee (id, name, role)
        VALUES (1, 'John Foo', 'CEO');*/
        //databaseRatings.insert(DatenbankRatingsHelper.TABLE_RATINGS, null, values);
    }

    public Map<Long, Integer> getAllRatings(Spiel game) {
        Log.d(LOG_TAG, "getAllRatings to Spiel " + game.getTitle() + " mit Id " + game.getId() + ": gestartet...");
        Map<Long, Integer> mapRatings = new HashMap<Long, Integer>();
        String whereClause = DatenbankRatingsHelper.COLUMN_SPIEL_ID + " = ?" ;
        String[] whereArgs = new String[] { String.valueOf(game.getId()) };

        //String orderBy = DatenbankRatingsHelper.COLUMN_SPIELER_ID;
       /* databaseRatings.execSQL("SELECT * FROM " + DatenbankRatingsHelper.TABLE_RATINGS +
                        " WHERE " + DatenbankRatingsHelper.COLUMN_SPIEL_ID + " = " + game.getId() );*/
       try {
           Cursor cursor = databaseRatings.query(DatenbankRatingsHelper.TABLE_RATINGS,
                   columns, whereClause, whereArgs, null, null, null);
           if (cursor.getCount() > 0) {
               cursor.moveToFirst();
               while (!cursor.isAfterLast()) {
                   Pair<Long, Integer> ratingEntry = cursorToRatingEntry(cursor);
                   mapRatings.put(ratingEntry.first, ratingEntry.second);
                   Log.d(LOG_TAG, "Spieler-Id: " + ratingEntry.first + ", Rating: " + ratingEntry.second);
                   cursor.moveToNext();
               }
           }
           cursor.close();
       } catch (Exception e){
           Log.d(LOG_TAG,  e.getMessage());
           throw new RuntimeException("Exception in getAllRatings");
       }
        Log.d(LOG_TAG, "getAllRatings to Spiel " + game.getTitle() + ": found " + mapRatings.size() + " ratings.");
        return mapRatings;
    }

    private Pair<Long, Integer> cursorToRatingEntry(Cursor cursor) {
        int idSpielerIndex = cursor.getColumnIndex(DatenbankRatingsHelper.COLUMN_SPIELER_ID);
        long spielerId = cursor.getLong(idSpielerIndex);
        int ratingIndex = cursor.getColumnIndex(DatenbankRatingsHelper.COLUMN_RATING);
        int rating = cursor.getInt(ratingIndex);

        return new Pair<Long, Integer>(spielerId, rating);
    }

}
