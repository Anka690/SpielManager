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
import android.widget.Button;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.widget.AbsListView;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.text.TextUtils;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private Datenbank _dataSource;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(LOG_TAG, "onCreate-Methode der MainActivity wird ausgeführt.");

        _dataSource = new Datenbank(this);

        activateAddButton();

        initializeContextualActionBar();
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

    private void activateAddButton(){
        Button add = (Button) findViewById(R.id.ButtonAdd);
        add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), AddGameActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });
    }


    private AlertDialog createEditGameDialog(final Spiel game) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        View dialogsView = inflater.inflate(R.layout.dialog_edit_game, null);

        final EditText editTextNewTitle = (EditText) dialogsView.findViewById(R.id.editText_new_title);
        editTextNewTitle.setText(String.valueOf(game.getTitle()));

        builder.setView(dialogsView)
                .setTitle(R.string.dialog_change_game)
                .setPositiveButton(R.string.dialog_button_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String titleString = editTextNewTitle.getText().toString();

                        if ((TextUtils.isEmpty(titleString)) ) {
                            Log.d(LOG_TAG, "Ein Eintrag enthielt keinen Text. Daher Abbruch der Änderung.");
                            return;
                        }

                        //int quantity = Integer.parseInt(quantityString);

                        // An dieser Stelle schreiben wir die geänderten Daten in die SQLite Datenbank
                        Spiel updatedGame = _dataSource.updateGame(game.getId(), titleString);

                        Log.d(LOG_TAG, "Alter Eintrag - ID: " + game.getId() + " Inhalt: " + game.toString());
                        Log.d(LOG_TAG, "Neuer Eintrag - ID: " + updatedGame.getId() + " Inhalt: " + updatedGame.toString());

                        showAllListEntries();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.dialog_button_negative, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        return builder.create();
    }

    private void initializeContextualActionBar() {

        final ListView gamesListView = (ListView) findViewById(R.id.listview_games);
        gamesListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        gamesListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            int selCount = 0;
            final SparseBooleanArray touchedGamesPositions = gamesListView.getCheckedItemPositions();

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                if (checked) {
                    selCount++;
                } else {
                    selCount--;
                }
                String cabTitle = selCount + " " + getString(R.string.cab_checked_string);
                mode.setTitle(cabTitle);
                mode.invalidate();
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                getMenuInflater().inflate(R.menu.menu_contextual_action_bar, menu);
                return true;
            }

            // In dieser Callback-Methode reagieren wir auf den invalidate() Aufruf
            // Wir lassen das Edit-Symbol verschwinden, wenn mehr als 1 Eintrag ausgewählt ist
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                MenuItem item = menu.findItem(R.id.action_change);
                if (selCount == 1) {
                    item.setVisible(true);
                } else {
                    item.setVisible(false);
                }

                return true;
            }

            @Override
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                boolean returnValue = true;
                //final SparseBooleanArray touchedGamesPositions = gamesListView.getCheckedItemPositions();
                Log.d(LOG_TAG, "Before action: Anzahl markierter Einträge = " + touchedGamesPositions.size());

                switch (item.getItemId()) {

                    case R.id.action_delete:
                        Log.d(LOG_TAG, "Delete action chosen: Anzahl markierter Einträge = " + touchedGamesPositions.size());
                        // 1. Erzeugen eines AlertDialog.Builder Objekts mit dem Konstruktor
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);//getActivity()
                        // 2. Aneinanderreihen mehrerer Setter-Methoden um den Dialog zu bauen
                        builder.setMessage(R.string.dialog_message_delete);
                               //.setTitle(R.string.dialog_title);
                        // Hinzufügen der Buttons
                        builder.setPositiveButton(R.string.dialog_button_positive, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                               for (int i=0; i < touchedGamesPositions.size(); i++) {
                                    boolean isChecked = touchedGamesPositions.valueAt(i);
                                    if(isChecked) {
                                        int positionInListView = touchedGamesPositions.keyAt(i);
                                        Spiel game = (Spiel) gamesListView.getItemAtPosition(positionInListView);
                                        Log.d(LOG_TAG, "Lösche Position im ListView: " + positionInListView + " Inhalt: " + game.toString());
                                        _dataSource.deleteGame(game);
                                    }
                                }
                                showAllListEntries();
                                mode.finish();
                                dialog.dismiss();
                            }
                        });
                        builder.setNegativeButton(R.string.dialog_button_negative, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mode.finish();
                                dialog.cancel();
                            }
                        });
                        // 3. Erzeugen des AlertDialogs mit der create() Methode
                        AlertDialog alertDialog = builder.create();
                        // 4. Anzeigen des AlertDialogs mit der show() Methode
                        alertDialog.show();
                        break;

                    case R.id.action_change:
                        Log.d(LOG_TAG, "Eintrag ändern");
                        for (int i = 0; i < touchedGamesPositions.size(); i++) {
                            boolean isChecked = touchedGamesPositions.valueAt(i);
                            if (isChecked) {
                                int positionInListView = touchedGamesPositions.keyAt(i);
                                Spiel game = (Spiel) gamesListView.getItemAtPosition(positionInListView);
                                Log.d(LOG_TAG, "Position im ListView: " + positionInListView + " Inhalt: " + game.toString());

                                AlertDialog editGameDialog = createEditGameDialog(game);
                                editGameDialog.show();
                            }
                        }
                        Log.d(LOG_TAG, "Change-Action. Anzahl markierter Einträge am Ende = " + touchedGamesPositions.size());
                        mode.finish();
                        break;

                    default:
                        return false;
                }

                //mode.finish(); //löscht alle Markierungen der Einträge.
                //Da Dialog-Zeigen die Code-Ausführung nicht unterbricht, führt das Einkommentieren dazu, dass nichts mehr gelöscht wird.
                return returnValue;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                Log.d(LOG_TAG, "onDestroyActionMode called");
                selCount = 0;
            }
        });
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

        // Holt das Menüeintrag-Objekt, das dem ShareActionProvider zugeordnet ist
        MenuItem shareMenuItem = menu.findItem(R.id.action_teile_spieldaten);

        // Holt den ShareActionProvider über den Share-Menüeintrag
        ShareActionProvider sAP;
        sAP = (ShareActionProvider) MenuItemCompat.getActionProvider(shareMenuItem);

        // Erzeugen des SEND-Intents mit den Spieldaten als Text
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        //noinspection deprecation
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Daten zum Test: "); //spielInfo

        // Der SEND-Intent wird an den ShareActionProvider angehangen
        if (sAP != null ) {
            sAP.setShareIntent(shareIntent);
        } else {
            String LOG_TAG = MainActivity.class.getSimpleName();
            Log.d(LOG_TAG, "Kein ShareActionProvider vorhanden!");
        }
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
