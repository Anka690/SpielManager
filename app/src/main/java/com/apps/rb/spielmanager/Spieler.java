package com.apps.rb.spielmanager;

/**
 * Created by Anka on 16.12.2017.
 */

public class Spieler {

    long _id;
    String _firstname;
    String _surname;
    String _initials;


    public Spieler(long id, String firstname, String surname, String kuerzel){
        _id =id;
        _firstname = firstname;
        _surname = surname;
        _initials = kuerzel;
    }

    public long getId(){
        return _id;
    }

    public String getInitials(){
        return _initials;
    }

    public String get_firstname() {
        return _firstname;
    }

    public String get_surname() {
        return _surname;
    }
}
