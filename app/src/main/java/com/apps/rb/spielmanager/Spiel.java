package com.apps.rb.spielmanager;

/**
 * Created by Anka on 05.12.2017.
 */

public class Spiel {

    private int _id;
    private String _title;
    private int[] _ratings;
    private int _minNumPlayers;
    private int _maxNumPlayers;

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String get_title() {
        return _title;
    }

    public void set_title(String _title) {
        this._title = _title;
    }

    public int[] get_ratings() {
        return _ratings;
    }

    public void set_ratings(int[] _ratings) {
        this._ratings = _ratings;
    }

    public int get_minNumPlayers() {
        return _minNumPlayers;
    }

    public void set_minNumPlayers(int _minNumPlayers) {
        this._minNumPlayers = _minNumPlayers;
    }

    public int get_maxNumPlayers() {
        return _maxNumPlayers;
    }

    public void set_maxNumPlayers(int _maxNumPlayers) {
        this._maxNumPlayers = _maxNumPlayers;
    }


}
