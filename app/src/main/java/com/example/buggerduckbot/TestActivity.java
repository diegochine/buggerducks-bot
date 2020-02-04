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

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.BluetoothConnection;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;
import it.unive.dais.legodroid.lib.util.Prelude;

public class TestActivity extends AppCompatActivity {


    private TextView stato;

    private boolean connected;
    private EV3 ev3;
    private TachoMotor motoreDx, motoreSx;

    private Float angolo;
    public interface MyRunnable {
        void run() throws IOException;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);


        Button connect = findViewById(R.id.connect);
        Button avanti = findViewById(R.id.avanti);
        Button sx = findViewById(R.id.sx);
        Button dx = findViewById(R.id.dx);
        Button stop = findViewById(R.id.stop);
        Button special = findViewById(R.id.special);
        stato = findViewById(R.id.stato);
        stato.setText("Non sei connesso");

        //intent
        Intent myIntent = getIntent();

        // Inizializzazione Giroscopio
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor giroscopio = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        HandlerThread thread = new HandlerThread("Sensor thread", Thread.MAX_PRIORITY);
        thread.start();
        Handler handler = new Handler(thread.getLooper());
        connected = false;
        angolo = new Float(0);

        SensorEventListener listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float[] rotationMatrix = new float[16];
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);

                // Remap coordinate system
                float[] remappedRotationMatrix = new float[16];
                SensorManager.remapCoordinateSystem(rotationMatrix,
                        SensorManager.AXIS_X,
                        SensorManager.AXIS_Z,
                        remappedRotationMatrix);

                // Convert to orientations
                float[] orientations = new float[3];
                SensorManager.getOrientation(remappedRotationMatrix, orientations);

                //Convert radiant to degree
                for (int i = 0; i < 3; i++) {
                    orientations[i] = (float) (Math.toDegrees(orientations[i]));
                }

                angolo = orientations[0];
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                //che cazzo ne so
            }
        };

        sensorManager.registerListener(listener, giroscopio, 10000, handler);

        // Connessione con il robot
        connect.setOnClickListener((e) -> {
            try {
                // connect to EV3 via bluetooth
                if (!connected) {
                    BluetoothConnection.BluetoothChannel ch = new BluetoothConnection("DUCK").connect(); // replace with your own brick name

                    ev3 = new EV3(ch);
                    connected = true;
                    stato.setText(R.string.connectionString);


                    Prelude.trap(() -> ev3.run(this::inizialization));
                }
            } catch (IOException ex) {
                stato.setText(R.string.connectionError);
            }
        });

        avanti.setOnClickListener((e)->{
            if(connected){
                vai_avanti();
            }
        });

        dx.setOnClickListener((e)->{
            if(connected){
                gira_dx();
            }
        });

        sx.setOnClickListener((e)->{
            if(connected){
               stato.setText("non posso ancora andare a sinistra");
            }
        });

        stop.setOnClickListener((e)->{
            if(connected){
                stoppa_tutto();
            }
        });

        special.setOnClickListener((e)->{
            if(connected){

                vai_avanti();
                gira_dx();
                try {
                    Thread.sleep(1000);
                }catch (InterruptedException ex){

                }

                gira_dx();
                vai_avanti();

            }
        });
    }

    /**
     * Le nostre funzioni
     * */

    /**
     * Inizializza motori e sensori del robot ad inizio task
     * (al momento solo i motori --> TODO inizializzare i sensori e testare!)
     */
    private void inizialization(EV3.Api api) {
        motoreDx = api.getTachoMotor(EV3.OutputPort.A);
        motoreSx = api.getTachoMotor(EV3.OutputPort.D);

        gestisci_eccezioni(() -> {
            motoreDx.resetPosition();
            motoreSx.resetPosition();

            motoreDx.setType(TachoMotor.Type.LARGE);
            motoreSx.setType(TachoMotor.Type.LARGE);

            motoreDx.setPolarity(TachoMotor.Polarity.FORWARD);
            motoreSx.setPolarity(TachoMotor.Polarity.FORWARD);

            stoppa_tutto();
        });

    }

    private void gestisci_eccezioni(MapActivity.MyRunnable r) {
        try {
            r.run();
        } catch (IOException e) {
            stato.setText(R.string.connectionError);
            connected = false;
        }
    }

    private void vai_avanti() { //di una casella
        gestisci_eccezioni(() -> {
            motoreDx.waitCompletion();
            motoreSx.waitCompletion();

            motoreDx.setTimePower(50, 780, 890, 1000, true);
            motoreSx.setTimePower(50, 780, 890, 1000, true);

            motoreDx.waitCompletion();
            motoreSx.waitCompletion();

            stoppa_tutto();
        });
    }

    private float differenza_angoloDx(float startAngle, float actualAngle) {
        if (startAngle * actualAngle < 0) {//discordi
            if (startAngle > 0) return (180 - startAngle) + (180 + actualAngle);
            else return Math.abs(startAngle) + actualAngle;
        }
        return Math.abs(startAngle - actualAngle);
    }

    private void gira_dx() {
        gestisci_eccezioni(() -> {
            motoreDx.waitCompletion();
            motoreSx.waitCompletion();

            try {
                Thread.sleep(500);
            }catch (InterruptedException ex){}

            float angoloInizale = angolo;

            motoreDx.setPower(-10);
            motoreSx.setPower(10);

            motoreDx.start();
            motoreSx.start();

            while (differenza_angoloDx(angoloInizale, angolo) < 89);

            motoreDx.stop();
            motoreSx.stop();


            stato.setText("so riva more");
        });
    }


    private void stoppa_tutto() {
        gestisci_eccezioni(() -> {
            motoreDx.stop();
            motoreSx.stop();
        });
    }
}