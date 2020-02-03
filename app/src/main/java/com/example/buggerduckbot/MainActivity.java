package com.example.buggerduckbot;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Pair;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.BluetoothConnection;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;
import it.unive.dais.legodroid.lib.util.Prelude;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText mapX = findViewById(R.id.mapX);
        EditText mapY = findViewById(R.id.mapY);

        EditText posX = findViewById(R.id.posX);
        EditText posY = findViewById(R.id.posY);

        Button task1Button, task2Button, task3Button;
        task1Button = findViewById(R.id.task1Btn);
        task2Button = findViewById(R.id.task2Btn);
        task3Button = findViewById(R.id.task3Btn);

        Intent myIntent = new Intent(MainActivity.this, MapActivity.class);

        task1Button.setOnClickListener(e -> this.openMap(myIntent, 1, mapX, mapY, posX, posY));
        task2Button.setOnClickListener(e -> this.openMap(myIntent, 2, mapX, mapY, posX, posY));
        task3Button.setOnClickListener(e -> this.openMap(myIntent, 3, mapX, mapY, posX, posY));
    }

    /**
     * Ritorna true solo se tutti i campi della mappa sono non vuoti
     */
    private boolean validateMapFields(EditText x, EditText y, EditText posx, EditText posy){
        return !(TextUtils.isEmpty(x.getText()) || TextUtils.isEmpty(y.getText()) || TextUtils.isEmpty(posx.getText()) || TextUtils.isEmpty(posy.getText()));
    }

    /**
     * apre la MapActivity con i paramentri giusti
     */
    private void openMap(Intent i, int task, EditText x, EditText y, EditText posx, EditText posy){
        if(this.validateMapFields(x, y, posx, posy)) {
            Toast.makeText(getApplicationContext(),"I campi della mappa non possono essere vuoti", Toast.LENGTH_SHORT).show();
        }else{
            i.putExtra("taskId", task);
            i.putExtra("map", new Map(new Pair<>(Integer.valueOf(x.getText().toString()), Integer.valueOf(y.getText().toString())),
                    new Pair<>(Integer.valueOf(posx.getText().toString()), Integer.valueOf(posy.getText().toString()))));
            startActivity(i);
        }
    }
}



