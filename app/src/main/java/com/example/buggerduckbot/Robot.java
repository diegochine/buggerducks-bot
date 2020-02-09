package com.example.buggerduckbot;

import android.content.Context;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import it.unive.dais.legodroid.lib.EV3;
import it.unive.dais.legodroid.lib.GenEV3;
import it.unive.dais.legodroid.lib.comm.BluetoothConnection;
import it.unive.dais.legodroid.lib.plugs.TachoMotor;
import it.unive.dais.legodroid.lib.plugs.UltrasonicSensor;

public class Robot {

    private TachoMotor motore_dx, motore_sx, motore_pinza;
    private UltrasonicSensor sensore;
    private float distanza_suolo;
    private boolean connesso;
    private EV3 ev3;
    private BluetoothConnection.BluetoothChannel ch;
    private Giroscopio giroscopio;
    private Direzione d;

    //costruttore
    public Robot (Context c){
        motore_dx = null;
        motore_sx = null;
        motore_pinza = null;
        sensore = null;
        ev3 = null;
        connesso = false;
        giroscopio = new Giroscopio(c);
        d = new Direzione();
    }

    //gestione connessione
    public boolean connetiti(){
        if(connesso)return true;
        try {
            ch = new BluetoothConnection("DUCK").connect(); // replace with your own brick name
        }catch (IOException e){
            return false;
        }
        ev3 = new EV3(ch);
        connesso = true;

        try {
            ev3.run(this::inizializza);
        } catch (GenEV3.AlreadyRunningException e) {
            e.printStackTrace();//non so cosa fare, non dovrebbe succedere mai comunque
        }
        return true;
    }

    public boolean isConnesso(){
        return connesso;
    }

