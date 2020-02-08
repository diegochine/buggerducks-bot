package com.example.buggerduckbot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.BluetoothConnection;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;
import it.unive.dais.legodroid.lib.plugs.UltrasonicSensor;
import it.unive.dais.legodroid.lib.util.Prelude;

public class TestActivity extends AppCompatActivity {


    private TextView stato;
    private TextView val_sens;

    private Robot rb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //roba activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);


        //grafica
        Button connect = findViewById(R.id.connect);
        Button avanti = findViewById(R.id.avanti);
        Button sx = findViewById(R.id.sx);
        Button dx = findViewById(R.id.dx);
        Button stop = findViewById(R.id.stop);
        Button special = findViewById(R.id.special);
        stato = findViewById(R.id.stato);
        stato.setText("Non sei connesso");
        val_sens = findViewById(R.id.val_sens);


        //intent
        Intent myIntent = getIntent();

       rb = new Robot(this);

        // Connessione con il robot
        connect.setOnClickListener(e->{
            if(rb.connetiti()){
                stato.setText(R.string.connectionString);
            }else{
                stato.setText(R.string.connectionError);
            }
        });

        avanti.setOnClickListener( e -> rb.avanza() );

        dx.setOnClickListener((e)->{
            rb.punta_dx();
        });

        sx.setOnClickListener((e)->{
            rb.punta_sx();
        });

        stop.setOnClickListener((e)->{
            rb.stop();
        });

        special.setOnClickListener((e)->{
           rb.punta_avanti();
           rb.avanza();
           rb.punta_indietro();
           rb.avanza();
        });
    }
}