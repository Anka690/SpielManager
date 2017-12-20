package com.apps.rb.spielmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Anka on 17.12.2017.
 */

public class DatenbankSpieler {

    private static final String LOG_TAG = DatenbankSpieler.class.getSimpleName();

    private SQLiteDatabase databaseSpieler;
    private DatenbankSpielerHelper dbSpielerHelper;

    private Map<Long, Spieler> _mapOfPlayers;

    private String[] columns = {
            DatenbankSpielerHelper.COLUMN_SPIELER_ID,
            DatenbankSpielerHelper.COLUMN_SPIELER_SHORT_NAME,
            DatenbankSpielerHelper.COLUMN_SPIELER_FIRSTNAME,
            DatenbankSpielerHelper.COLUMN_SPIELER_SURNAME
    };

    private String[] intColumns = {
            DatenbankSpielerHelper.COLUMN_SPIELER_ID
    };

    private String[] stringColumns = {
            DatenbankSpielerHelper.COLUMN_SPIELER_SHORT_NAME,
            DatenbankSpielerHelper.COLUMN_SPIELER_FIRSTNAME,
            DatenbankSpielerHelper.COLUMN_SPIELER_SURNAME
    };

    public DatenbankSpieler(Context context) {
        Log.d(LOG_TAG, "Unsere DataSource erzeugt jetzt den DatenbankSpielerHelper.");
        _mapOfPlayers =new HashMap<Long,Spieler>();

        dbSpielerHelper = new DatenbankSpielerHelper(context);

        open();
        fillMapOfPlayers();

        if( getPlayerByShortName("AR") == null) {
            createSpieler("AR", "Anka", "Rothenbächer");
        }
        if( getPlayerByShortName("TR") == null) {
            createSpieler("TR", "Thomas", "Rothenbächer");
        }
        if( getPlayerByShortName("MM") == null) {
            createSpieler("MM", "Micha", "Müller");
        }
        if( getPlayerByShortName("SZ") == null) {
            createSpieler("SZ", "Svenja", "Zielke");
        }
        if( getPlayerByShortName("JR") == null) {
            createSpieler("JR", "Janet", "Rothenbächer");
        }
        if( getPlayerByShortName("PR") == null) {
            createSpieler("PR", "Paul", "Rothenbächer");
        }
        if( getPlayerByShortName("NR") == null) {
            createSpieler("NR", "Nicole", "Rothenbächer");
        }
        if( getPlayerByShortName("CS") == null) {
            createSpieler("CS", "Cass", "Serna");
        }
        close();
    }

    public void fillMapOfPlayers(){
        Log.d(LOG_TAG, "fillMapOfPlayers: gestartet...");
        Cursor cursor = databaseSpieler.query(DatenbankSpielerHelper.TABLE_SPIELER,
                columns, null, null, null, null, null);

        if( cursor == null){
            Log.d(LOG_TAG, "fillMapOfPlayers: cursor is null");
            return;
        }
        cursor.moveToFirst();
        Spieler player;

        while(!cursor.isAfterLast()) {
            int idIndex = cursor.getColumnIndex(DatenbankSpielerHelper.COLUMN_SPIELER_ID);
            long id = cursor.getLong(idIndex);
            if( _mapOfPlayers.containsKey(id)){
                Log.d(LOG_TAG, "fillMapOfPlayers: key already included.");
                cursor.moveToNext();
                continue;
            }
            int idInitials = cursor.getColumnIndex(DatenbankSpielerHelper.COLUMN_SPIELER_SHORT_NAME);
            String initials = cursor.getString(idInitials);

            int idFirstname = cursor.getColumnIndex(DatenbankSpielerHelper.COLUMN_SPIELER_FIRSTNAME);
            String firstname = cursor.getString(idFirstname);

            int idLastname = cursor.getColumnIndex(DatenbankSpielerHelper.COLUMN_SPIELER_SURNAME);
            String lastname = cursor.getString(idLastname);

            player = new Spieler(id, initials, firstname, lastname);

            _mapOfPlayers.put(player.getId(), player);

            cursor.moveToNext();
        }
        cursor.close();

        Log.d(LOG_TAG, "fillMapOfPlayers: beendet. mapOfPlayers.size() = " + _mapOfPlayers.size());
    }

