package com.apps.rb.spielmanager;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableRow;

import java.util.ArrayList;

/**
 * Created by Anka on 13.01.2018.
 */

public class AddPlayerActivity extends Activity {
    private static final String LOG_TAG = AddPlayerActivity.class.getSimpleName();

    public static int CancelId = 0;
    public static int SaveId = 1;

    private DatenbankSpieler _dataSourceSpieler;

    EditText editTextFirstname;
    EditText editTextLastname;
    EditText editTextShortname;

    long currentPlayerId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "create AddPlayerActivity...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_player);

        Log.d(LOG_TAG, "create AddPlayerActivity reached here...");

        _dataSourceSpieler = new DatenbankSpieler(this);

        editTextFirstname = findViewById(R.id.editText_firstname);
        editTextLastname = findViewById(R.id.editText_lastname);
        editTextShortname= findViewById(R.id.editText_shortname);

        createCancelButton();
        createSaveButton();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Log.d(LOG_TAG, "Bundle is not null.");
            Long playerId = bundle.getLong("SpielerId", -1);
            _dataSourceSpieler.open();
            if (playerId != -1) {
                Spieler player = _dataSourceSpieler.getPlayerById(playerId);
                Log.d(LOG_TAG, "AddPlayerActivity mit Spieler " + player.getShortName() + " geöffnet.");
                currentPlayerId = player.getId();
                //Fill TextFields with stored values
                editTextFirstname.setText(player.get_firstname());
                editTextLastname.setText(player.get_lastname());
                editTextShortname.setText(player.getShortName());
            }
            _dataSourceSpieler.close();
        } else {
            currentPlayerId = -1;
        }
    }

    private void createCancelButton() {
        Button cancel = (Button) findViewById(R.id.button_cancelPlayer);
        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Log.d(LOG_TAG, "cancelButton clicked.");
                Intent intent = new Intent(AddPlayerActivity.this, PlayerManagementActivity.class);
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
        Button save = (Button) findViewById(R.id.button_savePlayer);
        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //read player
                Log.d(LOG_TAG, "saveButton clicked.");
                String firstnameString = editTextFirstname.getText().toString();
                if (TextUtils.isEmpty(firstnameString)) {
                    editTextFirstname.setError(getString(R.string.editText_errorMessage));
                    return;
                }

                String lastnameString = editTextLastname.getText().toString();
                if (TextUtils.isEmpty(lastnameString)) {
                    editTextLastname.setError(getString(R.string.editText_errorMessage));
                    return;
                }

                String shortnameString = editTextShortname.getText().toString();
                if (TextUtils.isEmpty(shortnameString)) {
                    editTextShortname.setError(getString(R.string.editText_errorMessage));
                    return;
                }

                Intent intent = new Intent(AddPlayerActivity.this, PlayerManagementActivity.class);

                Bundle bundle = new Bundle();
                bundle.putLong("SpielerId", currentPlayerId);
                bundle.putString("firstname", firstnameString);
                bundle.putString("lastname", lastnameString);
                bundle.putString("shortname", shortnameString);
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
