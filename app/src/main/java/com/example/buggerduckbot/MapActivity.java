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
import java.util.ArrayList;
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
        Pair<Integer, Integer> dimMap = map.getDimension();
        Pair<Integer, Integer> posIniziale = map.getInitialPosition();

        //mappa
        final GridView mapLayout =  findViewById(R.id.map);
        mapLayout.setNumColumns(map.getDimension().second);
        final CellAdapter cellAdapter = new CellAdapter(this, R.drawable.empty_square, map);
        mapLayout.setAdapter(cellAdapter);

       //Robot
        robot = new Robot(this);

        //iniziallizazione elementi grafici
        output_stato.setText(R.string.notConnectionString);
        output_errori.setText(R.string.noErrorString);
        connectButton.setOnClickListener((e) -> {
            //Grafica della Mappa
            cellAdapter.setHeight(mapLayout.getColumnWidth());
            // dice alla grafica che qualcosa è cambiato e si ridisegna
            cellAdapter.notifyDataSetChanged();

            //Se non sei gia connesso connettiti
            if (!robot.isConnesso()) {
                if(robot.connetiti()){
                    output_stato.setText(R.string.connectionString);
                }else{
                    output_stato.setText(R.string.connectionError);
                }
            }
            if(task == 1){
                //first = x
                //second = y
                //x = numero colonne
                //y = numero righe
                //taskOne();
            }else if(task == 2){
                taskTwo();
            }else if (task == 3){
                taskThree();
            }
        });
    }


    private  ArrayList <Pair<Integer, Integer>> taskOne(int colonna_iniziale, int n_righe, int n_colonne){
        int n_mine=1;//FIXME va preso da input il numero di mine
        int riga = 0;
        int colonna = colonna_iniziale;

        boolean [] colonne = new boolean[n_colonne];
        for(int i =0; i<n_colonne; ++i) colonne[i]=false;

        ArrayList <Pair<Integer, Integer>> mine = new ArrayList<>();

        while(n_mine > 0){
            //TODO pulisci prima riga
            colonna = go_to_new_col(riga, colonna, colonne);
            riga = scan_col(n_righe);
            if(riga != 0){//se ha trovato un mina si ferma sopra essa
                robot.raccogli_mina();
                --n_mine;
                dep_mina(riga, colonna, colonna_iniziale);

                mine.add(new Pair<>(riga, colonna));

                riga = 0;
                colonna = colonna_iniziale;
            }else{// se non ha trovato una mina torna nella righa 0
                colonne[colonna]=true;
            }
        }
        return mine;
    }

    void dep_mina (int riga, int colonna, int colonna_iniziale){
        robot.punta_indietro();

        //torno sulla prima riga
        while(riga>0){
            robot.avanza();
            --riga;
        }

        //vado davanti la safezone
        if(colonna > colonna_iniziale){
            robot.punta_sx();
            while(colonna > colonna_iniziale){
                robot.avanza();
                --colonna;
            }
        }else if (colonna < colonna_iniziale){
            robot.punta_dx();
            while(colonna < colonna_iniziale){
                robot.avanza();
                ++colonna;
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
    int go_to_new_col(int colonna, int n_colonne, boolean [] col){
        int prima_libera;
        for(prima_libera=0; col[prima_libera]; ++prima_libera);//do per scontato che non posso finire le colonne se non ho finito le mine
        if(prima_libera < colonna){//devo andare a sinistra
            robot.punta_sx();
            while(colonna!=prima_libera){
                robot.avanza();
                --colonna;
            }

        }else if (prima_libera > colonna){//è a destra
            robot.punta_dx();
            while(colonna!=prima_libera){
                robot.avanza();
                ++colonna;
            }

        }
        //anche se è sopra di me (aka x==primalibera)
        robot.punta_avanti();
        return prima_libera;
    }

    //ritorna true se ha finito la colonna
    //false se ha trovato una pallina
    int scan_col(int n_righe){
        boolean mina = false;
        int riga;
        for(riga=0; riga<n_righe && !mina; ++riga){
            robot.avanza();
            mina = robot.presenza_mina();
        }
        if( mina ){
            return riga;
        }else{
            robot.punta_indietro();
            while(n_righe>0){
                robot.avanza();
                --n_righe;
            }
            return 0;
        }
    }


    private void taskTwo(){

    }

    private void taskThree(){

    }
}
