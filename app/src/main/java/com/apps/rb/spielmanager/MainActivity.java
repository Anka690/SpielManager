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
import android.widget.ImageView;
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

//TODO: Cover-Foto speichern und anzeigen
//TODO: Filter-Möglichkeiten hinzufügen
//TODO: Layouts schön machen

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

    private Bundle bundle;
    private Spiel currentGame;

    private Boolean isFiltered;

    private Boolean isTitleSortedDescending = false;
    private Boolean isMinPlayersSortedDescending = false;
    private Boolean isMaxPlayersSortedDescending = false;
    private Boolean isRatingSortedDescending = true;

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

        isFiltered = false;
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
                startActivityForResult(myIntent, ADD_GAME_ACTIVITY_RESULT_CODE);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(LOG_TAG, "onNewIntent: gestartet...");
        setIntent(intent);
    }

    public void startLoadData() {
        mProgressBar.setCancelable(false);
        mProgressBar.setMessage("Lädt die Spiele...");
        mProgressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressBar.show();
        new LoadDataTask().execute(0);
    }

    public void loadGamesWithFilter(int playerNumber) {
        Log.d(LOG_TAG, "loadGamesWithFilter-Methode gestartet.");
        gameList = _dataSource.getAllSpieleWithFilter(playerNumber);
        showGames();
    }

    public void loadAllGames(){
        Log.d(LOG_TAG, "loadAllGames-Methode gestartet.");
        gameList = _dataSource.getAllSpiele();
        showGames();
    }

    public void loadAllGamesSorted(String sortingColumn, Boolean descending){
        Log.d(LOG_TAG, "loadAllGames-Methode gestartet.");
        gameList = _dataSource.getAllSpieleSorted(sortingColumn, descending);
        showGames();
    }

    public void showGames() {
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

        /*SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        DecimalFormat decimalFormat = new DecimalFormat("0.0");*/

        int rows = gameList.size(); //  data.length;
        getSupportActionBar().setTitle("Spiele (" + String.valueOf(rows) + ")");
        Log.d(LOG_TAG, "Anzahl Spiele = " + String.valueOf(rows));
        TextView textSpacer = null;
        mTableLayout.removeAllViews();

        createHeaders();

        // iterate over all games
        for(int i = 0; i < rows; i ++) {
            Spiel row = gameList.get(i);
            Log.d(LOG_TAG, "Spiel " + String.valueOf(row.getId()) + " mit Titel " + row.getTitle());

            final TextView tv2 = new TextView(this);
            tv2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.MATCH_PARENT));
            tv2.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
            tv2.setGravity(Gravity.LEFT);
            tv2.setPadding(5, 15, 0, 15);
            tv2.setBackgroundColor(Color.parseColor("#ffffff"));
            tv2.setTextColor(Color.parseColor("#000000"));
            tv2.setText(row.getTitle());

            final ImageView iv2 = new ImageView(this);
            iv2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.MATCH_PARENT));
            iv2.setPadding(5, 15, 0, 15);
            String coverString = row.getCoverString();
            if( !coverString.equals("")) {
                iv2.setImageBitmap(Tools.StringToBitMap(row.getCoverString()));
            }

            final TextView tv3 = new TextView(this);
            tv3.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tv3.setGravity(Gravity.RIGHT);
            tv3.setPadding(5, 15, 0, 15);
            tv3.setBackgroundColor(Color.parseColor("#f8f8f8"));
            tv3.setText(row.getMinNumPlayersString());
            tv3.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);

            final TextView tv4 = new TextView(this);
            tv4.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tv4.setGravity(Gravity.RIGHT);
            tv4.setPadding(5, 15, 0, 15);
            tv4.setBackgroundColor(Color.parseColor("#f8f8f8"));
            tv4.setText(row.getMaxNumPlayersString());
            tv4.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);

            final TextView tv5 = new TextView(this);
            tv5.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tv5.setGravity(Gravity.RIGHT);
            tv5.setPadding(5, 15, 0, 15);
            tv5.setBackgroundColor(Color.parseColor("#f8f8f8"));
            tv5.setText(row.getAverageRatingToString());
            tv5.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);

            // add table row
            final TableRow tr = new TableRow(this);
            tr.setId(i + 1);
            TableLayout.LayoutParams trParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT);
            trParams.setMargins(leftRowMargin, topRowMargin, rightRowMargin, bottomRowMargin);
            tr.setPadding(0,0,0,0);
            tr.setLayoutParams(trParams);

            //tr.addView(tv);
            tr.addView(tv2);
            tr.addView(iv2);
            tr.addView(tv3);
            tr.addView(tv4);
            tr.addView(tv5);

            _mapTableRowToGameId.put(tr, row.getId());

            tr.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    TableRow tr = (TableRow) v;
                    Long id = _mapTableRowToGameId.get(tr);
                    Spiel game = _dataSource.getGameById(id);

                    Intent myIntent = new Intent(v.getContext(), AddGameActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putLong("SpielId", game.getId());
                    myIntent.putExtras(bundle);

                    startActivityForResult(myIntent, ADD_GAME_ACTIVITY_RESULT_CODE);

/*                        AlertDialog editGameDialog = createEditGameDialog(game);
                        editGameDialog.show();*/
                    return true;
                }
            });
            mTableLayout.addView(tr, trParams);

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

    private void createHeaders(){
        int smallTextSize = (int) getResources().getDimension(R.dimen.font_size_small);

        final TextView tv2 = new TextView(this);
        tv2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
        tv2.setGravity(Gravity.LEFT);
        tv2.setPadding(5, 15, 0, 15);
        tv2.setText("Name");
        tv2.setBackgroundColor(Color.parseColor("#f7f7f7"));
        tv2.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
        tv2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                isTitleSortedDescending = !isTitleSortedDescending;
                loadAllGamesSorted(DatenbankHelper.COLUMN_TITLE, isTitleSortedDescending);
            }
        });

        final TextView tvCover = new TextView(this);
        tvCover.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        tvCover.setGravity(Gravity.CENTER);
        tvCover.setPadding(5, 15, 0, 15);
        tvCover.setText("Cover");
        tvCover.setBackgroundColor(Color.parseColor("#f7f7f7"));
        tvCover.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);

        final TextView tv3 = new TextView(this);
        tv3.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        tv3.setGravity(Gravity.RIGHT);
        tv3.setPadding(5, 15, 0, 15);
        tv3.setText("Min");
        tv3.setBackgroundColor(Color.parseColor("#f0f0f0"));
        tv3.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
        tv3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                isMinPlayersSortedDescending = !isMinPlayersSortedDescending;
                loadAllGamesSorted(DatenbankHelper.COLUMN_MIN_NUM_PLAYERS, isMinPlayersSortedDescending);
            }
        });

        final TextView tv4 = new TextView(this);
        tv4.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        tv4.setGravity(Gravity.RIGHT);
        tv4.setPadding(5, 15, 0, 15);
        tv4.setText("Max");
        tv4.setBackgroundColor(Color.parseColor("#f0f0f0"));
        tv4.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
        tv4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                isMaxPlayersSortedDescending = !isMaxPlayersSortedDescending;
                loadAllGamesSorted(DatenbankHelper.COLUMN_MAX_NUM_PLAYERS, isMaxPlayersSortedDescending);
            }
        });

        final TextView tv5 = new TextView(this);
        tv5.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        tv5.setGravity(Gravity.RIGHT);
        tv5.setPadding(5, 15, 0, 15);
        tv5.setText("Rating");
        tv5.setBackgroundColor(Color.parseColor("#f0f0f0"));
        tv5.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
        tv5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                isRatingSortedDescending = !isRatingSortedDescending;
                loadAllGamesSorted(DatenbankHelper.COLUMN_AVG_RATING, isRatingSortedDescending);
            }
        });

        // add table row
        int leftRowMargin=0;
        int topRowMargin=0;
        int rightRowMargin=0;
        int bottomRowMargin = 0;
        final TableRow tr = new TableRow(this);
        tr.setId(0);
        TableLayout.LayoutParams trParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT);
        trParams.setMargins(leftRowMargin, topRowMargin, rightRowMargin, bottomRowMargin);
        tr.setPadding(0,0,0,0);
        tr.setLayoutParams(trParams);

        //tr.addView(tv);
        tr.addView(tv2);
        tr.addView(tvCover);
        tr.addView(tv3);
        tr.addView(tv4);
        tr.addView(tv5);

        mTableLayout.addView(tr, trParams);
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
            loadAllGames();
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

                        startLoadData();
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


        MenuItem filterMenuItem = menu.findItem(R.id.action_filter);
        filterMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //YourActivity.this.someFunctionInYourActivity();
                Log.d(LOG_TAG, "Filter-MenuItem clicked...");

                if(isFiltered){
                    AlertDialog alert = createDeleteFilterGameDialog();
                    alert.show();
                } else{
                    AlertDialog alert = createFilterGameDialog();
                    alert.show();
                    //item.setIcon(getResources().getDrawable(R.drawable.ic_filter_deactivate);
                }

                return true;
            }
        });

        return true;
    }


    private AlertDialog createFilterGameDialog() {
        Log.d(LOG_TAG, "started createFilterGameDialog...");
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        final EditText edittext = new EditText(this);
        alert.setMessage("Wählen Sie die gewünschte Spieleranzahl");
        alert.setTitle("Filter einrichten");

        alert.setView(edittext);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String numPlayers = edittext.getText().toString();
                Log.d(LOG_TAG, "Filtere Spiele für "+ numPlayers + " Spieler.");
                isFiltered = true;
                loadGamesWithFilter(Integer.valueOf(numPlayers));
            }
        });

        alert.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
                isFiltered = false;
            }
        });

        return alert.create();
    }

    private AlertDialog createDeleteFilterGameDialog() {
        Log.d(LOG_TAG, "started createDeleteFilterGameDialog...");
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setMessage("Möchten Sie den eingestellten Filter zurücknehmen?");
        alert.setTitle("Filter löschen");

        alert.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                isFiltered = false;
                startLoadData();
            }
        });

        alert.setNegativeButton("Nein", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
                isFiltered = true;
            }
        });

        return alert.create();
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
        } else if( id == R.id.action_reset){
            AlertDialog alert = createResetGamesDialog();
            alert.show();
        } else if( id == R.id.action_manage_players){
            Intent myIntent = new Intent(MainActivity.this, PlayerManagementActivity.class);
            startActivity(myIntent);
        }

        return super.onOptionsItemSelected(item);
    }


    private AlertDialog createResetGamesDialog() {
        Log.d(LOG_TAG, "started createResetGamesDialog...");
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        final EditText edittext = new EditText(this);
        alert.setMessage("Geben Sie das Passwort zum Löschen aller Spiele ein!");
        alert.setTitle("Spieledatenbank zurücksetzen");

        alert.setView(edittext);

        alert.setPositiveButton("Alle Spiele löschen", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String password = edittext.getText().toString();
                Log.d(LOG_TAG, "Eingegebenes Passwort: " + password);
                if( password.equals("SM_Delete")){
                    _dataSource.reset();
                    _dataSourceRatings.reset();
                    startLoadData();
                } else{
                    Log.d(LOG_TAG, "Passwort ist falsch.");
                }
            }
        });

        alert.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
            }
        });

        return alert.create();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(LOG_TAG, "onResume-Methode der MainActivity wird ausgeführt.");

        _dataSource.open();
        _dataSourceSpieler.open();
        _dataSourceRatings.open();

        bundle = getIntent().getExtras();
        if(bundle != null) {
            Log.d(LOG_TAG, "Bundle is not null.");
            int actionId = bundle.getInt("ActionId");
            Log.d(LOG_TAG, "ActionId is " + actionId);
            if( actionId == 1 ) {
                Log.d(LOG_TAG, "Save-Action will be performed...");
                long gameId = bundle.getLong("SpielId");
                if (gameId != -1) {
                    //Existing game was modified
                    Log.d(LOG_TAG, "Existing game was modified...");
                    currentGame = _dataSource.getGameById(gameId);
                    updateGameFromBundle();
                } else {
                    //New game shall be created
                    String newTitle = bundle.getString("title", "");

                    currentGame = _dataSource.getGameByTitle(newTitle);
                    if( currentGame == null ){
                        currentGame = _dataSource.createSpiel(newTitle);
                        updateGameFromBundle();
                    } else {
                        Log.d(LOG_TAG, "Spiel" + newTitle + " existiert bereits! AlertDialog...");
                        // 1. Erzeugen eines AlertDialog.Builder Objekts mit dem Konstruktor
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        // 2. Aneinanderreihen mehrerer Setter-Methoden um den Dialog zu bauen
                        builder.setMessage("Das Spiel " + newTitle + " existiert bereits. Sollen die Eigenschaften des gespeicherten Spiels ergänzt werden?");
                        //.setTitle(R.string.dialog_title);
                        // Hinzufügen der Buttons
                        builder.setPositiveButton(R.string.dialog_button_positive, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Log.d(LOG_TAG, "Eigenschaften des gespeicherten Spiels sollen ergänzt werden...");
                                dialog.dismiss();
                                updateGameFromBundle();
                            }
                        });
                        builder.setNegativeButton(R.string.dialog_button_negative, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Log.d(LOG_TAG, "Eigenschaften des gespeicherten Spiels sollen NICHT ergänzt werden...");
                                dialog.cancel();
                                Log.d(LOG_TAG, "Folgende Einträge sind in der Datenbank vorhanden:");
                                startLoadData();
                            }
                        });
                        // 3. Erzeugen des AlertDialogs mit der create() Methode
                        AlertDialog alertDialog = builder.create();
                        // 4. Anzeigen des AlertDialogs mit der show() Methode
                        alertDialog.show();
                    }
                }
            }
        } else{
            Log.d(LOG_TAG, "onResume: Bundle is null.");
        }
    }

    private void updateGameFromBundle(){
        //Set min and max number of players
        int defaultPlayerNumber = -1;
        int minPlayers = bundle.getInt("minPlayers", defaultPlayerNumber);
        if (minPlayers != defaultPlayerNumber) {
            currentGame.setMinNumPlayers((minPlayers));
        }
        int maxPlayers = bundle.getInt("maxPlayers", defaultPlayerNumber);
        if (maxPlayers != defaultPlayerNumber) {
            currentGame.setMaxNumPlayers((maxPlayers));
        }
        String coverString = bundle.getString("cover", "");
        currentGame.setCoverString(coverString);

        //Set all ratings for this game
        ArrayList<String> initialList = bundle.getStringArrayList("initials");
        ArrayList<Integer> ratingList = bundle.getIntegerArrayList("ratings");
        int numRatings = initialList.size();
        for (int r = 0; r < numRatings; r++) {
            String shortName = initialList.get(r);
            Spieler player = _dataSourceSpieler.getPlayerByShortName(shortName);
            int rating = ratingList.get(r);
            currentGame.addRating(player, rating);
            _dataSourceRatings.addRating(player.getId(), currentGame.getId(), rating);
        }

        _dataSource.updateGame(currentGame);
        currentGame = null;
        //clear intent so that no game is added twice when the app is put in background and reopened
        Intent intent = new Intent();
        setIntent(intent);

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
