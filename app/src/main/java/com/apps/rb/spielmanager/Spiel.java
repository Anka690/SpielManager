package com.apps.rb.spielmanager;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Anka on 05.12.2017.
 */

public class Spiel {
    private static final String LOG_TAG = Spiel.class.getSimpleName();

    public Spiel(long id, String title){
        _id =id;
        _title = title;
        _mapOfRatings =new HashMap<Spieler,Integer>();
    }
    private long _id;
    private String _title = "";
    private Map<Spieler, Integer> _mapOfRatings;
    private int _minNumPlayers = -1;
    private int _maxNumPlayers = -1;
    private int _duration = -1;
    private Spieler _owner = null;
    private Location _place = null;
    private String _cover = "";
    private int _year = -1;


    public long getId() {
        return _id;
    }

    public void setId(long _id) {
        this._id = _id;
    }

    public String getTitle() {
        return _title;
    }

    public void setTitle(String _title) {
        this._title = _title;
    }

    public Map<Spieler, Integer> getRatings() {
        return _mapOfRatings;
    }

    public void addRating(Spieler player, int rating) {
        //adds a new rating or overwrites the rating given by this player
        _mapOfRatings.put(player, rating);
    }

    public int getMinNumPlayers() {
        return _minNumPlayers;
    }

    public String getMinNumPlayersString() {
        if( _minNumPlayers != -1 ){
            return String.valueOf(_minNumPlayers);
        } else {
            return "";
        }
    }

    public void setMinNumPlayers(int _minNumPlayers) {
        this._minNumPlayers = _minNumPlayers;
    }

    public int getMaxNumPlayers() {
        return _maxNumPlayers;
    }

    public String getMaxNumPlayersString() {
        if( _maxNumPlayers != -1 ){
            return String.valueOf(_maxNumPlayers);
        } else {
            return "";
        }
    }

    public void setMaxNumPlayers(int _maxNumPlayers) {
        this._maxNumPlayers = _maxNumPlayers;
    }


    public double getAverageRating() {
        //Log.d(LOG_TAG, "getAverageRating: gestartet für Spiel " + this.getTitle() +"...");
        int numEntries = 0;
        int sumRatings = 0;
        for(  Map.Entry<Spieler, Integer> mapEntry : _mapOfRatings.entrySet()){
            numEntries++;
            sumRatings += mapEntry.getValue();
        }
        if( numEntries == 0 ){
            return 0.0;
        }
        return sumRatings/(double)numEntries;
    }

    public String getAverageRatingToString() {
        double average = getAverageRating();
        if( average == 0.0 ){
            return "";
        } else{
            return String.valueOf(average);
        }
    }

    public int getDuration(){
        return _duration;
    }

    public int getYear(){
        return _year;
    }

    public void setCoverString(String cover) {
        this._cover = cover;
    }

    public String getCoverString(){
        return _cover;
    }

    public int getPlaceId(){
        if( _place != null ){
            return _place.getId();
        } else{
            return -1;
        }
    }

    public long getOwnerId(){
        if( _owner != null ){
            return _owner.getId();
        } else{
            return -1;
        }
    }


    public String getStringValue(String column){
        switch (column){
            case DatenbankHelper.COLUMN_TITLE:
                return getTitle();
            case DatenbankHelper.COLUMN_COVER:
                return getCoverString();
            default:
                return "";
        }
    }

    public int getIntValue(String column){
        switch (column){
            case DatenbankHelper.COLUMN_MIN_NUM_PLAYERS:
                return getMinNumPlayers();
            case DatenbankHelper.COLUMN_MAX_NUM_PLAYERS:
                return getMaxNumPlayers();
            case DatenbankHelper.COLUMN_DURATION:
                return getDuration();
            case DatenbankHelper.COLUMN_YEAR:
                return getYear();
            case DatenbankHelper.COLUMN_PLACE:
                return getPlaceId();
            case DatenbankHelper.COLUMN_OWNER:
                return (int)getOwnerId();
            default:
                return -1;
        }
    }

    public double getRealValue(String column){
        switch (column){
            case DatenbankHelper.COLUMN_AVG_RATING:
                return getAverageRating();
            default:
                return 0.0;
        }
    }

    @Override
    public String toString() {
        return _title + " (Id = " + _id +")";
               /* "Spiel{" +
                "_id=" + _id +
                ", _title='" + _title + '\'' +
                '}';*/
    }
}
