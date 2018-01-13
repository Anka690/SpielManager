package com.apps.rb.spielmanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Anka on 13.01.2018.
 */

public class PlayerManagementActivity extends AppCompatActivity {
    private static final String LOG_TAG = PlayerManagementActivity.class.getSimpleName();

    private static final int ADD_PLAYER_ACTIVITY_RESULT_CODE = 123;

    private DatenbankSpieler _dataSourceSpieler;

    private TableLayout playersTableLayout;

    private List<Spieler> playersList;
    private static Map<TableRow, Long> _mapTableRowToPlayerId;

    private Bundle bundle;
    private Spieler currentPlayer;

    Boolean isFirstnameSortedDescending = false;
    Boolean isLastnameSortedDescending = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_players);
        Log.d(LOG_TAG, "onCreate-Methode gestartet.");
        _dataSourceSpieler = new DatenbankSpieler(this);
        _dataSourceSpieler.open();

        activateAddButton();

        playersTableLayout = (TableLayout) findViewById(R.id.tablePlayers);
        playersTableLayout.setStretchAllColumns(true);

        _mapTableRowToPlayerId = new HashMap<TableRow, Long>();

        loadAllPlayers();
    }

    @Override
    protected void onPause(){
        super.onPause();
        _dataSourceSpieler.close();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(LOG_TAG, "onNewIntent: gestartet...");
        setIntent(intent);
    }

    private void activateAddButton(){
        Log.d(LOG_TAG, "activateAddButton-Methode gestartet.");
        Button add = (Button) findViewById(R.id.ButtonAddPlayer);
        add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Log.d(LOG_TAG, "AddButton clicked.");
                Intent myIntent = new Intent(view.getContext(), AddPlayerActivity.class);
                startActivityForResult(myIntent, ADD_PLAYER_ACTIVITY_RESULT_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if(requestCode==ADD_PLAYER_ACTIVITY_RESULT_CODE)
        {
           Log.d(LOG_TAG, "onActivityResult from addPlayerActivity.");
           setIntent(data);
        }
    }

    private void loadAllPlayers(){
        Log.d(LOG_TAG, "loadAllPlayers-Methode gestartet.");
        playersList = _dataSourceSpieler.getAllPlayers();
        showPlayers();
    }

    private void loadAllPlayersSorted(String sortingColumn, Boolean descending){
        Log.d(LOG_TAG, "loadAllPlayers-Methode gestartet.");
        playersList = _dataSourceSpieler.getAllPlayersSorted(sortingColumn, descending);
        showPlayers();
    }

    private void showPlayers() {
        Log.d(LOG_TAG, "showPlayers-Methode gestartet.");
        int leftRowMargin=0;
        int topRowMargin=0;
        int rightRowMargin=0;
        int bottomRowMargin = 0;
        int smallTextSize = (int) getResources().getDimension(R.dimen.font_size_small);

        _mapTableRowToPlayerId.clear();

        /*SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        DecimalFormat decimalFormat = new DecimalFormat("0.0");*/

        int players = playersList.size(); //  data.length;
        getSupportActionBar().setTitle("Spieler (" + String.valueOf(players) + ")");
        Log.d(LOG_TAG, "Anzahl Spieler = " + String.valueOf(players));

        playersTableLayout.removeAllViews();

        createHeaders();

        // iterate over all games
        for(int i = 0; i < players; i ++) {
            Spieler player = playersList.get(i);
            Log.d(LOG_TAG, "Spieler " + String.valueOf(player.getId()) + " mit Name " + player.get_firstname());

            final TextView tvFirstname = new TextView(this);
            tvFirstname.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.MATCH_PARENT));
            tvFirstname.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
            tvFirstname.setGravity(Gravity.LEFT);
            tvFirstname.setPadding(5, 15, 0, 15);
            tvFirstname.setBackgroundColor(Color.parseColor("#ffffff"));
            tvFirstname.setTextColor(Color.parseColor("#000000"));
            tvFirstname.setText(player.get_firstname());

            final TextView tvLastname = new TextView(this);
            tvLastname.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tvLastname.setGravity(Gravity.LEFT);
            tvLastname.setPadding(5, 15, 0, 15);
            tvLastname.setBackgroundColor(Color.parseColor("#ffffff"));
            tvLastname.setText(player.get_lastname());
            tvLastname.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);

            final TextView tvShortname = new TextView(this);
            tvShortname.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tvShortname.setGravity(Gravity.LEFT);
            tvShortname.setPadding(5, 15, 0, 15);
            tvShortname.setBackgroundColor(Color.parseColor("#f8f8f8"));
            tvShortname.setText(player.getShortName());
            tvShortname.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);

            // add table row
            final TableRow tr = new TableRow(this);
            tr.setId(i + 1);
            TableLayout.LayoutParams trParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT);
            trParams.setMargins(leftRowMargin, topRowMargin, rightRowMargin, bottomRowMargin);
            tr.setPadding(0,0,0,0);
            tr.setLayoutParams(trParams);

            //tr.addView(tv);
            tr.addView(tvFirstname);
            tr.addView(tvLastname);
            tr.addView(tvShortname);

            _mapTableRowToPlayerId.put(tr, player.getId());

            tr.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    TableRow tr = (TableRow) v;
                    Long id = _mapTableRowToPlayerId.get(tr);
                    Spieler player = _dataSourceSpieler.getPlayerById(id);

                    Intent myIntent = new Intent(v.getContext(), AddPlayerActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putLong("SpielerId", player.getId());
                    myIntent.putExtras(bundle);

                    startActivityForResult(myIntent, ADD_PLAYER_ACTIVITY_RESULT_CODE);
                    return true;
                }
            });
            playersTableLayout.addView(tr, trParams);

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
            playersTableLayout.addView(trSep, trParamsSep);
        }
    }

    private void createHeaders(){
        int smallTextSize = (int) getResources().getDimension(R.dimen.font_size_small);

        final TextView tvFirstnameHeader = new TextView(this);
        tvFirstnameHeader.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        tvFirstnameHeader.setGravity(Gravity.LEFT);
        tvFirstnameHeader.setPadding(5, 15, 0, 15);
        tvFirstnameHeader.setText("Vorname");
        tvFirstnameHeader.setBackgroundColor(Color.parseColor("#f7f7f7"));
        tvFirstnameHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
        tvFirstnameHeader.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                isFirstnameSortedDescending = !isFirstnameSortedDescending;
                loadAllPlayersSorted(DatenbankSpielerHelper.COLUMN_SPIELER_FIRSTNAME, isFirstnameSortedDescending);
            }
        });

        final TextView tvLastnameHeader = new TextView(this);
        tvLastnameHeader.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        tvLastnameHeader.setGravity(Gravity.LEFT);
        tvLastnameHeader.setPadding(5, 15, 0, 15);
        tvLastnameHeader.setText("Nachname");
        tvLastnameHeader.setBackgroundColor(Color.parseColor("#f7f7f7"));
        tvLastnameHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);
        tvLastnameHeader.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                isLastnameSortedDescending = !isLastnameSortedDescending;
                loadAllPlayersSorted(DatenbankSpielerHelper.COLUMN_SPIELER_SURNAME, isLastnameSortedDescending);
            }
        });

        final TextView tvShortnameHeader = new TextView(this);
        tvShortnameHeader.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        tvShortnameHeader.setGravity(Gravity.LEFT);
        tvShortnameHeader.setPadding(5, 15, 0, 15);
        tvShortnameHeader.setText("Kurzname");
        tvShortnameHeader.setBackgroundColor(Color.parseColor("#f0f0f0"));
        tvShortnameHeader.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);

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
        tr.addView(tvFirstnameHeader);
        tr.addView(tvLastnameHeader);
        tr.addView(tvShortnameHeader);

        playersTableLayout.addView(tr, trParams);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(LOG_TAG, "onResume-Methode der PlayerManagementActivity wird ausgeführt.");

        _dataSourceSpieler.open();

        bundle = getIntent().getExtras();
        if (bundle != null) {
            Log.d(LOG_TAG, "Bundle is not null.");
            int actionId = bundle.getInt("ActionId");
            Log.d(LOG_TAG, "ActionId is " + actionId);
            if (actionId == AddPlayerActivity.SaveId) {
                Log.d(LOG_TAG, "Save-Action will be performed...");
                long playerId = bundle.getLong("SpielerId");
                if (playerId != -1) {
                    //Existing player was modified
                    Log.d(LOG_TAG, "Existing player was modified...");
                    currentPlayer = _dataSourceSpieler.getPlayerById(playerId);
                    updatePlayerFromBundle();
                } else {
                    //New player shall be created
                    String newFirstname = bundle.getString("firstname", "");
                    String newLastname = bundle.getString("lastname", "");
                    String newShortname = bundle.getString("shortname", "");

                    _dataSourceSpieler.createSpieler(newShortname, newFirstname, newLastname);
                    //clear intent so that no game is added twice when the app is put in background and reopened
                    Intent intent = new Intent();
                    setIntent(intent);
                    loadAllPlayers();
                }
            }
        } else {
            Log.d(LOG_TAG, "onResume: Bundle is null.");
        }
    }

    private void updatePlayerFromBundle(){
        Log.d(LOG_TAG, "updatePlayerFromBundle");
        currentPlayer.setFirstname(bundle.getString("firstname", ""));
        currentPlayer.setLastname(bundle.getString("lastname", ""));
        currentPlayer.setShortname(bundle.getString("shortname", ""));

        _dataSourceSpieler.updatePlayer(currentPlayer);
        //clear intent so that no game is added twice when the app is put in background and reopened
        Intent intent = new Intent();
        setIntent(intent);
        currentPlayer = null;

        loadAllPlayers();
    }
}
