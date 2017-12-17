package com.apps.rb.spielmanager;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.Color;
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

/**
 * Created by Anka on 06.12.2017.
 */

public class AddGameActivity extends Activity{
    private static final String LOG_TAG = AddGameActivity.class.getSimpleName();
    private static final int CAMERA_REQUEST = 1888;

    File resultingFile;//File of taken picture

    private ImageButton takePictureButton;
    private Button addRatingButton;
    private TableLayout ratingsTable;

    MainActivity main;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_game);

        Log.d(LOG_TAG, "create AddGameActivity...");
        createTakePictureButton();

        createCancelButton();

        createSaveButton();

        ratingsTable = findViewById(R.id.table_ratings);
        createAddRatingButton();

        loadRatings();

        Log.d(LOG_TAG, "AddGameActivity successfully created.");
    }

    private void loadRatings(){
        //TODO: Use this activity also for editing, not only for creating
        //At the moment, there are no ratings to load
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
        list.add("AR");
        list.add("TR");
        list.add("MM");
        list.add("SZ");
        list.add("");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpieler.setAdapter(dataAdapter);

        final EditText etRating = new EditText(this);
        etRating.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        etRating.setGravity(Gravity.LEFT);
        etRating.setPadding(5, 15, 0, 15);

        etRating.setText("10");
        etRating.setTextSize(TypedValue.COMPLEX_UNIT_PX, smallTextSize);

        Log.d(LOG_TAG, "addTableRow: TextView and EditText created.");
        final TableRow tr = new TableRow(this);
        //tr.setId(i + 1);
        TableLayout.LayoutParams trParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT);
        trParams.setMargins(0, 0, 0, 0);
        tr.setPadding(0,0,0,0);
        tr.setLayoutParams(trParams);

        //tr.addView(tvSpieler);
        tr.addView(spinnerSpieler);
        tr.addView(etRating);
        Log.d(LOG_TAG, "addTableRow: TableRow created.");

        ratingsTable.addView(tr, trParams);
        Log.d(LOG_TAG, "addTableRow: TableRow added.");
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
        takePictureButton = (ImageButton) findViewById(R.id.button_take_picture);
        takePictureButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Log.d(LOG_TAG, "takePictureButton clicked.");
                checkPermissionsAndTakePhoto();
            }});
    }

    private void takePicture(){
        File folder = new File(Environment.getExternalStorageDirectory().toString()+"/ImagesFolder/");
        folder.mkdirs();
        resultingFile = new File(folder, "image.jpg"); //TODO: decide name of picture
        Uri uriSavedImage = FileProvider.getUriForFile(AddGameActivity.this,
                BuildConfig.APPLICATION_ID + ".spielmanager.provider", resultingFile);
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uriSavedImage);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);

/*      Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);*/
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
          /*  //If we do not putExtra on the cameraIntent, the following code works:
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            takePictureButton.setImageBitmap(photo);*/

            //coolere Alternative with path
            Bitmap testPhoto = BitmapFactory.decodeFile(resultingFile.getPath());
            Log.d(LOG_TAG, "Photo successfully set from path!");
            takePictureButton.setImageBitmap(testPhoto);
        }
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
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private void createSaveButton(){
        Button save = (Button) findViewById(R.id.button_saveGame);
        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //read title
                EditText editTextTitle = (EditText) findViewById(R.id.editText_newGame_title);
                String titleString = editTextTitle.getText().toString();
                if(TextUtils.isEmpty(titleString)) {
                    editTextTitle.setError(getString(R.string.editText_errorMessage));
                    return;
                }
                editTextTitle.setText("");

                //read minPlayers
                EditText editTextMinPlayers = (EditText) findViewById(R.id.editText_newGame_minSpieler);
                String minPlayersString = editTextMinPlayers.getText().toString();
                int minPlayers = Integer.parseInt(minPlayersString);
                editTextMinPlayers.setText("");
                //read maxPlayers
                EditText editTextMaxPlayers = (EditText) findViewById(R.id.editText_newGame_maxSpieler);
                String maxPlayersString = editTextMaxPlayers.getText().toString();
                int maxPlayers = Integer.parseInt(maxPlayersString);
                editTextMaxPlayers.setText("");

                Intent intent = new Intent(AddGameActivity.this, MainActivity.class);

                Bundle bundle = new Bundle();
                bundle.putString("title", titleString);
                bundle.putInt("minPlayers", minPlayers);
                bundle.putInt("maxPlayers", maxPlayers);

                ArrayList<String> listOfInitials = new ArrayList<String>();
                ArrayList<Integer> listOfRatings = new ArrayList<Integer>();
                //extract ratings
                for(int i = 0, j = ratingsTable.getChildCount(); i < j; i++) {
                    View currentView = ratingsTable.getChildAt(i);
                    if (currentView instanceof TableRow) {
                        TableRow row = (TableRow) currentView;
                        EditText ratingEditText = (EditText) row.getChildAt(1);
                        String ratingString = ratingEditText.getText().toString();
                        int rating = Integer.parseInt(ratingString);

                        Spinner playerSpinner = (Spinner) row.getChildAt(0);
                        String playerInitials = playerSpinner.getSelectedItem().toString();

                        listOfInitials.add(playerInitials);
                        listOfRatings.add(rating);
                        Log.d(LOG_TAG, "Rating: " + playerInitials + " rated " + String.valueOf(rating));
                    }
                }

                bundle.putStringArrayList("initials", listOfInitials);
                bundle.putIntegerArrayList("ratings", listOfRatings);

                intent.putExtras(bundle);
                //intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                setResult(RESULT_OK, intent);
                finish();
                Log.d(LOG_TAG, "Finished.");
                startActivity(intent);
                Log.d(LOG_TAG, "startActivity with intent.");
            }
        });
    }




}
