package com.apps.rb.spielmanager;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
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
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Anka on 06.12.2017.
 */

public class AddGameActivity extends Activity {
    private static final String LOG_TAG = AddGameActivity.class.getSimpleName();
    private static final int CAMERA_REQUEST = 1888;

    private final int CancelId = 0;
    private final int SaveId = 1;
    private final String emptyString = "";

    File resultingFile;//File of taken picture
    String stringOfPicture = "";

    private String gameTitle = "";

    EditText editTextMinPlayers;
    EditText editTextMaxPlayers;

    private ImageButton takePictureButton;
    private Button addRatingButton;
    private TableLayout ratingsTable;

    private Button infoFromInternetButton;
    public ArrayAdapter<String> myGameListAdapter;

    private Datenbank _dataSource;
    private DatenbankSpieler _dataSourceSpieler;
    private DatenbankRatings _dataSourceRatings;

    long currentGameId;

    MainActivity main;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_game);

        Log.d(LOG_TAG, "create AddGameActivity...");

        _dataSource = new Datenbank(this);
        _dataSourceSpieler = new DatenbankSpieler(this);
        _dataSourceRatings = new DatenbankRatings(this);

        ratingsTable = findViewById(R.id.table_ratings);
        editTextMinPlayers = findViewById(R.id.editText_newGame_minSpieler);
        editTextMaxPlayers = findViewById(R.id.editText_newGame_maxSpieler);

        takePictureButton = (ImageButton) findViewById(R.id.button_take_picture);
        createTakePictureButton();

        infoFromInternetButton = (Button) findViewById(R.id.button_infoFromInternet);
        createGetInfoFromInternetButton();

        createCancelButton();

        createSaveButton();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
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
                if (!stringOfPicture.equals(emptyString)) {
                    Bitmap photo = Tools.StringToBitMap(game.getCoverString());
                    photo = Tools.scaleBitmapToViewSize(photo, 400);
                    takePictureButton.setImageBitmap(photo);
                }

                loadRatings(game);
            }
            _dataSource.close();
        } else {
            currentGameId = -1;
        }

        createAddRatingButton();

        Log.d(LOG_TAG, "AddGameActivity successfully created.");
    }

    private void loadRatings(Spiel game) {
        _dataSourceRatings.open();
        for (Map.Entry<Long, Integer> ratingEntry : _dataSourceRatings.getAllRatings(game).entrySet()) {
            addExistingTableRow(ratingEntry.getKey(), ratingEntry.getValue());
        }
        _dataSourceRatings.close();
    }

    private void addTableRow() {
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
        List<Spieler> players = _dataSourceSpieler.getAllPlayers();
        for (Spieler player : players) {
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
        for (int i = 1; i <= 10; i++) {
            ratingList.add(i);
        }
        ArrayAdapter<Integer> dataAdapterRating = new ArrayAdapter<Integer>(this,
                android.R.layout.simple_spinner_item, ratingList);
        dataAdapterRating.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRating.setAdapter(dataAdapterRating);

        Log.d(LOG_TAG, "addTableRow: both spinners created.");

        final TableRow tr = new TableRow(this);
        TableLayout.LayoutParams trParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT);
        trParams.setMargins(0, 0, 0, 0);
        tr.setPadding(0, 0, 0, 0);
        tr.setLayoutParams(trParams);

        tr.addView(spinnerSpieler);
        tr.addView(spinnerRating);

        ratingsTable.addView(tr, trParams);
        Log.d(LOG_TAG, "addTableRow: TableRow added.");
    }

    private void addExistingTableRow(long spielerId, int rating) {
        Log.d(LOG_TAG, "addExistingTableRow: gestartet...");

        final Spinner spinnerSpieler = new Spinner(this);
        //spinnerSpieler.setId()
        spinnerSpieler.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        spinnerSpieler.setGravity(Gravity.LEFT);
        spinnerSpieler.setPadding(5, 15, 0, 15);

        _dataSourceSpieler.open();
        Spieler playerOfRating = _dataSourceSpieler.getPlayerById(spielerId);
        List<String> list = new ArrayList<String>();
        List<Spieler> players = _dataSourceSpieler.getAllPlayers();
        for (Spieler p : players) {
            list.add(p.getShortName());
        }
        _dataSourceSpieler.close();
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpieler.setAdapter(dataAdapter);
        int spinnerPosition = dataAdapter.getPosition(playerOfRating.getShortName());
        if (spinnerPosition != -1) {
            spinnerSpieler.setSelection(spinnerPosition);
        }

        final Spinner spinnerRating = new Spinner(this);
        spinnerRating.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        spinnerRating.setGravity(Gravity.CENTER);
        spinnerRating.setPadding(5, 15, 0, 15);

        List<Integer> ratingList = new ArrayList<Integer>();
        for (int i = 1; i <= 10; i++) {
            ratingList.add(i);
        }
        ArrayAdapter<Integer> dataAdapterRating = new ArrayAdapter<Integer>(this,
                android.R.layout.simple_spinner_item, ratingList);
        dataAdapterRating.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRating.setAdapter(dataAdapterRating);
        int spinnerPositionRating = dataAdapterRating.getPosition(rating);
        if (spinnerPositionRating != -1) {
            spinnerRating.setSelection(spinnerPositionRating);
        }

        final TableRow tr = new TableRow(this);
        //tr.setId(i + 1);
        TableLayout.LayoutParams trParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT);
        trParams.setMargins(0, 0, 0, 0);
        tr.setPadding(0, 0, 0, 0);
        tr.setLayoutParams(trParams);

        tr.addView(spinnerSpieler);
        tr.addView(spinnerRating);

        ratingsTable.addView(tr, trParams);
        Log.d(LOG_TAG, "addExistingTableRow: TableRow added.");
    }

    private void createAddRatingButton() {
        addRatingButton = (Button) findViewById(R.id.button_AddRating);
        addRatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG, "addRatingButton clicked.");
                addTableRow();
            }
        });
    }

    private void createTakePictureButton() {
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG, "takePictureButton clicked.");
                checkPermissionsAndTakePhoto();
            }
        });
    }

    private void checkPermissionsAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG_TAG, "new Permissions requested.");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 110);
        } else {
            takePicture();
        }
    }

    private void takePicture() {
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
            Log.d(LOG_TAG, "Size of original picture: strLength =" + Tools.BitMapToString(photo).length() + ", w: " + photo.getWidth() + ", h: " +photo.getHeight() );
            stringOfPicture = Tools.BitMapToString(photo);
            Log.d(LOG_TAG, "Length of picture string: " + stringOfPicture.length());

            photo = Tools.scaleBitmapToViewSize(photo, 400);
            Log.d(LOG_TAG, "Length of picture as string after scaling: " + Tools.BitMapToString(photo).length() + "w: " + photo.getWidth() + ", h: " +photo.getHeight() );
            takePictureButton.setImageBitmap(photo);
/*            //coolere Alternative with path
            Bitmap testPhoto = BitmapFactory.decodeFile(resultingFile.getPath());
            testPhoto = Tools.scaleBitmapToViewSize(testPhoto, dpToPx(200));
            Log.d(LOG_TAG, "Photo successfully set from path!");
            takePictureButton.setImageBitmap(testPhoto);*/
        }
    }

    private int dpToPx(int dp) {
        float density = getApplicationContext().getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 110) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                takePicture();
            }
        } else if (requestCode == 220) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                searchForDataInInternet();
            }
        }
    }

    private void createGetInfoFromInternetButton() {
        infoFromInternetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(LOG_TAG, "infoFromInternetButton clicked.");
                checkPermissionsAndGetWebAccess();
            }
        });
    }

    private void checkPermissionsAndGetWebAccess() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG_TAG, "new Permissions requested.");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 220);
        } else {
            searchForDataInInternet();
        }
    }

    private void searchForDataInInternet() {
        EditText editTextTitle = (EditText) findViewById(R.id.editText_newGame_title);

        WebDataAccessTask webTask = new WebDataAccessTask();
        webTask.execute(editTextTitle.getText().toString());
    }

    private void createCancelButton() {
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

    private void createSaveButton() {
        Button save = (Button) findViewById(R.id.button_saveGame);
        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //read title
                Log.d(LOG_TAG, "saveButton clicked.");
                EditText editTextTitle = (EditText) findViewById(R.id.editText_newGame_title);
                String titleString = editTextTitle.getText().toString();
                if (TextUtils.isEmpty(titleString)) {
                    editTextTitle.setError(getString(R.string.editText_errorMessage));
                    return;
                }
                editTextTitle.setText(emptyString);

                //read minPlayers
                EditText editTextMinPlayers = (EditText) findViewById(R.id.editText_newGame_minSpieler);
                String minPlayersString = editTextMinPlayers.getText().toString();
                int minPlayers = -1;
                if (!minPlayersString.equals(emptyString)) {
                    minPlayers = Integer.parseInt(minPlayersString);
                }
                editTextMinPlayers.setText(emptyString);
                //read maxPlayers
                EditText editTextMaxPlayers = (EditText) findViewById(R.id.editText_newGame_maxSpieler);
                String maxPlayersString = editTextMaxPlayers.getText().toString();
                int maxPlayers = -1;
                if (!maxPlayersString.equals(emptyString)) {
                    maxPlayers = Integer.parseInt(maxPlayersString);
                }
                editTextMaxPlayers.setText(emptyString);

                Intent intent = new Intent(AddGameActivity.this, MainActivity.class);

                Bundle bundle = new Bundle();
                bundle.putLong("SpielId", currentGameId);
                bundle.putString("title", titleString);
                bundle.putInt("minPlayers", minPlayers);
                bundle.putInt("maxPlayers", maxPlayers);

                if (!stringOfPicture.equals(emptyString)) {
                    bundle.putString("cover", stringOfPicture);
                    Log.d(LOG_TAG, "createSaveButton: stringOfPicture stored in bundle.");
                }

                ArrayList<String> listOfInitials = new ArrayList<String>();
                ArrayList<Integer> listOfRatings = new ArrayList<Integer>();
                //extract ratings
                _dataSourceSpieler.open();
                _dataSourceRatings.open();
                for (int i = 0, j = ratingsTable.getChildCount(); i < j; i++) {
                    View currentView = ratingsTable.getChildAt(i);
                    if (currentView instanceof TableRow) {
                        TableRow row = (TableRow) currentView;

                        Spinner playerSpinner = (Spinner) row.getChildAt(0);
                        String playerInitials = playerSpinner.getSelectedItem().toString();

                        Spinner ratingSpinner = (Spinner) row.getChildAt(1);
                        String ratingString = ratingSpinner.getSelectedItem().toString();
                        int rating = Integer.parseInt(ratingString);
                        if (!playerInitials.equals(emptyString)) {
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


    public class WebDataAccessTask extends AsyncTask<String, Integer, String[]> {
        private final String LOG_TAG = WebDataAccessTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... strings) {
            Log.d(LOG_TAG, "Starte WebAccess: " + strings[0]);
            if (strings.length == 0) { // Keine Eingangsparameter erhalten, daher Abbruch
                return null;
            }

            HttpURLConnection httpURLConnection = null;
            BufferedReader bufferedReader = null;

            // Wir konstruieren die Anfrage-URL für unseren Web-Server
            final String BoardGameString = "https://www.boardgamegeek.com/xmlapi/";
            try {
                String searchPart = "search?search=" + URLEncoder.encode(strings[0], "UTF-8") + "&exact=1";
                String searchGameString = BoardGameString + searchPart;
                Log.d(LOG_TAG, "Zusammengesetzter Anfrage-String: " + searchGameString);

                // Die URL-Verbindung und der BufferedReader, werden im finally-Block geschlossen
                Log.d(LOG_TAG, "Try to get url access.");
                URL url = new URL(searchGameString);
                // Aufbau der Verbindung zur YQL Platform
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.connect();
                Log.d(LOG_TAG, "Connection response message: " + httpURLConnection.getResponseMessage());
                if (200 <= httpURLConnection.getResponseCode() && httpURLConnection.getResponseCode() <= 299) {
                    bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                } else {
                    Log.d(LOG_TAG, "Error in creating connection.");
                    return null;
                }

                String line;
                int boardGameId = -1;
                bufferedReader.readLine();
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.contains("objectid")) {
                        String gameIdString = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
                        boardGameId = Integer.parseInt(gameIdString);
                        break; // take first fitting game
                    }
                }

                if (boardGameId != -1) {
                    Log.d(LOG_TAG, "Requested game found under objectid = " + boardGameId);
                    String getStatsString = "https://www.boardgamegeek.com/xmlapi/boardgame/" + boardGameId + "?stats=1";
                    URL urlStats = new URL(getStatsString);
                    HttpURLConnection httpURLConnectionStats = (HttpURLConnection) urlStats.openConnection();
                    Log.d(LOG_TAG, "Connection response message when trying to get Stats: " + httpURLConnectionStats.getResponseMessage());
                    if (200 <= httpURLConnectionStats.getResponseCode() && httpURLConnectionStats.getResponseCode() <= 299) {
                        bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnectionStats.getInputStream()));
                    } else {
                        Log.d(LOG_TAG, "Error in creating connection.");
                        return null;
                    }
                    int lineCounter = 0;
                    int foundInfoCounter = 0;
                    String[] importantInfos = new String[10];
                    while ((line = bufferedReader.readLine()) != null && lineCounter < 10) {
                        if (line.contains("minplayers")) {
                            Log.d(LOG_TAG, "minplayers found.");
                            importantInfos[foundInfoCounter] = line;
                            foundInfoCounter++;
                        } else if( line.contains("maxplayers")){
                            Log.d(LOG_TAG, "maxplayers found.");
                            importantInfos[foundInfoCounter] = line;
                            foundInfoCounter++;
                        }
                        //gameDataStatsXmlString += line + "\n";
                        lineCounter++;
                    }

                    publishProgress(1, 1);
                    return importantInfos;
                }
            } catch (UnsupportedEncodingException e) {
                Log.d(LOG_TAG, "UnsupportedEncodingException-Error ", e);
                return null;
            } catch (IOException e) { // Beim Holen der Daten trat ein Fehler auf, daher Abbruch
                Log.d(LOG_TAG, "IO-Error ", e);
                return null;
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (final IOException e) {
                        Log.d(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            // TODO: Parse die XML-GameStats-Daten professionell mit xml-Parser

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // Auf dem Bildschirm geben wir eine Statusmeldung aus, immer wenn
            // publishProgress(int...) in doInBackground(String...) aufgerufen wird
/*        Toast.makeText(getActivity(), values[0] + " von " + values[1] + " geladen",
                Toast.LENGTH_SHORT).show();*/
        }

        @Override
        protected void onPostExecute(String[] strings) {
            Log.d(LOG_TAG, "onPostExecute gestartet.");
            // Wir löschen den Inhalt des ArrayAdapters und fügen den neuen Inhalt ein
            // Der neue Inhalt ist der Rückgabewert von doInBackground(String...) also
            // der StringArray gefüllt mit Beispieldaten


            if (strings != null) {
                //myGameListAdapter.clear();
                for (String gameInfoString : strings) {
                    if(gameInfoString != null && !gameInfoString.equals("")) {
                        if( gameInfoString.contains("minplayers")){
                            String minPlayers = gameInfoString.substring(gameInfoString.indexOf(">") + 1, gameInfoString.lastIndexOf("<"));
                            Log.d(LOG_TAG, "GameInfo minplayers = " + minPlayers);
                            if( editTextMinPlayers != null) {
                                editTextMinPlayers.setText(minPlayers);
                            }
                        } else if ( gameInfoString.contains("maxplayers")){
                            String maxPlayers = gameInfoString.substring(gameInfoString.indexOf(">") + 1, gameInfoString.lastIndexOf("<"));
                            Log.d(LOG_TAG, "GameInfo maxplayers = " + maxPlayers);
                            if( editTextMaxPlayers != null) {
                                editTextMaxPlayers.setText(maxPlayers);
                            }
                        }
                    }
                }
                Toast.makeText(AddGameActivity.this, "Spieldaten online verifiziert.",
                        Toast.LENGTH_SHORT).show();
            } else{
                Toast.makeText(AddGameActivity.this, "Spiel online nicht gefunden.",
                        Toast.LENGTH_SHORT).show();
            }
        }

        /*private String[] readXmlDataGame(String xmlString) {

            Document doc;
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder db = dbf.newDocumentBuilder();
                InputSource is = new InputSource();
                is.setCharacterStream(new StringReader(xmlString));
                doc = db.parse(is);
            } catch (ParserConfigurationException e) {
                Log.d(LOG_TAG,"Error: " + e.getMessage());
                return null;
            } catch (SAXException e) {
                Log.d(LOG_TAG,"SAXError: " + e.getMessage());
                return null;
            } catch (IOException e) {
                Log.d(LOG_TAG,"IOError: " + e.getMessage());
                return null;
            }

            Element xmlDataGame = doc.getDocumentElement();
            NodeList xmlGames = xmlDataGame.getElementsByTagName("boardgame");

            int numberFoundGames = xmlGames.getLength();
            int numberInfosPerGame = xmlGames.item(0).getChildNodes().getLength();
            Log.d(LOG_TAG, "Number found games: " + numberFoundGames + ", number infos per game: " + numberInfosPerGame);

            String[] ausgabeArray = new String[numberFoundGames];
            String[][] alleAktienDatenArray = new String[numberFoundGames][numberInfosPerGame];

            Node gameInfo;
            String objectIdString;
            for( int i=0; i<numberFoundGames; i++ ) {
                NodeList aktienParameterListe = xmlGames.item(i).getChildNodes();

                for (int j=0; j<numberInfosPerGame; j++) {
                    gameInfo = aktienParameterListe.item(j);
                    objectIdString = gameInfo.getFirstChild().getNodeValue();
                    alleAktienDatenArray[i][j] = objectIdString;
                }

            *//*<boardgame objectid="2860">
                <yearpublished>2001</yearpublished>
                <minplayers>0</minplayers>
                <maxplayers>0</maxplayers>
                <playingtime>10</playingtime>
                    ...
            </boardgame>*//*

                ausgabeArray[i]  = alleAktienDatenArray[i][0];                // symbol
                ausgabeArray[i] += ": " + alleAktienDatenArray[i][4];         // price
                ausgabeArray[i] += " " + alleAktienDatenArray[i][2];          // currency
                ausgabeArray[i] += " (" + alleAktienDatenArray[i][8] + ")";   // percent
                ausgabeArray[i] += " - [" + alleAktienDatenArray[i][1] + "]"; // name

                Log.v(LOG_TAG,"XML Output:" + ausgabeArray[i]);
            }
            return ausgabeArray;
        }*/
    }
}