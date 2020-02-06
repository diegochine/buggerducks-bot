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

    private TextView textOutput;

    private boolean connected;
    private EV3 ev3;
    private TachoMotor motoreDx, motoreSx, pinza;
    private UltrasonicSensor sensore;

    Pair<Integer, Integer> dimMap, posIniziale;

    private Float angolo, no_mina;
    Direzione d;

    public interface MyRunnable {
        void run() throws IOException;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //inizializzazione activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //grafica
        Button connectButton = findViewById(R.id.connectionBtn);
        textOutput = findViewById(R.id.errori);
        TextView connectionString = findViewById(R.id.connectionString);

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

        // Inizializzazione Giroscopio
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

        // Connessione con il robot
        connectButton.setOnClickListener((e) -> {
            try {
                // connect to EV3 via bluetooth
                if (!connected) {
                    BluetoothConnection.BluetoothChannel ch = new BluetoothConnection("DUCK").connect(); // replace with your own brick name

                    ev3 = new EV3(ch);
                    connected = true;
                    connectionString.setText(R.string.connectionString);
                    textOutput.setText(R.string.noErrorString);

                    if(task == 1){
                        Prelude.trap(() -> ev3.run(this::taskOne));
                    }else if(task == 2){
                        Prelude.trap(() -> ev3.run(this::taskTwo));
                    }else if (task == 3){
                        Prelude.trap(() -> ev3.run(this::taskThree));
                    }

                }
            } catch (IOException ex) {
                textOutput.setText(R.string.connectionError);
            }
        });
    }

    /**
     * Le nostre funzioni
     * */

    /**
     * Inizializza motori e sensori del robot ad inizio task
     */
    private void inizialization(EV3.Api api) {
        motoreDx = api.getTachoMotor(EV3.OutputPort.A);
        motoreSx = api.getTachoMotor(EV3.OutputPort.D);
        pinza = api.getTachoMotor(EV3.OutputPort.C);
        sensore = api.getUltrasonicSensor(EV3.InputPort._1);
        d = new Direzione();

        gestisci_eccezioni(() -> {
            motoreDx.setType(TachoMotor.Type.LARGE);
            motoreSx.setType(TachoMotor.Type.LARGE);
            pinza.setType(TachoMotor.Type.MEDIUM);

            motoreDx.setPolarity(TachoMotor.Polarity.FORWARD);
            motoreSx.setPolarity(TachoMotor.Polarity.FORWARD);
            pinza.setPolarity(TachoMotor.Polarity.FORWARD);

            motoreDx.resetPosition();
            motoreSx.resetPosition();
            pinza.resetPosition();

            no_mina = leggiSensore();

            stoppa_tutto();
        });

    }

    private float leggiSensore(){
        Future<Float> ff = null;
        Float f = null;
        try{
            while(f == null){
                if(ff == null || ff.isCancelled()){
                    ff = sensore.getDistance();
                }else{
                    if( ff.isDone()){
                        f = ff.get();
                    }else{
                        Thread.sleep(50);
                    }
                }
            }
        }catch (IOException e1){
            textOutput.setText(R.string.connectionError);
            connected = false;
        }catch (ExecutionException | InterruptedException e2){
            textOutput.setText("Il future xe nda in merda");
        }
        return f;
    }

    private void gestisci_eccezioni(TestActivity.MyRunnable r) {
        try {
            r.run();
        } catch (IOException e) {
            textOutput.setText(R.string.connectionError);
            connected = false;
        }
    }

    private void prendiMina(){
        gestisci_eccezioni(()->{
            pinza.setTimePower(70, 300, 400, 300, true);
        });
    }

    private void lasciaMina(){
        gestisci_eccezioni(()->{
            pinza.setTimePower(-70, 300, 300, 300, true);
        });
    }

    private void vai_avanti() { //di una casella
        gestisci_eccezioni(() -> {
            motoreDx.waitCompletion();
            motoreSx.waitCompletion();

            motoreDx.setTimePower(50, 780, 890, 1000, true);
            motoreSx.setTimePower(51, 780, 890, 1000, true);

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

            d.giraDx();
            //textOutput.setText("so riva more");
        });
    }


    private void stoppa_tutto() {
        gestisci_eccezioni(() -> {
            motoreDx.stop();
            motoreSx.stop();
            pinza.stop();
        });
    }
    /**
     * Esecuzione dei Task
     * */

    private void taskOne(EV3.Api api){
        this.inizialization(api);

        int n_mine=1;//FIXME va preso da input il numero di mine

        boolean colonne [] = new boolean[dimMap.first];
        for(int i =0; i<dimMap.first; ++i) colonne[i]=false;

        int px = posIniziale.first, py = posIniziale.second;

        while(n_mine > 0){
            //TODO pulisci prima riga
            int col = go_to_new_col(px, py, colonne);
            boolean mina = scan_col();
            if(mina){
                prendiMina();
                --n_mine;
                dep_mina(px, py);
            }else{
                colonne[col]=true;
            }
        }
    }

    void dep_mina (int x, int y){
        punta_indietro();
        while(y>0){
            vai_avanti();
            --y;
        }
        if(x > posIniziale.first){
            punta_sx();
            while(x>posIniziale.first){
                vai_avanti();
                --x;
            }
        }else if (x < posIniziale.first){
            punta_dx();
            while(x<posIniziale.first){
                vai_avanti();
                ++x;
            }
        }
        punta_indietro();
        vai_avanti();
        lasciaMina();
        try {
            Thread.sleep(1500);
        }catch (InterruptedException e){
                textOutput.setText(R.string.sleepError);
        }
        punta_su();
        vai_avanti();
    }

    //puo essere chiamata solo se il robot è nella prima riga (aka y=0) FIXME se ci sono mine
    int go_to_new_col(int x, int y, boolean [] col){
        int maxX = dimMap.first;
        int prima_libera;
        for(prima_libera=0; col[prima_libera]; ++prima_libera);//FIXME do per scontato che non posso finire le colonne se non ho finito le mine
        if(prima_libera < x){//devo andare a sinistra
            punta_sx();
            while(x!=prima_libera){
                vai_avanti();
                --x;
            }

        }else if (prima_libera > x){//è a destra
            punta_dx();
            while(x!=prima_libera){
                vai_avanti();
                ++x;
            }

        }
        //anche se è sopra di me (aka x==primalibera)
        punta_su();
        return prima_libera;
    }

    //ritorna true se ha finito la colonna
    //false se ha trovato una pallina
    boolean scan_col(){
        int maxY = dimMap.second;
        boolean mina = false;
        for(int y=0; y<maxY && !mina; ++y){
            vai_avanti();
            mina = cerca_mina();
        }
        if( mina ){
            return true;
        }else{
            punta_indietro();
            while(maxY>0){
                vai_avanti();
                --maxY;
            }
            return false;
        }
    }

    boolean cerca_mina(){
        return leggiSensore() < (no_mina - no_mina*0.15);
    }

    void punta_su(){
        if(d.isAvanti())return;
        if(d.isDx()){
            //gira_sx(); FIXME
        }else{
            gira_dx();
            if(d.isSx()){
                gira_dx();
            }
        }
    }

    void punta_sx(){
        if(d.isSx())return;
        if(d.isAvanti()){
            //gira_sx(); FIXME
        }else{
            gira_dx();
            if(d.isIndietro()){
                gira_dx();
            }
        }
    }

    void punta_dx(){
        if(d.isDx())return;
        if(d.isIndietro()){
            //gira_sx(); FIXME
        }else{
            gira_dx();
            if(d.isAvanti()){
                gira_dx();
            }
        }
    }

    void punta_indietro(){
        if(d.isIndietro())return;
        if(d.isSx()){
            //gira_sx(); FIXME
        }else{
            gira_dx();
            if(d.isDx()){
                gira_dx();
            }
        }
    }

    private void taskTwo(EV3.Api api){
        this.inizialization(api);

    }

    private void taskThree(EV3.Api api){
        this.inizialization(api);
    }
}