    public void open() {
        Log.d(LOG_TAG, "Eine Referenz auf die SpielerDatenbank wird jetzt angefragt.");
        databaseSpieler = dbSpielerHelper.getWritableDatabase();

        Log.d(LOG_TAG, "SpielerDatenbank-Referenz erhalten. Pfad zur Datenbank: " + databaseSpieler.getPath());
    }

    public void close() {
        dbSpielerHelper.close();
        Log.d(LOG_TAG, "SpielerDatenbank mit Hilfe des DbSpielerHelpers geschlossen.");
    }

    public Spieler getPlayerById(long id){
        Log.d(LOG_TAG, "getPlayerById: gestartet...");
        Log.d(LOG_TAG, "Anzahl Einträge in mapOfPlayers = " + _mapOfPlayers.size());
        if( _mapOfPlayers.containsKey(id) ){
            return _mapOfPlayers.get(id);
        } else {
            return null;
        }
    }

    public Spieler getPlayerByShortName(String shortName){
        Log.d(LOG_TAG, "getPlayerByShortName: gestartet mit shortName = " + shortName + " ...");

        String whereClause = DatenbankSpielerHelper.COLUMN_SPIELER_SHORT_NAME + " = ?" ;
        String[] whereArgs = new String[] { shortName };

        Cursor cursor = databaseSpieler.query(DatenbankSpielerHelper.TABLE_SPIELER,
                columns, whereClause, whereArgs, null, null, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            return cursorToSpieler(cursor);
        } else{
            Log.d(LOG_TAG, "getPlayerByShortName: no entry found");
            return null;
        }
    }

    public Spieler createSpieler(String initials, String firstname, String lastname) {
        Log.d(LOG_TAG, "createSpieler: gestartet...");
        ContentValues values = new ContentValues();
        values.put(DatenbankSpielerHelper.COLUMN_SPIELER_SHORT_NAME, initials);
        values.put(DatenbankSpielerHelper.COLUMN_SPIELER_FIRSTNAME, firstname);
        values.put(DatenbankSpielerHelper.COLUMN_SPIELER_SURNAME, lastname);

        if( databaseSpieler == null){
            Log.d(LOG_TAG, "createSpieler: Error. databaseSpieler is null");
        }
        long insertId = databaseSpieler.insert(DatenbankSpielerHelper.TABLE_SPIELER, null, values);
        if( insertId > 0) {
            Spieler player = new Spieler(insertId, firstname, lastname, initials);
            _mapOfPlayers.put(insertId, player);

            Log.d(LOG_TAG, "createSpieler: Spieler " + initials + " mit Id " + insertId + " erfolgreich hinzugefügt.");
            return player;
        } else{
            return null;
        }
    }

    public List<Spieler> getAllSpieler() {
        Log.d(LOG_TAG, "getAllSpieler: gestartet...");
        List<Spieler> playerList = new ArrayList<>();

        Cursor cursor = databaseSpieler.query(DatenbankSpielerHelper.TABLE_SPIELER,
                columns, null, null, null, null, null);

        cursor.moveToFirst();
        Spieler player;
        while(!cursor.isAfterLast()) {
            player = cursorToSpieler(cursor);
            if( player == null ){
                Log.d(LOG_TAG, "getAllSpieler: player is null");
            }
            playerList.add(player);
            Log.d(LOG_TAG, "Spieler-ID: " + player.getId() + ", Inhalt: " + player.getInitials());
            cursor.moveToNext();
        }
        cursor.close();

        return playerList;
    }

    private Spieler cursorToSpieler(Cursor cursor) {
        Log.d(LOG_TAG, "cursorToSpieler: gestartet...");
        int idIndex = cursor.getColumnIndex(DatenbankSpielerHelper.COLUMN_SPIELER_ID);
        long id = cursor.getLong(idIndex);
        return getPlayerById(id);
    }
}
