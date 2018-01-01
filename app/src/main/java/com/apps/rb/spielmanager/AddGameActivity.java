package com.apps.rb.spielmanager;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Anka on 06.12.2017.
 */

public class AddGameActivity extends Activity{
    private static final String LOG_TAG = AddGameActivity.class.getSimpleName();
    private static final int CAMERA_REQUEST = 1888;

    private final int CancelId = 0;
    private final int SaveId = 1;
    private final String emptyString = "";

    File resultingFile;//File of taken picture
    String stringOfPicture = "";

    private ImageButton takePictureButton;
    private Button addRatingButton;
    private TableLayout ratingsTable;

    private Datenbank _dataSource;
    private DatenbankSpieler _dataSourceSpieler;
    private DatenbankRatings _dataSourceRatings;

    long currentGameId;

    MainActivity main;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_game);

        Log.d(LOG_TAG, "create AddGameActivity...");

        _dataSource = new Datenbank(this);
        _dataSourceSpieler = new DatenbankSpieler(this);
        _dataSourceRatings = new DatenbankRatings(this);

        ratingsTable = findViewById(R.id.table_ratings);

        takePictureButton = (ImageButton) findViewById(R.id.button_take_picture);
        createTakePictureButton();

        createCancelButton();

        createSaveButton();

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            Log.d(LOG_TAG, "Bundle is not null.");
            Long spielId = bundle.getLong("SpielId", -1);
            _dataSource.open();
            if (spielId != -1) {
                Spiel game = _dataSource.getGameById(spielId);
                Log.d(LOG_TAG, "AddGameActivity mit Spiel " + game.getTitle() + " geöffnet.");
                currentGameId = game.getId();
                //Fill TextFields with stored values
                EditText editTextTitle = (EditText) findViewById(R.id.editText_newGame_title);
                editTextTitle.setText(game.getTitle());

                EditText editTextMinPlayers = (EditText) findViewById(R.id.editText_newGame_minSpieler);
                editTextMinPlayers.setText(game.getMinNumPlayersString());

                EditText editTextMaxPlayers = (EditText) findViewById(R.id.editText_newGame_maxSpieler);
                editTextMaxPlayers.setText(game.getMaxNumPlayersString());

                stringOfPicture = game.getCoverString();
                if( !stringOfPicture.equals(emptyString) ) {
                    takePictureButton.setImageBitmap(Tools.StringToBitMap(game.getCoverString()));
                }

                loadRatings(game);
            }
            _dataSource.close();
        } else{
            currentGameId = -1;
        }

        createAddRatingButton();

        Log.d(LOG_TAG, "AddGameActivity successfully created.");
    }

    private void loadRatings(Spiel game){
        _dataSourceRatings.open();
        for( Map.Entry<Long,Integer> ratingEntry : _dataSourceRatings.getAllRatings(game).entrySet() ){
            addExistingTableRow(ratingEntry.getKey(), ratingEntry.getValue());
        }
        _dataSourceRatings.close();
    }

    private void addTableRow(){
        Log.d(LOG_TAG, "addTableRow: gestartet...");
        int smallTextSize = (int) getResources().getDimension(R.dimen.font_size_small);

        final Spinner spinnerSpieler = new Spinner(this);
        //spinnerSpieler.setId()
        spinnerSpieler.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        spinnerSpieler.setGravity(Gravity.LEFT);
        spinnerSpieler.setPadding(5, 15, 0, 15);

        List<String> list = new ArrayList<String>();
        _dataSourceSpieler.open();
        List<Spieler> players = _dataSourceSpieler.getAllSpieler();
        for(Spieler player : players){
            list.add(player.getShortName());
        }
        _dataSourceSpieler.close();
        list.add(emptyString);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpieler.setAdapter(dataAdapter);


        final Spinner spinnerRating = new Spinner(this);
        spinnerRating.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        spinnerRating.setGravity(Gravity.CENTER);
        spinnerRating.setPadding(5, 15, 0, 15);

        List<Integer> ratingList = new ArrayList<Integer>();
        for(int i = 1; i <= 10; i++){
            ratingList.add(i);
        }
        ArrayAdapter<Integer> dataAdapterRating = new ArrayAdapter<Integer>(this,
                android.R.layout.simple_spinner_item, ratingList);
        dataAdapterRating.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRating.setAdapter(dataAdapterRating);


       /* final EditText etRating = new EditText(this);
        etRating.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        etRating.setGravity(Gravity.LEFT);
        etRating.setPadding(5, 15, 0, 15);

        etRating.setText( getResources().getString(R.string.rating_default) ); //"10");
        etRating.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);*/

        Log.d(LOG_TAG, "addTableRow: both spinners created.");

        final TableRow tr = new TableRow(this);
        //tr.setId(i + 1);
        TableLayout.LayoutParams trParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT);
        trParams.setMargins(0, 0, 0, 0);
        tr.setPadding(0,0,0,0);
        tr.setLayoutParams(trParams);

        tr.addView(spinnerSpieler);
        //tr.addView(etRating);
        tr.addView(spinnerRating);

        ratingsTable.addView(tr, trParams);
        Log.d(LOG_TAG, "addTableRow: TableRow added.");
    }

    private void addExistingTableRow(long spielerId, int rating){
        Log.d(LOG_TAG, "addExistingTableRow: gestartet...");

        int smallTextSize = (int) getResources().getDimension(R.dimen.font_size_small);

        final Spinner spinnerSpieler = new Spinner(this);
        //spinnerSpieler.setId()
        spinnerSpieler.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        spinnerSpieler.setGravity(Gravity.LEFT);
        spinnerSpieler.setPadding(5, 15, 0, 15);

        _dataSourceSpieler.open();
        Spieler playerOfRating = _dataSourceSpieler.getPlayerById(spielerId);
        List<String> list = new ArrayList<String>();
        List<Spieler> players = _dataSourceSpieler.getAllSpieler();
        for(Spieler p : players){
            list.add(p.getShortName());
        }
        _dataSourceSpieler.close();
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpieler.setAdapter(dataAdapter);
        int spinnerPosition = dataAdapter.getPosition(playerOfRating.getShortName());
        if( spinnerPosition != -1 ) {
            spinnerSpieler.setSelection(spinnerPosition);
        }


        final Spinner spinnerRating = new Spinner(this);
        spinnerRating.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        spinnerRating.setGravity(Gravity.CENTER);
        spinnerRating.setPadding(5, 15, 0, 15);

        List<Integer> ratingList = new ArrayList<Integer>();
        for(int i = 1; i <= 10; i++){
            ratingList.add(i);
        }
        ArrayAdapter<Integer> dataAdapterRating = new ArrayAdapter<Integer>(this,
                android.R.layout.simple_spinner_item, ratingList);
        dataAdapterRating.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRating.setAdapter(dataAdapterRating);
        int spinnerPositionRating = dataAdapterRating.getPosition(rating);
        if( spinnerPositionRating != -1 ) {
            spinnerRating.setSelection(spinnerPositionRating);
        }

       /* final EditText etRating = new EditText(this);
        etRating.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        etRating.setGravity(Gravity.LEFT);
        etRating.setPadding(5, 15, 0, 15);
        etRating.setText(String.valueOf(rating));
        etRating.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);*/

        Log.d(LOG_TAG, "addExistingTableRow: TextView and EditText created.");
        final TableRow tr = new TableRow(this);
        //tr.setId(i + 1);
        TableLayout.LayoutParams trParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT);
        trParams.setMargins(0, 0, 0, 0);
        tr.setPadding(0,0,0,0);
        tr.setLayoutParams(trParams);

        //tr.addView(tvSpieler);
        tr.addView(spinnerSpieler);
        //tr.addView(etRating);
        tr.addView(spinnerRating);

        ratingsTable.addView(tr, trParams);
        Log.d(LOG_TAG, "addExistingTableRow: TableRow added.");
    }

    private void createAddRatingButton(){
        addRatingButton = (Button) findViewById(R.id.button_AddRating);
        addRatingButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Log.d(LOG_TAG, "addRatingButton clicked.");
                addTableRow();
            }});
    }

    private void createTakePictureButton(){
        takePictureButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Log.d(LOG_TAG, "takePictureButton clicked.");
                checkPermissionsAndTakePhoto();
            }});
    }

    private void takePicture(){
       /* File folder = new File(Environment.getExternalStorageDirectory().toString()+"/ImagesFolder/");
        folder.mkdirs();
        resultingFile = new File(folder, "image.jpg"); //TODO: decide name of picture
        Uri uriSavedImage = FileProvider.getUriForFile(AddGameActivity.this,
                BuildConfig.APPLICATION_ID + ".spielmanager.provider", resultingFile);
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uriSavedImage);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);*/

        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            //If we do not putExtra on the cameraIntent, the following code works:
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            photo = Tools.scaleBitmapToViewSize(photo, 400);
            takePictureButton.setImageBitmap(photo);
            stringOfPicture = Tools.BitMapToString(photo);

/*            //coolere Alternative with path
            Bitmap testPhoto = BitmapFactory.decodeFile(resultingFile.getPath());
            testPhoto = Tools.scaleBitmapToViewSize(testPhoto, dpToPx(200));
            Log.d(LOG_TAG, "Photo successfully set from path!");
            takePictureButton.setImageBitmap(testPhoto);*/
        }
    }

    private int dpToPx(int dp) {
        float density = getApplicationContext().getResources().getDisplayMetrics().density;
        return Math.round((float)dp * density);
    }

    private void checkPermissionsAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
            Log.d(LOG_TAG, "new Permissions requested.");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 110);
        } else {
            takePicture();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 110) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                takePicture();
            }
        }
    }

    private void createCancelButton(){
        Button cancel = (Button) findViewById(R.id.button_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Log.d(LOG_TAG, "cancelButton clicked.");
                Intent intent = new Intent(AddGameActivity.this, MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("ActionId", CancelId);
                Log.d(LOG_TAG, "ActionId set to " + CancelId);
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
                startActivity(intent);
            }
        });
    }

    private void createSaveButton(){
        Button save = (Button) findViewById(R.id.button_saveGame);
        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //read title
                Log.d(LOG_TAG, "saveButton clicked.");
                EditText editTextTitle = (EditText) findViewById(R.id.editText_newGame_title);
                String titleString = editTextTitle.getText().toString();
                if(TextUtils.isEmpty(titleString)) {
                    editTextTitle.setError(getString(R.string.editText_errorMessage));
                    return;
                }
                editTextTitle.setText(emptyString);

                //read minPlayers
                EditText editTextMinPlayers = (EditText) findViewById(R.id.editText_newGame_minSpieler);
                String minPlayersString = editTextMinPlayers.getText().toString();
                int minPlayers = -1;
                if( !minPlayersString.equals(emptyString)){
                    minPlayers = Integer.parseInt(minPlayersString);
                }
                editTextMinPlayers.setText(emptyString);
                //read maxPlayers
                EditText editTextMaxPlayers = (EditText) findViewById(R.id.editText_newGame_maxSpieler);
                String maxPlayersString = editTextMaxPlayers.getText().toString();
                int maxPlayers = -1;
                if( !maxPlayersString.equals(emptyString)){
                    maxPlayers = Integer.parseInt(maxPlayersString);
                }
                editTextMaxPlayers.setText(emptyString);

                Intent intent = new Intent(AddGameActivity.this, MainActivity.class);

                Bundle bundle = new Bundle();
                bundle.putLong("SpielId", currentGameId);
                bundle.putString("title", titleString);
                bundle.putInt("minPlayers", minPlayers);
                bundle.putInt("maxPlayers", maxPlayers);

                if( ! stringOfPicture.equals(emptyString)) {
                    bundle.putString("cover", stringOfPicture);
                    Log.d(LOG_TAG, "createSaveButton: stringOfPicture stored in bundle.");
                }

                ArrayList<String> listOfInitials = new ArrayList<String>();
                ArrayList<Integer> listOfRatings = new ArrayList<Integer>();
                //extract ratings
                _dataSourceSpieler.open();
                _dataSourceRatings.open();
                for(int i = 0, j = ratingsTable.getChildCount(); i < j; i++) {
                    View currentView = ratingsTable.getChildAt(i);
                    if (currentView instanceof TableRow) {
                        TableRow row = (TableRow) currentView;

                        Spinner playerSpinner = (Spinner) row.getChildAt(0);
                        String playerInitials = playerSpinner.getSelectedItem().toString();

                        Spinner ratingSpinner = (Spinner) row.getChildAt(1);
                        String ratingString =ratingSpinner.getSelectedItem().toString();
                        int rating = Integer.parseInt(ratingString);
                        if( !playerInitials.equals(emptyString) ) {
                            listOfInitials.add(playerInitials);
                            listOfRatings.add(rating);
                        }
                        Log.d(LOG_TAG, "Rating: " + playerInitials + " rated " + String.valueOf(rating));
                    }
                }
                bundle.putStringArrayList("initials", listOfInitials);
                bundle.putIntegerArrayList("ratings", listOfRatings);

                bundle.putInt("ActionId", SaveId);
                Log.d(LOG_TAG, "ActionId set to " + SaveId);

                intent.putExtras(bundle);

                setResult(RESULT_OK, intent);
                finish();
                startActivity(intent);
            }
        });
    }




}
