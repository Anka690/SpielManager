package com.apps.rb.spielmanager;

import android.util.Log;

/**
 * Created by Anka on 16.12.2017.
 */

public class Spieler {

    private static final String LOG_TAG = Spieler.class.getSimpleName();

    long _id;
    String _firstname;
    String _lastname;
    String _shortName;


    public Spieler(long id, String firstname, String lastname, String shortName){
        _id =id;
        _firstname = firstname;
        _lastname = lastname;
        _shortName = shortName;

        Log.d(LOG_TAG, "Folgender Spieler wurde erzeugt: " + toString());
    }

    public long getId(){
        return _id;
    }

    public String getShortName(){
        return _shortName;
    }

    public String get_firstname() {
        return _firstname;
    }

    public String get_lastname() {
        return _lastname;
    }

    public void setShortname(String shortname){
        _shortName = shortname;
    }

    public void setFirstname(String firstname) { _firstname = firstname; }

    public void setLastname(String lastname) {
        _lastname = lastname;
    }

    public String getStringValue(String column){
        switch (column){
            case DatenbankSpielerHelper.COLUMN_SPIELER_FIRSTNAME:
                return get_firstname();
            case DatenbankSpielerHelper.COLUMN_SPIELER_SURNAME:
                return get_lastname();
            case DatenbankSpielerHelper.COLUMN_SPIELER_SHORT_NAME:
                return getShortName();
            default:
                return "";
        }
    }

    @Override
    public String toString() {
        return "Spieler " + get_firstname() + " " + get_lastname() +
                " (Kürzel = " + getShortName() + ", Id = " + getId() +")";
    }
}