    //sensore
    //ritorna -1 se il robot non è connesso
    private float leggiSensore(){
        if(!connesso)return -1;
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
            connesso = false;//non ha mai perso la connessione quindi speriamo bene
        }catch (ExecutionException | InterruptedException e2){
            //problema se il thread muore, per ora non è mai successo
        }
        return f;
    }

    public boolean presenza_mina(){
        if(!connesso)return false;
        //dato che so che è connesso non mi darà mai -1
        float distanza_attuale = leggiSensore();
        float limite = distanza_suolo * 0.85f;
        return distanza_attuale < limite;
    }

    //inizializzazione
    private void inizializza(EV3.Api api){
        motore_dx = api.getTachoMotor(EV3.OutputPort.A);
        motore_sx = api.getTachoMotor(EV3.OutputPort.D);
        motore_pinza = api.getTachoMotor(EV3.OutputPort.C);
        sensore = api.getUltrasonicSensor(EV3.InputPort._1);

        try {
            motore_dx.setType(TachoMotor.Type.LARGE);
            motore_sx.setType(TachoMotor.Type.LARGE);
            motore_pinza.setType(TachoMotor.Type.MEDIUM);

            motore_dx.setPolarity(TachoMotor.Polarity.BACKWARDS);
            motore_sx.setPolarity(TachoMotor.Polarity.BACKWARDS);
            motore_pinza.setPolarity(TachoMotor.Polarity.FORWARD);
        } catch (IOException e) {
            connesso = false;
            e.printStackTrace();//non ha mai perso la connessione quindi speriamo bene
        }

        stop();
        distanza_suolo = leggiSensore();
        giroscopio.register();
    }


    //utilizzo pinza
    public void raccogli_mina(){
        if(!connesso)return;
        try {
            motore_pinza.setTimePower(70, 300, 400, 300, true);
            motore_pinza.waitUntilReady();
        } catch (IOException e) {
            connesso=false;
            e.printStackTrace();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void posa_mina(){
        if(!connesso)return;
        try {
            motore_pinza.setTimePower(-70, 300, 300, 300, true);
            motore_pinza.waitUntilReady();
        } catch (IOException e) {
            connesso=false;
            e.printStackTrace();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    //motori
    public void stop(){
        if(!connesso)return;
        try {
            motore_sx.stop();
            motore_dx.stop();
            motore_pinza.stop();
        } catch (IOException e) {
            connesso=false;
            e.printStackTrace();
        }
    }

    public void avanza() { //di una casella
        if(!connesso)return;
        try{
            motore_dx.setTimePower(70, 780, 1200, 1000, true);
            motore_sx.setTimePower(72, 780, 1200, 1000, true);

            motore_dx.waitCompletion();
            motore_sx.waitCompletion();

            Thread.sleep(1000);
        } catch (IOException e) {
            connesso=false;
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        stop();
    }

    //rotazione
    private float differenza_angolo_sx(float iniziale, float attuale) {
        float dif_angolo_sx;
        if (iniziale * attuale < 0) {//discordi
            if (iniziale < 0) return (180 + iniziale) + (180 - attuale);
            else return Math.abs(attuale) + iniziale;
        }
        return Math.abs(attuale - iniziale);
    }

    private float differenza_angolo_dx(float iniziale, float attuale) {
        float diff_angolo;
        if(iniziale * attuale > 0 ) diff_angolo = Math.abs(attuale - iniziale);
        else {
            //sono per forza discordi
            if (iniziale > 0) diff_angolo = (180 - iniziale) + (180 + attuale);
            else diff_angolo = Math.abs(iniziale) + attuale;
        }
        return diff_angolo;
    }

    private void gira_dx(float gradi, int power) {
        if(!connesso)return;
        try {
            Thread.sleep(500);
        }catch (InterruptedException ex){}

        while(giroscopio.getOrientation()==null){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        float angolo_inizale = giroscopio.getOrientation();
        float angolo = 0;
        float limite = gradi*0.8f;

        try {
            motore_dx.setPower(-power);
            motore_sx.setPower(power);

            motore_dx.start();
            motore_sx.start();


            while (angolo < limite){
                angolo = differenza_angolo_dx(angolo_inizale, giroscopio.getOrientation());
            }

            motore_dx.setPower(-power/2);
            motore_sx.setPower(power/2);

            while (angolo < gradi) {
                angolo = differenza_angolo_dx(angolo_inizale, giroscopio.getOrientation());
            }

            motore_dx.stop();
            motore_sx.stop();
        }catch (IOException e){
            connesso=false;
            e.printStackTrace();
        }
    }

    private void gira_sx() {
        if(!connesso)return;
        try {
            Thread.sleep(500);
        }catch (InterruptedException ex){}

        while(giroscopio.getOrientation()==null){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        float angolo_inizale = giroscopio.getOrientation();
        float angolo = 0;
        float limite = 90*0.8f;

        try {
            motore_dx.setPower(30);
            motore_sx.setPower(-30);

            motore_dx.start();
            motore_sx.start();


            while (angolo < limite){
                angolo = differenza_angolo_sx(angolo_inizale, giroscopio.getOrientation());
            }

            motore_dx.setPower(15);
            motore_sx.setPower(-15);

            while (angolo < 90) {
                angolo = differenza_angolo_sx(angolo_inizale, giroscopio.getOrientation());
            }

            motore_dx.stop();
            motore_sx.stop();
        }catch (IOException e){
            connesso=false;
            e.printStackTrace();
        }
    }

    //cambio direzione
    public void punta_avanti(){
        if(d.is_avanti())return;
        if(d.is_dx()){
            gira_sx();
            d.gira_sx();
        }else if(d.is_sx()){
            gira_dx(90,30);
            d.gira_dx();
        }else{//è verso indietro
            gira_dx(180, 40);
            d.voltati();
        }
    }

    public void punta_sx(){
        if(d.is_sx())return;
        if(d.is_avanti()){
            gira_sx();
            d.gira_sx();
        }else if(d.is_indietro()){
            gira_dx(90, 30);
            d.gira_dx();
        }else{//è verso destra
            gira_dx(180, 40);
            d.voltati();
        }
    }

    public void punta_dx(){
        if(d.is_dx())return;
        if(d.is_indietro()){
            gira_sx();
            d.gira_sx();
        }else if(d.is_avanti()){
            gira_dx(90, 30);
            d.gira_dx();
        }else{//è verso sinistra
            gira_dx(180, 40);
            d.voltati();
        }
    }

    public void punta_indietro(){
        if(d.is_indietro())return;
        if(d.is_sx()){
            gira_sx();
            d.gira_sx();
        }else if(d.is_dx()){
            gira_dx(90, 30);
            d.gira_dx();
        }else{//sta guardando avanti
            gira_dx(180, 40);
            d.gira_dx();
        }
    }
}
