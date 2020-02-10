package com.example.buggerduckbot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

public class MapActivity extends AppCompatActivity {

    private TextView output_errori, output_stato;

    private CellAdapter cellAdapter;

    private void punta_indietro(Direzione d){
        if(d.is_avanti()){
            robot.voltati();
            d.voltati();
        }
        else if(d.is_sx()){
            robot.gira_sx();
            d.gira_sx();
        }
        else if(d.is_dx()){
            robot.gira_dx();
            d.gira_dx();
        }
    }

    private void punta_avanti(Direzione d){
        if(d.is_indietro()){
            robot.voltati();
            d.voltati();
        }
        else if(d.is_sx()){
            robot.gira_dx();
            d.gira_dx();
        }
        else if(d.is_dx()){
            robot.gira_sx();
            d.gira_sx();
        }
    }

    private void punta_dx(Direzione d){
        if(d.is_sx()){
            robot.voltati();
            d.voltati();
        }
        else if(d.is_avanti()){
            robot.gira_dx();
            d.gira_dx();
        }
        else if(d.is_indietro()){
            robot.gira_sx();
            d.gira_sx();
        }
    }

    private void punta_sx(Direzione d){
        if(d.is_dx()){
            robot.voltati();
            d.voltati();
        }
        else if(d.is_avanti()) {
            robot.gira_sx();
            d.gira_sx();
        }else if(d.is_indietro()){
            robot.gira_dx();
            d.gira_dx();
        }
    }

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
        mapLayout.setNumColumns(map.getNumeroColonne());
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
                taskOne(map, n_mine);
            }else if(task == 2){

            }else if (task == 3){

            }
        });
    }

    private  void taskOne(Map map, int n_mine){
        Direzione d = new Direzione(Direzione.AVANTI);

        boolean [] colonne = new boolean[map.getNumeroColonne()];
        for(int i = 0; i<map.getNumeroColonne(); ++i) colonne[i]=false;

        while(n_mine > 0){
            //TODO pulisci prima riga
            go_to_new_col(map, colonne, d);
            boolean mina = scansione_colonna(map, d);
            if(mina){//se ha trovato un mina si ferma sopra essa
                robot.raccogli_mina();
                if(map.getRiga()==map.getNumeroRighe()-1) colonne[map.getColonna()]=true;
                --n_mine;
                dep_mina(map, d);
            }else{// se non ha trovato una mina torna nella righa 0
                colonne[map.getColonna()]=true;
            }
        }
    }

    void dep_mina (Map map, Direzione d){
        //torno sulla prima riga
        if(map.getRiga()!=map.getNumeroRighe()-1){
            punta_indietro(d);
            while(map.getRiga() < map.getNumeroRighe()-1){
                robot.avanza();
                map.moveDown();
            }
        }

        //vado davanti la safezone
        if(map.getColonna() > map.getInitialColonna()){
            punta_sx(d);
            while(map.getColonna() > map.getInitialColonna()){
                robot.avanza();
                map.moveLeft();
            }
        }else if (map.getColonna() < map.getInitialColonna()){
            punta_dx(d);
            while(map.getColonna() < map.getInitialColonna()){
                robot.avanza();
                map.moveRight();
            }
        }

        punta_indietro(d);
        robot.avanza();
        robot.posa_mina();
        try {
            Thread.sleep(1500);
        }catch (InterruptedException e){
                output_errori.setText(R.string.sleepError);
        }
        punta_avanti(d);
        robot.avanza();
    }

    //puo essere chiamata solo se il robot è nella prima riga (aka y=maxy)
    private void go_to_new_col(Map map, boolean [] col, Direzione d){
        int prima_libera;
        for(prima_libera=0; col[prima_libera]; ++prima_libera);//do per scontato che non posso finire le colonne se non ho finito le mine
        if(prima_libera < map.getColonna()){//devo andare a sinistra
            punta_sx(d);
            while(map.getColonna()!=prima_libera){
                robot.avanza();
                map.moveLeft();
            }

        }else if (prima_libera > map.getColonna()){//è a destra
            punta_dx(d);
            while(map.getColonna()!=prima_libera){
                robot.avanza();
                map.moveRight();
            }
        }
        //anche se è sopra di me (aka x==primalibera)

        //devo girarmi verso su
       punta_avanti(d);
    }


    private boolean scansione_colonna(Map map, Direzione d){
        //do per scontato che sto guardando su
        boolean mina = false;
        while(!mina && map.getRiga() > 0){
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
            //torno nella riga iniziale
            while(map.getRiga()<map.getNumeroRighe()-1){
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
