package com.apps.rb.spielmanager;

/**
 * Created by Anka on 05.12.2017.
 */

public class Spiel {

    public Spiel(long id, String title){
        _id =id;
        _title = title;
    }
    private long _id;
    private String _title;
    private int[] _ratings;
    private int _minNumPlayers;
    private int _maxNumPlayers;

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

    public int[] getRatings() {
        return _ratings;
    }

    public void setRatings(int[] _ratings) {
        this._ratings = _ratings;
    }

    public int getMinNumPlayers() {
        return _minNumPlayers;
    }

    public void setMinNumPlayers(int _minNumPlayers) {
        this._minNumPlayers = _minNumPlayers;
    }

    public int getMaxNumPlayers() {
        return _maxNumPlayers;
    }

    public void setMaxNumPlayers(int _maxNumPlayers) {
        this._maxNumPlayers = _maxNumPlayers;
    }

    @Override
    public String toString() {
        return "Spiel{" +
                "_id=" + _id +
                ", _title='" + _title + '\'' +
                '}';
    }
}
