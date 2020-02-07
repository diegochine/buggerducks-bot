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

    public Robot (Context c){
        motore_dx = null;
        motore_sx = null;
        motore_pinza = null;
        sensore = null;
        ev3 = null;
        connesso = false;
        giroscopio = new Giroscopio(c);
    }

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
            connesso = false;//non ha mai perso la connessione quindi speriamo bene
        }catch (ExecutionException | InterruptedException e2){
            //problema se il thread muore, per ora non è mai successo
        }
        return f;
    }

    public void stop(){
        try {
            motore_sx.stop();
            motore_dx.stop();
            motore_pinza.stop();
        } catch (IOException e) {
            connesso=false;
            e.printStackTrace();
        }
    }

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

    public void raccogli_mina(){
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

    public void avanza() { //di una casella
        try{
            motore_dx.setTimePower(70, 780, 890, 1000, true);
            motore_sx.setTimePower(71, 780, 890, 1000, true);

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

    private float differenza_angolo_sx(float iniziale, float attuale) {
        if (iniziale * attuale < 0) {//discordi
            if (iniziale > 0) return (180 - iniziale) + (180 + attuale);
            else return Math.abs(iniziale) + attuale;
        }
        return Math.abs(iniziale - attuale);
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

    public void gira_dx(float gradi, int power) {
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
}