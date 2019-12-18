package com.example.buggerduckbot;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.GenEV3;
import it.unive.dais.legodroid.lib.comm.BluetoothConnection;
import it.unive.dais.legodroid.lib.plugs.GyroSensor;
import it.unive.dais.legodroid.lib.plugs.LightSensor;
import it.unive.dais.legodroid.lib.plugs.Plug;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;
import it.unive.dais.legodroid.lib.plugs.TouchSensor;
import it.unive.dais.legodroid.lib.plugs.UltrasonicSensor;
import it.unive.dais.legodroid.lib.util.Consumer;
import it.unive.dais.legodroid.lib.util.Prelude;
import it.unive.dais.legodroid.lib.util.ThrowingConsumer;

public class MainActivity extends AppCompatActivity {

    private static class MyCustomApi extends EV3.Api {

        private MyCustomApi(@NonNull GenEV3<? extends EV3.Api> ev3) {
            super(ev3);
        }

        public void mySpecialCommand() { /* do something special */ }
    }

    private TachoMotor motoreDx, motoreSx;
    private TextView textOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //roba nuova

        //roba della view
        Button startButton = findViewById(R.id.task1Btn);
        textOutput = findViewById(R.id.errori);

        //roba del robot
        try {
            // connect to EV3 via bluetooth
            BluetoothConnection.BluetoothChannel ch = new BluetoothConnection("DUCK").connect(); // replace with your own brick name

            EV3 ev3 = new EV3(ch);
            // use GenEV3 only if you need a custom API
            //GenEV3<MyCustomApi> ev3 = new GenEV3<>(ch);

            startButton.setOnClickListener(v -> Prelude.trap(() -> ev3.run(this::legoMain)));
            // alternatively with GenEV3
//          startButton.setOnClickListener(v -> Prelude.trap(() -> ev3.run(this::legoMainCustomApi, MyCustomApi::new)));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    protected void legoMain(EV3.Api api){
        motoreDx = api.getTachoMotor(EV3.OutputPort.D);
        motoreSx = api.getTachoMotor(EV3.OutputPort.A);

        try {
            motoreDx.resetPosition();
            motoreSx.resetPosition();

            motoreDx.setType(TachoMotor.Type.LARGE);
            motoreSx.setType(TachoMotor.Type.LARGE);

            motoreDx.setPolarity(TachoMotor.Polarity.FORWARD);
            motoreSx.setPolarity(TachoMotor.Polarity.FORWARD);

            motoreDx.start();
            motoreSx.start();

            motoreDx.setTimePower(100, 1000, 2000, 1000, true);
            motoreSx.setTimePower(-100, 1000, 2000, 1000, true);

            motoreDx.brake();
            motoreSx.brake();
        }catch (IOException e){
            textOutput.setText(R.string.genericErrorString);
        }/*catch (InterruptedException e){
            textOutput.setText("InterruptedException");
        }catch (ExecutionException e){
            textOutput.setText("ExecutionException");
        }*/
    }


    protected void gira(int gradi){

    }

    protected void avanza (){ //di una casella

    }

}



