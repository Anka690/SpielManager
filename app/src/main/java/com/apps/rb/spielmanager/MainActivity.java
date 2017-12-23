package com.apps.rb.spielmanager;

import android.os.Bundle;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.util.TypedValue;
import android.content.Intent;
import android.content.DialogInterface;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.text.TextUtils;
import android.app.ProgressDialog;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;


public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private Datenbank _dataSource;
    private DatenbankSpieler _dataSourceSpieler;
    private DatenbankRatings _dataSourceRatings;

    private static final int ADD_GAME_ACTIVITY_RESULT_CODE = 0;

    private TableLayout mTableLayout;
    ProgressDialog mProgressBar;

    private static Map<TableRow, Long> _mapTableRowToGameId;

    private List<Spiel> gameList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(LOG_TAG, "onCreate-Methode der MainActivity wird ausgeführt.");

        activateAddButton();

        //initializeContextualActionBar();

        mProgressBar = new ProgressDialog(this);

        // setup the table
        mTableLayout = (TableLayout) findViewById(R.id.tableGames);
        mTableLayout.setStretchAllColumns(true);

        _dataSource = new Datenbank(this);
        _dataSourceSpieler = new DatenbankSpieler(this);
        _dataSourceRatings = new DatenbankRatings(this);

        //at the start, fill the ratings in the Spiel-objects
        restoreRatingsInGames();


        _mapTableRowToGameId = new HashMap<TableRow, Long>();
        startLoadData();
    }

    private void restoreRatingsInGames(){
        _dataSource.open();
        _dataSourceSpieler.open();
        _dataSourceRatings.open();
        for(Spiel game : _dataSource.getAllSpiele()) {
            Map<Long, Integer> mapOfRatings = _dataSourceRatings.getAllRatings(game);
            for (Map.Entry<Long, Integer> rating : mapOfRatings.entrySet()) {
                game.addRating(_dataSourceSpieler.getPlayerById(rating.getKey()), rating.getValue());
            }
        }
        _dataSource.close();
        _dataSourceSpieler.close();
        _dataSourceRatings.close();
    }


    private void activateAddButton(){
        Button add = (Button) findViewById(R.id.ButtonAdd);
        add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), AddGameActivity.class);
                /*Bundle bundle = new Bundle();
                bundle.putString ("title", titleString);
                myIntent.putExtras(bundle);*/

                startActivityForResult(myIntent, ADD_GAME_ACTIVITY_RESULT_CODE);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(LOG_TAG, "onNewIntent: gestartet...");
        setIntent(intent);
        //now getIntent() should always return the last received intent
    }

   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(LOG_TAG, "onActivityResult: gestartet...");
        // check that it is the SecondActivity with an OK result
        if (requestCode == ADD_GAME_ACTIVITY_RESULT_CODE) {
            if (resultCode == RESULT_OK) {

                Log.d(LOG_TAG, "Die Datenquelle wird geöffnet.");
                _dataSource.open();

                Bundle bundle = getIntent().getExtras();
                if(bundle != null) {
                    Log.d(LOG_TAG, "onActivityResult: Bundle is not null.");
                    String newTitle = bundle.getString("title");
                    Spiel game = _dataSource.createSpiel(newTitle);
                    int defaultPlayerNumber = -1;
                    int minPlayers = bundle.getInt("minPlayers", defaultPlayerNumber);
                    if (minPlayers != defaultPlayerNumber) {
                        game.setMinNumPlayers((minPlayers));
                        //_dataSource.updateGame(game.getId(), DatenbankHelper.COLUMN_MIN_NUM_PLAYERS, minPlayers );
                    }
                    int maxPlayers = bundle.getInt("maxPlayers", defaultPlayerNumber);
                    if (maxPlayers != defaultPlayerNumber) {
                        game.setMaxNumPlayers((maxPlayers));
                        //_dataSource.updateGame(game.getId(), DatenbankHelper.COLUMN_MAX_NUM_PLAYERS, maxPlayers );
                    }
                    _dataSource.updateGame(game);
                } else{
                    Log.d(LOG_TAG, "onActivityResult: Attention: Bundle is null.");
                }

                Log.d(LOG_TAG, "Folgende Einträge sind in der Datenbank vorhanden:");
                startLoadData();
            }
        }
    }*/


    public void startLoadData() {
        mProgressBar.setCancelable(false);
        mProgressBar.setMessage("Lädt die Spiele...");
        mProgressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressBar.show();
        new LoadDataTask().execute(0);
    }

    public void loadData() {
        Log.d(LOG_TAG, "loadData-Methode gestartet.");
        int leftRowMargin=0;
        int topRowMargin=0;
        int rightRowMargin=0;
        int bottomRowMargin = 0;
        int textSize = 0, smallTextSize =0, mediumTextSize = 0;

        textSize = (int) getResources().getDimension(R.dimen.font_size_verysmall);
        smallTextSize = (int) getResources().getDimension(R.dimen.font_size_small);
        mediumTextSize = (int) getResources().getDimension(R.dimen.font_size_medium);

        _mapTableRowToGameId.clear();

        gameList = _dataSource.getAllSpiele();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        DecimalFormat decimalFormat = new DecimalFormat("0.0");

        int rows = gameList.size(); //  data.length;
        getSupportActionBar().setTitle("Spiele (" + String.valueOf(rows) + ")");
        Log.d(LOG_TAG, "Anzahl Spiele = " + String.valueOf(rows));
        TextView textSpacer = null;
        mTableLayout.removeAllViews();
        // -1 means heading row
        for(int i = -1; i < rows; i ++) {
            Spiel row = null;
            if (i > -1) {
                row = gameList.get(i);
                Log.d(LOG_TAG, "Spiel " + String.valueOf(row.getId()) + " mit Titel " + row.getTitle());
                //row = data[i];
            } else {
                textSpacer = new TextView(this);
                textSpacer.setText("");
            }

            // data columns
            final TextView tv = new TextView(this);
            tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tv.setGravity(Gravity.LEFT);
            tv.setPadding(5, 15, 0, 15);
            if (i == -1) {
                tv.setText("Id");
                tv.setBackgroundColor(Color.parseColor("#f0f0f0"));
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
            } else {
                tv.setBackgroundColor(Color.parseColor("#f8f8f8"));
                tv.setText(String.valueOf(row.getId()));
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
            }

            final TextView tv2 = new TextView(this);
            if (i == -1) {
                tv2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT));
                tv2.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
            } else {
                tv2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.MATCH_PARENT));
                tv2.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
            }

            tv2.setGravity(Gravity.LEFT);
            tv2.setPadding(5, 15, 0, 15);
            if (i == -1) {
                tv2.setText("Name");
                tv2.setBackgroundColor(Color.parseColor("#f7f7f7"));
            }else {
                tv2.setBackgroundColor(Color.parseColor("#ffffff"));
                tv2.setTextColor(Color.parseColor("#000000"));
                tv2.setText(row.getTitle());
                //tv2.setText(dateFormat.format(row.getTitle()));
            }


            final TextView tv3 = new TextView(this);
            tv3.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tv3.setGravity(Gravity.RIGHT);
            tv3.setPadding(5, 15, 0, 15);
            if (i == -1) {
                tv3.setText("Min");
                tv3.setBackgroundColor(Color.parseColor("#f0f0f0"));
                tv3.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
            } else {
                tv3.setBackgroundColor(Color.parseColor("#f8f8f8"));
                tv3.setText(row.getMinNumPlayersString());
                tv3.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
            }

            final TextView tv4 = new TextView(this);
            tv4.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tv4.setGravity(Gravity.RIGHT);
            tv4.setPadding(5, 15, 0, 15);
            if (i == -1) {
                tv4.setText("Max");
                tv4.setBackgroundColor(Color.parseColor("#f0f0f0"));
                tv4.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
            } else {
                tv4.setBackgroundColor(Color.parseColor("#f8f8f8"));
                tv4.setText(row.getMaxNumPlayersString());
                tv4.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
            }

            final TextView tv5 = new TextView(this);
            tv5.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tv5.setGravity(Gravity.RIGHT);
            tv5.setPadding(5, 15, 0, 15);
            if (i == -1) {
                tv5.setText("Rating");
                tv5.setBackgroundColor(Color.parseColor("#f0f0f0"));
                tv5.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
            } else {
                tv5.setBackgroundColor(Color.parseColor("#f8f8f8"));
                tv5.setText(String.valueOf(row.getAverageRating()));
                tv5.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
            }

            // add table row
            final TableRow tr = new TableRow(this);
            tr.setId(i + 1);
            TableLayout.LayoutParams trParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT);
            trParams.setMargins(leftRowMargin, topRowMargin, rightRowMargin, bottomRowMargin);
            tr.setPadding(0,0,0,0);
            tr.setLayoutParams(trParams);

            tr.addView(tv);
            tr.addView(tv2);
            tr.addView(tv3);
            tr.addView(tv4);
            tr.addView(tv5);

            if (i > -1) {
                _mapTableRowToGameId.put(tr, row.getId());

                tr.setOnLongClickListener(new View.OnLongClickListener() {
                    public boolean onLongClick(View v) {
                        TableRow tr = (TableRow) v;
                        /*//Attention: First TextView with id is necessary, dangerous
                        TextView idTextView = (TextView)tr.getChildAt(0);
                        String idString = idTextView.getText().toString();
                        Long id = Long.valueOf(idString);*/
                        Long id = _mapTableRowToGameId.get(tr);
                        Spiel game = _dataSource.getGameById(id);

                        Intent myIntent = new Intent(v.getContext(), AddGameActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putLong ("SpielId", game.getId());
                        myIntent.putExtras(bundle);

                        startActivityForResult(myIntent, ADD_GAME_ACTIVITY_RESULT_CODE);

/*                        AlertDialog editGameDialog = createEditGameDialog(game);
                        editGameDialog.show();*/

                        return true;
                    }
                });
            }
            mTableLayout.addView(tr, trParams);

            if (i > -1) {
                // add separator row
                final TableRow trSep = new TableRow(this);
                TableLayout.LayoutParams trParamsSep = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT);
                trParamsSep.setMargins(leftRowMargin, topRowMargin, rightRowMargin, bottomRowMargin);

                trSep.setLayoutParams(trParamsSep);
                TextView tvSep = new TextView(this);
                TableRow.LayoutParams tvSepLay = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT);
                tvSepLay.span = 4;
                tvSep.setLayoutParams(tvSepLay);
                tvSep.setBackgroundColor(Color.parseColor("#d9d9d9"));
                tvSep.setHeight(1);

                trSep.addView(tvSep);
                mTableLayout.addView(trSep, trParamsSep);
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////
    // The params are dummy and not used
    class LoadDataTask extends AsyncTask<Integer, Integer, String> {
        @Override
        protected String doInBackground(Integer... params) {
            try {
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return "Task Completed.";
        }

        @Override
        protected void onPostExecute(String result) {
            mProgressBar.hide();
            loadData();
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
        }
    }


    private AlertDialog createEditGameDialog(final Spiel game) {
        Log.d(LOG_TAG, "started editGameDialog...");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        View dialogsView = inflater.inflate(R.layout.dialog_edit_game, null);

        final EditText editTextNewTitle = (EditText) dialogsView.findViewById(R.id.editText_new_title);
        editTextNewTitle.setText(String.valueOf(game.getTitle()));

        final EditText editTextNewMinPlayers= (EditText) dialogsView.findViewById(R.id.editText_new_minSpieler);
        editTextNewMinPlayers.setText(String.valueOf(game.getMinNumPlayers()));

        final EditText editTextNewMaxPlayers= (EditText) dialogsView.findViewById(R.id.editText_new_maxSpieler);
        editTextNewMaxPlayers.setText(String.valueOf(game.getMaxNumPlayers()));

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
                        game.setTitle(titleString);

                        String minPlayersString = editTextNewMinPlayers.getText().toString();
                        int minPlayers = Integer.parseInt(minPlayersString);
                        game.setMinNumPlayers(minPlayers);

                        String maxPlayersString = editTextNewMaxPlayers.getText().toString();
                        int maxPlayers = Integer.parseInt(maxPlayersString);
                        game.setMaxNumPlayers(maxPlayers);

                        // An dieser Stelle schreiben wir die geänderten Daten in die SQLite Datenbank
                        _dataSource.updateGame(game);

                        //Spiel updatedGame = _dataSource.updateGame(game.getId(), titleString);

/*                        Log.d(LOG_TAG, "Alter Eintrag - ID: " + game.getId() + " Inhalt: " + game.toString());
                        Log.d(LOG_TAG, "Neuer Eintrag - ID: " + updatedGame.getId() + " Inhalt: " + updatedGame.toString());*/

                        startLoadData();
                        //showAllListEntries();
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

   /* private void initializeContextualActionBar() {
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
    }*/


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

        _dataSource.open();
        _dataSourceSpieler.open();
        _dataSourceRatings.open();

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            Log.d(LOG_TAG, "Bundle is not null.");
            int actionId = bundle.getInt("ActionId");
            Log.d(LOG_TAG, "ActionId is " + actionId);
            if( actionId == 1 ) {
                Log.d(LOG_TAG, "Save-Action will be performed...");
                Spiel game;
                long gameId = bundle.getLong("SpielId");
                if (gameId != -1) {
                    //Existing game was modified
                    game = _dataSource.getGameById(gameId);
                } else {
                    //New game shall be created
                    String newTitle = bundle.getString("title", "");
                    game = _dataSource.createSpiel(newTitle);
                }

                //Set min and max number of players
                int defaultPlayerNumber = -1;
                int minPlayers = bundle.getInt("minPlayers", defaultPlayerNumber);
                if (minPlayers != defaultPlayerNumber) {
                    game.setMinNumPlayers((minPlayers));
                }
                int maxPlayers = bundle.getInt("maxPlayers", defaultPlayerNumber);
                if (maxPlayers != defaultPlayerNumber) {
                    game.setMaxNumPlayers((maxPlayers));
                }

                //Set all ratings for this game
                ArrayList<String> initialList = bundle.getStringArrayList("initials");
                ArrayList<Integer> ratingList = bundle.getIntegerArrayList("ratings");
                int numRatings = initialList.size();
                for (int r = 0; r < numRatings; r++) {
                    String shortName = initialList.get(r);
                    Spieler player = _dataSourceSpieler.getPlayerByShortName(shortName);
                    int rating = ratingList.get(r);
                    game.addRating(player, rating);
                    _dataSourceRatings.addRating(player.getId(), game.getId(), rating);
                }

                _dataSource.updateGame(game);
            }
        } else{
            Log.d(LOG_TAG, "onResume: Bundle is null.");
        }

        Log.d(LOG_TAG, "Folgende Einträge sind in der Datenbank vorhanden:");
        startLoadData();
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d(LOG_TAG, "Die Datenquelle wird geschlossen.");
        _dataSource.close();
        _dataSourceSpieler.close();
        _dataSourceRatings.close();
    }
}
