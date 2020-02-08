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
import android.util.Pair;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.comm.BluetoothConnection;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;
import it.unive.dais.legodroid.lib.plugs.UltrasonicSensor;
import it.unive.dais.legodroid.lib.util.Prelude;

public class MapActivity extends AppCompatActivity {

    private TextView output_errori, output_stato;

    Robot robot;
    Pair<Integer, Integer> dimMap, posIniziale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //inizializzazione activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //ottenimento elementi grafici dalla UI
        Button connectButton = findViewById(R.id.connectionBtn);
        output_errori = findViewById(R.id.errori);
        output_stato = findViewById(R.id.connectionString);


        //intent
        Intent myIntent = getIntent();
        Map map = myIntent.getParcelableExtra("map");
        int task = myIntent.getIntExtra("taskId", 0);
        dimMap = map.getDimension();
        posIniziale = map.getInitialPosition();

        //mappa
        final GridView mapLayout =  findViewById(R.id.map);
        mapLayout.setNumColumns(map.getDimension().second);
        mapLayout.setAdapter(new CellAdapter(this, R.drawable.empty_square, map.getDimension().first*map.getDimension().second));

       //Robot
        robot = new Robot(this);

        //iniziallizazione elementi grafici
        output_stato.setText(R.string.notConnectionString);
        output_errori.setText(R.string.noErrorString);
        connectButton.setOnClickListener((e) -> {
            //Se non sei gia connesso connettiti
            if (!robot.isConnesso()) {
                if(robot.connetiti()){
                    output_stato.setText(R.string.connectionString);
                }else{
                    output_stato.setText(R.string.connectionError);
                }
            }
            if(task == 1){
                taskOne();
            }else if(task == 2){
                taskTwo();
            }else if (task == 3){
                taskThree();
            }
        });
    }


    private void taskOne(){
        int n_mine=1;//FIXME va preso da input il numero di mine

        boolean [] colonne = new boolean[dimMap.first];
        for(int i =0; i<dimMap.first; ++i) colonne[i]=false;

        int px = posIniziale.first, py = posIniziale.second;

        while(n_mine > 0){
            //TODO pulisci prima riga
            int col = go_to_new_col(px, py, colonne);
            boolean mina = scan_col();
            if(mina){
                robot.raccogli_mina();
                --n_mine;
                dep_mina(px, py);
            }else{
                colonne[col]=true;
            }
        }
    }

    void dep_mina (int x, int y){
        robot.punta_indietro();
        while(y>0){
            robot.avanza();
            --y;
        }
        if(x > posIniziale.first){
            robot.punta_sx();
            while(x>posIniziale.first){
                robot.avanza();
                --x;
            }
        }else if (x < posIniziale.first){
            robot.punta_dx();
            while(x<posIniziale.first){
                robot.avanza();
                ++x;
            }
        }
        robot.punta_indietro();
        robot.avanza();
        robot.posa_mina();
        try {
            Thread.sleep(1500);
        }catch (InterruptedException e){
                output_errori.setText(R.string.sleepError);
        }
        robot.punta_avanti();
        robot.avanza();
    }

    //puo essere chiamata solo se il robot è nella prima riga (aka y=0) FIXME se ci sono mine
    int go_to_new_col(int x, int y, boolean [] col){
        int maxX = dimMap.first;
        int prima_libera;
        for(prima_libera=0; col[prima_libera]; ++prima_libera);//FIXME do per scontato che non posso finire le colonne se non ho finito le mine
        if(prima_libera < x){//devo andare a sinistra
            robot.punta_sx();
            while(x!=prima_libera){
                robot.avanza();
                --x;
            }

        }else if (prima_libera > x){//è a destra
            robot.punta_dx();
            while(x!=prima_libera){
                robot.avanza();
                ++x;
            }

        }
        //anche se è sopra di me (aka x==primalibera)
        robot.punta_avanti();
        return prima_libera;
    }

    //ritorna true se ha finito la colonna
    //false se ha trovato una pallina
    boolean scan_col(){
        int maxY = dimMap.second;
        boolean mina = false;
        for(int y=0; y<maxY && !mina; ++y){
            robot.avanza();
            mina = robot.presenza_mina();
        }
        if( mina ){
            return true;
        }else{
            robot.punta_indietro();
            while(maxY>0){
                robot.avanza();
                --maxY;
            }
            return false;
        }
    }


    private void taskTwo(){

    }

    private void taskThree(){

    }
}
