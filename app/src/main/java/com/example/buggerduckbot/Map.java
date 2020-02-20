package com.example.buggerduckbot;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Pair;

import java.util.ArrayList;


public class Map implements Parcelable {

    private int numeroRighe, numeroColonne, riga, colonna, rigaIn, colonnaIn;
    private ArrayList<Pair<Integer, Integer>> mine;

    public Map(int n_r, int n_c, int r, int c) {
        numeroRighe = n_r;
        numeroColonne = n_c;
        riga = r;
        colonna = c;
        rigaIn = riga;
        colonnaIn = colonna;
        this.mine = new ArrayList<>();
    }

    public Map(Parcel in){
        String[] data= new String[2];

        in.readStringArray(data);
        String dim = data[0];
        String pos = data[1];
        numeroRighe = Integer.valueOf(dim.substring(0,dim.indexOf(" ")));
        numeroColonne = Integer.valueOf(dim.substring(dim.indexOf(" ")+1));
        riga = Integer.valueOf(pos.substring(0,pos.indexOf(" ")));
        colonna = Integer.valueOf(pos.substring(pos.indexOf(" ")+1));
        colonnaIn = colonna;
        rigaIn = riga;
        this.mine = new ArrayList<>();
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

    public boolean haTrovato(int a, int b){
        for(Pair <Integer, Integer> x : mine){
            int a1 = x.first, b1=x.second;
            if(a == a1 && b == b1)return true;
        }
        return false;
    }

    public void addMina(){
        mine.add(new Pair<>(riga, colonna));
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
