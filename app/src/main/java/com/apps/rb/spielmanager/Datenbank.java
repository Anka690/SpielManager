package com.apps.rb.spielmanager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.content.ContentValues;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Anka on 05.12.2017.
 */

public class Datenbank {
    private static final String LOG_TAG = Datenbank.class.getSimpleName();

    private SQLiteDatabase database;
    private DatenbankHelper dbHelper;

    //Problem: diese Map wird natürlich bei Beendigung des Spielmanagers nicht gespeichert. Daher funktioniert erneuter Start gerade nicht
    //TODO: retrieve all information by the database (rcreate games once)
    private Map<Long, Spiel> _mapOfGames;

    private String[] columns = {
            DatenbankHelper.COLUMN_SPIEL_ID,
            DatenbankHelper.COLUMN_TITLE,
            DatenbankHelper.COLUMN_MIN_NUM_PLAYERS,
            DatenbankHelper.COLUMN_MAX_NUM_PLAYERS,
            DatenbankHelper.COLUMN_AVG_RATING,
            DatenbankHelper.COLUMN_YEAR,
            DatenbankHelper.COLUMN_DURATION,
            DatenbankHelper.COLUMN_FIRST_PLAY_DATE,
            DatenbankHelper.COLUMN_OWNER,
            DatenbankHelper.COLUMN_PLACE,
            DatenbankHelper.COLUMN_COVER
    };

    private String[] stringColumns = {
            DatenbankHelper.COLUMN_TITLE,
            DatenbankHelper.COLUMN_FIRST_PLAY_DATE,
            DatenbankHelper.COLUMN_COVER
    };

    private String[] intColumns = {
            DatenbankHelper.COLUMN_MIN_NUM_PLAYERS,
            DatenbankHelper.COLUMN_MAX_NUM_PLAYERS,
            DatenbankHelper.COLUMN_YEAR ,
            DatenbankHelper.COLUMN_DURATION,
            DatenbankHelper.COLUMN_OWNER ,
            DatenbankHelper.COLUMN_PLACE
    };

    private String[] realColumns = {
            DatenbankHelper.COLUMN_AVG_RATING
    };


    public Datenbank(Context context) {
        Log.d(LOG_TAG, "Unsere DataSource erzeugt jetzt den DatenbankHelper.");
        _mapOfGames =new HashMap<Long,Spiel>();

        dbHelper = new DatenbankHelper(context);
    }

    //fill mapOfGames, hoping that this method is only called when the app is started, we create the game-objects here
    public void fillMapOfGames(){
        Log.d(LOG_TAG, "fillMapOfGames: gestartet...");
        Cursor cursor = database.query(DatenbankHelper.TABLE_SPIELE,
                columns, null, null, null, null, null);

        if( cursor == null){
            Log.d(LOG_TAG, "fillMapOfGames: cursor is null");
            return;
        }
        cursor.moveToFirst();
        Spiel game;

        while(!cursor.isAfterLast()) {
            int idIndex = cursor.getColumnIndex(DatenbankHelper.COLUMN_SPIEL_ID);
            int idTitle = cursor.getColumnIndex(DatenbankHelper.COLUMN_TITLE);
            long id = cursor.getLong(idIndex);

            if( _mapOfGames.containsKey(id)){
                Log.d(LOG_TAG, "fillMapOfGames: key already included.");
                cursor.moveToNext();
                continue;//TODO: unschön! This method should be called only once when app is started
            }
            String title = cursor.getString(idTitle);
            game = new Spiel(id, title);

            int minPlayers = cursor.getInt(cursor.getColumnIndex(DatenbankHelper.COLUMN_MIN_NUM_PLAYERS));
            game.setMinNumPlayers(minPlayers);

            int maxPlayers = cursor.getInt(cursor.getColumnIndex(DatenbankHelper.COLUMN_MAX_NUM_PLAYERS));
            game.setMaxNumPlayers(maxPlayers);

            _mapOfGames.put(game.getId(), game);

            Log.d(LOG_TAG, "ID: " + game.getId() + ", Inhalt: " + game.toString());
            cursor.moveToNext();
        }
        cursor.close();

        Log.d(LOG_TAG, "fillMapOfGames: beendet. mapOfGames.size() = " + _mapOfGames.size());
    }

