package com.example.buggerduckbot;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Pair;

import java.util.ArrayList;


public class Map implements Parcelable {

    private int numeroRighe, numeroColonne, riga, colonna, rigaIn, colonnaIn;
    private ArrayList<Pair<Integer, Integer>> balls;


    public Map(int n_r, int n_c, int r, int c) {
        numeroRighe = n_r;
        numeroColonne = n_c;
        riga = r;
        colonna = c;
        rigaIn = riga;
        colonnaIn = colonna;
        this.balls = new ArrayList<>();
    }

    public Map(Parcel in){
        String[] data= new String[2];

        in.readStringArray(data);
        numeroRighe = Integer.valueOf(data[0].substring(0,1));
        numeroColonne = Integer.valueOf(data[0].substring(2));
        riga = Integer.valueOf(data[1].substring(0,1));
        colonna = Integer.valueOf(data[1].substring(2));
        colonnaIn = colonna;
        rigaIn = riga;
        this.balls = new ArrayList<>();
    }

    public int getNumeroRighe(){
        return numeroRighe;
    }

    public int getNumeroColonne(){
        return numeroColonne;
    }

    public int getRiga(){
        return riga;
    }

    public int getColonna(){
        return colonna;
    }

    public Pair<Integer, Integer> getPosition(){
        return new Pair<>(riga, colonna);
    }

    public int getInitialRiga(){
        return rigaIn;
    }

    public int getInitialColonna(){
        return colonnaIn;
    }

    public Integer getCellNumber(){
        return numeroRighe * numeroColonne;
    }

    public ArrayList<Pair<Integer, Integer>> getBalls() {
        return balls;
    }

    public void addMina(){
        balls.add(new Pair<>(riga, colonna));
    }

    public void addBall(Pair<Integer, Integer> position){
        this.balls.add(position);
    }

    public void moveUp(){
        riga--;
    }

    public void moveDown(){
       riga++;
    }

    public void moveLeft(){
        colonna--;
    }

    public void moveRight(){
        colonna++;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringArray(new String[]{
                numeroRighe + " " + numeroColonne,
                rigaIn + " " + colonnaIn});
    }

    public static final Parcelable.Creator<Map> CREATOR= new Parcelable.Creator<Map>() {
        @Override
        public Map createFromParcel(Parcel source) {
            return new Map(source);
        }

        @Override
        public Map[] newArray(int size) {
            return new Map[size];
        }
    };
}
