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

                Integer x = Math.round(orientations[0]);
                Integer y = Math.round(orientations[1]);
                Integer z = Math.round(orientations[2]);

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                //che cazzo ne so
            }
        };

        sensorManager.registerListener(rvEventListener, rvSensor, sensorManager.SENSOR_DELAY_NORMAL);


        //roba della grafica
        task1Button = findViewById(R.id.task1Btn);
        task2Button = findViewById(R.id.task2Btn);
        task3Button = findViewById(R.id.task3Btn);
        Button connectButton = findViewById(R.id.connectionBtn);
        textOutput = findViewById(R.id.errori);
        connectionString = findViewById(R.id.connectionString);

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

            //motoreDx.start();
            //motoreSx.start();

            task1Button.setOnClickListener(e -> vai_avanti());
            task2Button.setOnClickListener(e -> vai_indietro());
            task3Button.setOnClickListener(e -> stoppa_tutto());

            motoreDx.waitCompletion();
            motoreSx.waitCompletion();

            motoreDx.brake();
            motoreSx.brake();
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


    protected void gira_dx() {
        gestice_eccezioni(() -> {
            motoreDx.waitCompletion();
            motoreSx.waitCompletion();

            motoreDx.setTimePower(-100, 1000, 2000, 1000, true);
            motoreSx.setTimePower(-100, 1000, 2000, 1000, true);
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



