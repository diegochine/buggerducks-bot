package com.example.buggerduckbot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;

public class MapActivity extends AppCompatActivity {

    private TextView output_errori, output_stato;

    private CellAdapter cellAdapter;

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
        int n_mine = myIntent.getIntExtra("mine", 0);


        //mappa
        final GridView mapLayout =  findViewById(R.id.map);
        mapLayout.setNumColumns(map.getDimension().second);
        cellAdapter = new CellAdapter(this, R.drawable.empty_square, map);
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
                //taskOne(map, n_mine);
            }else if(task == 2){

            }else if (task == 3){

            }
        });
    }
    /*
    private  void taskOne(Map map, int n_mine){
        Direzione d = new Direzione(Direzione.AVANTI);

        boolean [] file_da_controllare = new boolean[map.getMaxY()];
        for(int i =0; i<map.getMaxY(); ++i) file_da_controllare[i]=false;

        ArrayList <Pair<Integer, Integer>> mine = new ArrayList<>();

        while(n_mine > 0){
            //TODO pulisci prima riga
            go_to_new_col(riga, colonna, colonne);
            riga = scansione_colonna(n_righe);
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

   */
    private boolean scansione_colonna(Map map, Direzione d){
        boolean mina = false;
        while(!mina && map.getY() > 0){
            robot.avanza();
            map.moveUp();
            mina = robot.presenza_mina();
        }
        if( mina ){
            map.addBall(map.getPosition());
            cellAdapter.notifyDataSetChanged();
            return true;
        }else{
            robot.voltati();
            d.voltati();
            while(map.getY()<map.getMaxY()){
                robot.avanza();
                map.moveDown();
            }
            return false;
        }
    }


    private void taskTwo(){

    }

    private void taskThree(){

    }
}
