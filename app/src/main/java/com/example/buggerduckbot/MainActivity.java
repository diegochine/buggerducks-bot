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

        EditText mine = findViewById(R.id.mine);

        Button task1Button, task2Button, task3Button;
        task1Button = findViewById(R.id.task1Btn);
        task2Button = findViewById(R.id.task2Btn);
        task3Button = findViewById(R.id.task3Btn);

        //startActivity(new Intent(MainActivity.this, TestActivity.class));

        Intent myIntent = new Intent(MainActivity.this, MapActivity.class);

        task1Button.setOnClickListener(e -> this.openMap(myIntent, 1, mapX, mapY, posX, posY, mine));
        task2Button.setOnClickListener(e -> this.openMap(myIntent, 2, mapX, mapY, posX, posY, mine));
        task3Button.setOnClickListener(e -> this.openMap(myIntent, 3, mapX, mapY, posX, posY, mine));
    }

    /**
     * Ritorna true solo se tutti i campi della mappa sono non vuoti
     */
    private boolean validateMapFields(EditText x, EditText y, EditText posx, EditText posy, EditText mine){
        return !(TextUtils.isEmpty(x.getText()) || TextUtils.isEmpty(y.getText()) || TextUtils.isEmpty(posx.getText()) || TextUtils.isEmpty(posy.getText()) || TextUtils.isEmpty(mine.getText()));
    }

    /**
     * Apre la MapActivity con i paramentri giusti
     */
    private void openMap(Intent i, int task, EditText x, EditText y, EditText posx, EditText posy, EditText mine){
        if(task == 2){
            startActivity(new Intent(MainActivity.this, ReciverActivity.class));
        }

        /*
        if(!this.validateMapFields(x, y, posx, posy, mine)) {
            Toast.makeText(getApplicationContext(),"I campi della mappa non possono essere vuoti", Toast.LENGTH_SHORT).show();
        }else{



            i.putExtra("taskId", task);
            i.putExtra("mine", Integer.valueOf(mine.getText().toString()));
            int n_righe = Integer.valueOf(x.getText().toString());
            int n_colonne = Integer.valueOf(y.getText().toString());
            int riga = Integer.valueOf(posx.getText().toString());
            int colonna = Integer.valueOf(posy.getText().toString());
            i.putExtra("map", new Map(n_righe, n_colonne, riga, colonna));
            startActivity(i);
        }*/
    }
}



