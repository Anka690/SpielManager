package com.apps.rb.spielmanager;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.graphics.Bitmap;
import android.widget.ImageView;

import java.io.File;

/**
 * Created by Anka on 06.12.2017.
 */

public class AddGameActivity extends Activity{
    private static final String LOG_TAG = AddGameActivity.class.getSimpleName();
    private static final int CAMERA_REQUEST = 1888;

    File resultingFile;//File of taken picture

    private ImageButton takePictureButton;
    private ImageView imageView;

    MainActivity main;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_game);

        createTakePictureButton();

        createCancelButton();

        createSaveButton();
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
        }}

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

    private void createTakePictureButton(){
        takePictureButton = (ImageButton) findViewById(R.id.button_take_picture);
        takePictureButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Log.d(LOG_TAG, "takePictureButton clicked.");
                checkPermissionsAndTakePhoto();
            }});
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
                if(TextUtils.isEmpty(minPlayersString)) {
                    editTextMinPlayers.setError(getString(R.string.editText_errorMessage));
                    return;
                }
                int minPlayers = Integer.parseInt(minPlayersString);
                editTextMinPlayers.setText("");

                EditText editTextMaxPlayers = (EditText) findViewById(R.id.editText_newGame_maxSpieler);
                String maxPlayersString = editTextMaxPlayers.getText().toString();
                if(TextUtils.isEmpty(maxPlayersString)) {
                    editTextMaxPlayers.setError(getString(R.string.editText_errorMessage));
                    return;
                }
                int maxPlayers = Integer.parseInt(maxPlayersString);
                editTextMaxPlayers.setText("");

                Intent intent = new Intent(AddGameActivity.this, MainActivity.class);

                Bundle bundle = new Bundle();
                bundle.putString("title", titleString); //Your id
                intent.putExtras(bundle); //Put your id to your next Intent

                //Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
                startActivity(intent);
            }
        });
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
          /*  //If we do not putExtra on the camereIntent, the following code works:
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            takePictureButton.setImageBitmap(photo);*/

            //coolere Alternative with path
            Bitmap testPhoto = BitmapFactory.decodeFile(resultingFile.getPath());
            Log.d(LOG_TAG, "Photo successfully set from path!");
            takePictureButton.setImageBitmap(testPhoto);
        }
    }

}
