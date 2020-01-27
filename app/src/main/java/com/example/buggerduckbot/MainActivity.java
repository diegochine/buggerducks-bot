package com.example.buggerduckbot;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Pair;
import android.widget.Button;
import android.widget.TextView;

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

        Button task1Button, task2Button, task3Button;
        task1Button = findViewById(R.id.task1Btn);
        task2Button = findViewById(R.id.task2Btn);
        task3Button = findViewById(R.id.task3Btn);

        Intent myIntent = new Intent(MainActivity.this, MapActivity.class);

        task1Button.setOnClickListener(e -> {
            myIntent.putExtra("taskId", 1);
            //myIntent.putExtra("map", new Map());
            startActivity(myIntent);
        });
        task2Button.setOnClickListener(e -> {
            myIntent.putExtra("taskId", 2);
            //myIntent.putExtra("map", new Map());
            startActivity(myIntent);
        });
        task3Button.setOnClickListener(e -> {
            myIntent.putExtra("taskId", 3);
            //myIntent.putExtra("map", new Map());
            startActivity(myIntent);
        });
    }

}



