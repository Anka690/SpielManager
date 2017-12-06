package com.apps.rb.spielmanager;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.List;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = Datenbank.class.getSimpleName();

    private Datenbank _dataSource;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(LOG_TAG, "onCreate-Methode der MainActivity wird ausgeführt.");

        _dataSource = new Datenbank(this);

        Log.d(LOG_TAG, "Die Datenquelle wird geöffnet.");
        _dataSource.open();

        //Spiel game = _dataSource.createSpiel("NeuesSpiel");
       /* Log.d(LOG_TAG, "Es wurde der folgende Eintrag in die Datenbank geschrieben:");
        Log.d(LOG_TAG, "ID: " + game.getId() + ", Inhalt: " + game.toString());*/

        Log.d(LOG_TAG, "Folgende Einträge sind in der Datenbank vorhanden:");
        showAllListEntries();

        Log.d(LOG_TAG, "Die Datenquelle wird geschlossen.");
        _dataSource.close();


        Button add = (Button) findViewById(R.id.ButtonAdd);
        add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), AddGameActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });

/*        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replaced with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
    }

    private void showAllListEntries () {
        List<Spiel> gameList = _dataSource.getAllSpiele();

        ArrayAdapter<Spiel> gameArrayAdapter = new ArrayAdapter<> (
                this,
                android.R.layout.simple_list_item_multiple_choice,
                gameList);

        ListView gamesListView = (ListView) findViewById(R.id.listview_games);
        gamesListView.setAdapter(gameArrayAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(LOG_TAG, "onResume-Methode der MainActivity wird ausgeführt.");

        Log.d(LOG_TAG, "Die Datenquelle wird geöffnet.");
        _dataSource.open();

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            String newTitle = bundle.getString("title");
            _dataSource.createSpiel(newTitle);
        }

        Log.d(LOG_TAG, "Folgende Einträge sind in der Datenbank vorhanden:");
        showAllListEntries();
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d(LOG_TAG, "Die Datenquelle wird geschlossen.");
        _dataSource.close();
    }
}
