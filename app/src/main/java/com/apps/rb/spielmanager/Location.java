package com.apps.rb.spielmanager;

/**
 * Created by Anka on 16.12.2017.
 */

public class Location {

    int _id;
    String _shortLocationName;
    String _street;
    int _number;
    String _city;
    int _plz;

    public void Location(int id, String shortLocationName){
        _id = id;
        _shortLocationName = shortLocationName;
    }

    public int getId(){
        return _id;
    }

    public String getShortName(){
        return _shortLocationName;
    }
}