    public void open() {
        Log.d(LOG_TAG, "Eine Referenz auf die Datenbank wird jetzt angefragt.");
        database = dbHelper.getWritableDatabase();
        fillMapOfGames();
        Log.d(LOG_TAG, "Datenbank-Referenz erhalten. Pfad zur Datenbank: " + database.getPath());
    }

    public void close() {
        dbHelper.close();
        Log.d(LOG_TAG, "Datenbank mit Hilfe des DbHelpers geschlossen.");
    }

    public Spiel getGameById(long id){
        Log.d(LOG_TAG, "getGameById: gestartet...");
        Log.d(LOG_TAG, "Anzahl Einträge in mapOfGames = " + _mapOfGames.size());
        return _mapOfGames.get(id);
    }

    public Spiel createSpiel(String title) {
        Log.d(LOG_TAG, "createSpiel: gestartet...");
        ContentValues values = new ContentValues();
        values.put(DatenbankHelper.COLUMN_TITLE, title);

        if( database == null){
            Log.d(LOG_TAG, "createSpiel: Error. database is null");
        }
        long insertId = database.insert(DatenbankHelper.TABLE_SPIELE, null, values);
        if( insertId > 0) {
            Log.d(LOG_TAG, "createSpiel: Insertion in database successful.");
        }
        Spiel game = new Spiel( insertId, title);
        _mapOfGames.put(insertId, game);

        Log.d(LOG_TAG, "createSpiel: Spiel " + title + " mit Id " + insertId + " erfolgreich hinzugefügt.");
        return game;
    }

    public void deleteGame(Spiel game) {
        long id = game.getId();

        database.delete(DatenbankHelper.TABLE_SPIELE,
                DatenbankHelper.COLUMN_SPIEL_ID + "=" + id,
                null);

        Log.d(LOG_TAG, "Eintrag gelöscht! ID: " + id + " Inhalt: " + game.toString());
    }

    public void updateGame(Spiel game) {
        Log.d(LOG_TAG, "updateGame: gestartet...");
        ContentValues values = new ContentValues();
        for (String column: stringColumns) {
            //Log.d(LOG_TAG, "updateGame: Column = " + column + " , Value = " + game.getStringValue(column));
            values.put(column, game.getStringValue(column));
        }
        for (String column: intColumns) {
            //Log.d(LOG_TAG, "updateGame: Column = " + column + " , Value = " + game.getIntValue(column));
            values.put(column, game.getIntValue(column));
        }
        for (String column: realColumns) {
            //Log.d(LOG_TAG, "updateGame: Column = " + column + " , Value = " + game.getRealValue(column));
            values.put(column, game.getRealValue(column));
        }
        doInternGameUpdate(game.getId(), values);
    }

    public Spiel doInternGameUpdate(long id, ContentValues values){
        Log.d(LOG_TAG, "doInternGameUpdate: gestartet...");
        database.update(DatenbankHelper.TABLE_SPIELE,
                values,
                DatenbankHelper.COLUMN_SPIEL_ID + "=" + id,
                null);

        return getGameById(id);
    }

    private Spiel cursorToSpiel(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(DatenbankHelper.COLUMN_SPIEL_ID);
        int idTitle = cursor.getColumnIndex(DatenbankHelper.COLUMN_TITLE);

        String title = cursor.getString(idTitle);
        long id = cursor.getLong(idIndex);

        return getGameById(id);
    }

    public List<Spiel> getAllSpiele() {
        Log.d(LOG_TAG, "getAllSpiele: gestartet...");
        List<Spiel> gameList = new ArrayList<>();

        Cursor cursor = database.query(DatenbankHelper.TABLE_SPIELE,
                columns, null, null, null, null, null);

        cursor.moveToFirst();
        Spiel game;

        while(!cursor.isAfterLast()) {
            game = cursorToSpiel(cursor);
            if( game == null ){
                Log.d(LOG_TAG, "getAllSpiele: game is null");
            }
            gameList.add(game);
            Log.d(LOG_TAG, "ID: " + game.getId() + ", Inhalt: " + game.toString());
            cursor.moveToNext();
        }

        cursor.close();

        return gameList;
    }
}
