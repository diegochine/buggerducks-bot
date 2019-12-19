package com.example.buggerduckbot;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.BluetoothConnection;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;
import it.unive.dais.legodroid.lib.util.Prelude;

public class MainActivity extends AppCompatActivity {
    Button task1Button, task2Button, task3Button;
    //robe del telefono
    private SensorManager sensorManager;
    private Sensor rvSensor;
    private SensorEventListener rvEventListener;
    private float angolo;
    //robe del robot
    private TachoMotor motoreDx, motoreSx;
    private EV3 ev3;
    //robe della grafica
    private TextView textOutput, connectionString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //roba della grafica
        task1Button = findViewById(R.id.task1Btn);
        task2Button = findViewById(R.id.task2Btn);
        task3Button = findViewById(R.id.task3Btn);
        Button connectButton = findViewById(R.id.connectionBtn);
        textOutput = findViewById(R.id.errori);
        connectionString = findViewById(R.id.connectionString);

        //roba del telefono
        angolo = 0;
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        rvSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        rvEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                //roba copiata da stack overflow
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

                //roba scritta da noi

                angolo = orientations[0];
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                //che cazzo ne so
            }
        };

        sensorManager.registerListener(rvEventListener, rvSensor, sensorManager.SENSOR_DELAY_NORMAL);

        //roba del robot
        connectButton.setOnClickListener((e) -> {
            try {
                // connect to EV3 via bluetooth
                BluetoothConnection.BluetoothChannel ch = new BluetoothConnection("DUCK").connect(); // replace with your own brick name

                ev3 = new EV3(ch);

                connectionString.setText(R.string.connectionString);

                Prelude.trap(() -> ev3.run(this::legoMain));


            } catch (IOException e2) {
                textOutput.setText(R.string.connectionError);
            }
        });


        task1Button.setOnClickListener(e -> vai_avanti());
        task2Button.setOnClickListener(e -> gira_dx());
        task3Button.setOnClickListener(e -> stoppa_tutto());
    }


    protected void legoMain(EV3.Api api){
        motoreDx = api.getTachoMotor(EV3.OutputPort.D);
        motoreSx = api.getTachoMotor(EV3.OutputPort.A);

        try {
            motoreDx.resetPosition();
            motoreSx.resetPosition();

            motoreDx.setType(TachoMotor.Type.LARGE);
            motoreSx.setType(TachoMotor.Type.LARGE);

            motoreDx.setPolarity(TachoMotor.Polarity.BACKWARDS);
            motoreSx.setPolarity(TachoMotor.Polarity.BACKWARDS);
        }catch (IOException e){
            textOutput.setText(R.string.connectionError);
        }
    }


    protected void gestice_eccezioni(MyRunnable r) {
        try {
            r.run();
        } catch (IOException e) {
            textOutput.setText(R.string.connectionError);
        }
    }

    protected void vai_avanti() { //di una casella
        gestice_eccezioni(() -> {
            motoreDx.waitCompletion();
            motoreSx.waitCompletion();

            motoreDx.setTimePower(100, 1000, 2000, 1000, true);
            motoreSx.setTimePower(100, 1000, 2000, 1000, true);
        });
    }

    protected void vai_indietro() { //di una casella
        gestice_eccezioni(() -> {
            motoreDx.waitCompletion();
            motoreSx.waitCompletion();

            motoreDx.setTimePower(-100, 1000, 2000, 1000, true);
            motoreSx.setTimePower(-100, 1000, 2000, 1000, true);
        });
    }


    protected float differenza_angoloDx(float startAngle, float actualAngle) {
        if (startAngle * actualAngle < 0) {
            if (startAngle > 0) return (180 - startAngle) + (180 + actualAngle);
            else return Math.abs(startAngle) + actualAngle;
        }
        return Math.abs(startAngle - actualAngle);
    }


    protected void gira_dx() {
        gestice_eccezioni(() -> {
            //motoreDx.waitCompletion();
            //motoreSx.waitCompletion();

            float angolo0 = angolo;
            /*
            motoreDx.setSpeed(25);
            motoreSx.setSpeed(25);
            motoreDx.start();
            motoreSx.start();
            while(differenza_angoloDx(angolo0, angolo)<10); //idle
            motoreDx.setSpeed(-50);
            motoreSx.setSpeed(50);
            while(differenza_angoloDx(angolo0, angolo)<20); //idle
            motoreDx.setSpeed(-75);
            motoreSx.setSpeed(75);
            while(differenza_angoloDx(angolo0, angolo)<30); //idle
            motoreDx.setSpeed(-100);
            motoreSx.setSpeed(100);
            while(differenza_angoloDx(angolo0, angolo)<55); //idle
            motoreDx.setSpeed(-75);
            motoreSx.setSpeed(75);
            while(differenza_angoloDx(angolo0, angolo)<65); //idle
            motoreDx.setSpeed(-50);
            motoreSx.setSpeed(50);
            while(differenza_angoloDx(angolo0, angolo)<75); //idle
            motoreDx.setSpeed(-25);
            motoreSx.setSpeed(25);
            while(differenza_angoloDx(angolo0, angolo)<85); //idle
            motoreDx.setSpeed(-5);
            motoreSx.setSpeed(5);
            while(differenza_angoloDx(angolo0, angolo)<89.9); //idleù
            stoppa_tutto();*/

            for (float i = 0; i < 100000; i += 0.1) {
                connectionString.setText("" + differenza_angoloDx(angolo0, angolo));
                for (int j = 0; j < 1000; j++) ;
            }
        });
    }


    protected void stoppa_tutto() {
        gestice_eccezioni(() -> {
            motoreDx.stop();
            motoreSx.stop();
        });
    }


    public interface MyRunnable {
        void run() throws IOException;
    }
}



