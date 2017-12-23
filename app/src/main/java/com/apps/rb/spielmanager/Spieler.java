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

    @Override
    public String toString() {
        return "Spieler " + get_firstname() + " " + get_lastname() +
                " (Kürzel = " + getShortName() + ", Id = " + getId() +")";
    }
}
