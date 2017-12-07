package com.apps.rb.spielmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.text.TextUtils;
import android.widget.EditText;
/**
 * Created by Anka on 06.12.2017.
 */

public class AddGameActivity extends Activity{

    MainActivity main;
    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_game);

        Button cancel = (Button) findViewById(R.id.button_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });

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
}
