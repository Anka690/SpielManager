package com.apps.rb.spielmanager;

/**
 * Created by Anka on 16.12.2017.
 */

public class Spieler {

    int _id;
    String _firstname;
    String _surname;
    String _initials;


    public Spieler(int id, String firstname, String surname, String kuerzel){
        _id =id;
        _firstname = firstname;
        _surname = surname;
        _initials = kuerzel;
    }

    public int getId(){
        return _id;
    }

    public String getInitials(){
        return _initials;
    }
}
