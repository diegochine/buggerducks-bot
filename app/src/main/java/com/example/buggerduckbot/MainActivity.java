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
import java.util.concurrent.Future;
import java.util.function.Consumer;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.BluetoothConnection;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;
import it.unive.dais.legodroid.lib.util.Prelude;

public class MainActivity extends AppCompatActivity {
    //robe del telefono
    private SensorManager sensorManager;
    private Sensor giroscopio;
    //robe del robot
    private TachoMotor motoreDx, motoreSx;
    private EV3 ev3;
    //robe della grafica
    private TextView textOutput, connectionString;
    //roba del programma
    private boolean connected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //roba della grafica
        Button task1Button, task2Button, task3Button, connectButton;
        task1Button = findViewById(R.id.task1Btn);
        task2Button = findViewById(R.id.task2Btn);
        task3Button = findViewById(R.id.task3Btn);
        connectButton = findViewById(R.id.connectionBtn);
        textOutput = findViewById(R.id.errori);
        connectionString = findViewById(R.id.connectionString);

        //roba del telefono
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        giroscopio = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        connected = false;


        //roba del robot
        connectButton.setOnClickListener((e) -> {
            try {
                // connect to EV3 via bluetooth
                if (!connected) {
                    BluetoothConnection.BluetoothChannel ch = new BluetoothConnection("DUCK").connect(); // replace with your own brick name

                    ev3 = new EV3(ch);
                    connected = true;

                    connectionString.setText(R.string.connectionString);

                    task1Button.setOnClickListener(ev -> vai_avanti());
                    task2Button.setOnClickListener(ev -> gira_dx());
                    task3Button.setOnClickListener(ev -> stoppa_tutto());

                    Prelude.trap(() -> ev3.run(this::legoMain));
                }
            } catch (IOException e2) {
                textOutput.setText(R.string.connectionError);
            }
        });


        task1Button.setOnClickListener(e -> textOutput.setText(R.string.needConnection));
        task2Button.setOnClickListener(e -> textOutput.setText(R.string.needConnection));
        task3Button.setOnClickListener(e -> textOutput.setText(R.string.needConnection));
    }


    protected void legoMain(EV3.Api api){
        motoreDx = api.getTachoMotor(EV3.OutputPort.D);
        motoreSx = api.getTachoMotor(EV3.OutputPort.A);

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


    protected void gestisci_eccezioni(MyRunnable r) {
        try {
            r.run();
        } catch (IOException e) {
            textOutput.setText(R.string.connectionError);
            connected = false;
        }
    }

    protected void vai_avanti() { //di una casella
        gestisci_eccezioni(() -> {
            motoreDx.waitCompletion();
            motoreSx.waitCompletion();

            motoreDx.setTimePower(51, 780, 890, 1000, true);
            motoreSx.setTimePower(50, 780, 890, 1000, true);

            motoreDx.waitCompletion();
            motoreSx.waitCompletion();

            stoppa_tutto();
        });
    }

    protected float differenza_angoloDx(float startAngle, float actualAngle) {
        if (startAngle * actualAngle < 0) {//discordi
            if (startAngle > 0) return (180 - startAngle) + (180 + actualAngle);
            else return Math.abs(startAngle) + actualAngle;
        }
        return Math.abs(startAngle - actualAngle);
    }

    protected void gira_dx() {
        Future<Float> a;
        gestisci_eccezioni(() -> {
            SensorEventListener listener = creaListener((angolo -> {

            }));

            sensorManager.registerListener(listener, giroscopio, Sensor.REPORTING_MODE_ONE_SHOT);


        });
    }



    protected void stoppa_tutto() {
        gestisci_eccezioni(() -> {
            motoreDx.stop();
            motoreSx.stop();
        });
    }

    public SensorEventListener creaListener(Consumer<Float> c) {
        SensorEventListener listener = new SensorEventListener() {
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

                c.accept(orientations[1]);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                //che cazzo ne so
            }
        };

        return listener;
    }

    public interface MyRunnable {
        void run() throws IOException;
    }
}



