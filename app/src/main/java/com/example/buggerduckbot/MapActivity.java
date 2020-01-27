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
import android.widget.GridView;
import android.widget.TextView;

import java.io.IOException;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.BluetoothConnection;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;
import it.unive.dais.legodroid.lib.util.Prelude;

public class MapActivity extends AppCompatActivity {
    int task;

    private TextView textOutput;

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
        setContentView(R.layout.activity_map);

        Button connectButton = findViewById(R.id.connectionBtn);
        textOutput = findViewById(R.id.errori);
        TextView connectionString = findViewById(R.id.connectionString);

        Intent myIntent = getIntent();
        Map map = myIntent.getParcelableExtra("map");
        task = myIntent.getIntExtra("taskId", 0);

        final GridView mapLayout =  findViewById(R.id.map);
        mapLayout.setNumColumns(map.getDimension().second);
        mapLayout.setAdapter(new CellAdapter(this, R.drawable.empty_square, map.getDimension().first*map.getDimension().second));

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor giroscopio = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
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

        sensorManager.registerListener(listener, giroscopio, Sensor.REPORTING_MODE_CONTINUOUS, handler);

        connectButton.setOnClickListener((e) -> {
            try {
                // connect to EV3 via bluetooth
                if (!connected) {
                    BluetoothConnection.BluetoothChannel ch = new BluetoothConnection("DUCK").connect(); // replace with your own brick name

                    ev3 = new EV3(ch);
                    connected = true;
                    connectionString.setText(R.string.connectionString);
                    textOutput.setText(R.string.noErrorString);


                    Prelude.trap(() -> ev3.run(this::legoMain));
                }
            } catch (IOException ex) {
                textOutput.setText(R.string.connectionError);
            }
        });
    }

    protected void legoMain(EV3.Api api){
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

        if(this.task == 1){
            this.taskOne();
        }else if(this.task == 2){
            this.taskTwo();
        }else if (this.task == 3){
            this.taskThree();
        }
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

            motoreDx.setTimePower(50, 780, 890, 1000, true);
            motoreSx.setTimePower(52, 780, 890, 1000, true);

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
        gestisci_eccezioni(() -> {
            motoreDx.waitCompletion();
            motoreSx.waitCompletion();

            motoreDx.setPower(0);
            motoreSx.setPower(0);

            float angoloInizale = angolo;
            int power = 0;

            motoreDx.start();
            motoreSx.start();

            while (differenza_angoloDx(angoloInizale, angolo) < 45) {
                if (power < 50) {
                    power += 2;
                }
                motoreDx.setPower(-power);
                motoreSx.setPower(power);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    textOutput.setText(R.string.sleepError);
                }
            }

            while (differenza_angoloDx(angoloInizale, angolo) < 90) {
                if (power > 10) {
                    power -= 2;
                }
                motoreDx.setPower(-power);
                motoreSx.setPower(power);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    textOutput.setText(R.string.sleepError);
                }
            }

            textOutput.setText("so riva more");
            stoppa_tutto();
        });
    }



    protected void stoppa_tutto() {
        gestisci_eccezioni(() -> {
            motoreDx.stop();
            motoreSx.stop();
        });
    }

    private void taskOne(){

    }

    private void taskTwo(){

    }

    private void taskThree(){

    }
}
