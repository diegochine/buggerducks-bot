package com.example.buggerduckbot;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Pair;

import java.util.ArrayList;


public class Map implements Parcelable {

    private final Pair<Integer, Integer> dimension;
    private final Pair<Integer, Integer> initialPosition;
    private Pair<Integer, Integer> position;
    private ArrayList<Pair<Integer, Integer>> balls;


    public Map(Pair<Integer, Integer> dimension, Pair<Integer, Integer> initialPosition) {
        this.dimension = dimension;
        this.initialPosition = initialPosition;
    }

    public Map(Parcel in){
        String[] data= new String[2];

        in.readStringArray(data);
        this.dimension = new Pair<>(Integer.valueOf(data[0].substring(0,1)), Integer.valueOf(data[0].substring(2)));
        this.initialPosition = new Pair<>(Integer.valueOf(data[1].substring(0,1)), Integer.valueOf(data[1].substring(2)));
    }

    public Pair<Integer, Integer> getDimension() {
        return dimension;
    }

    public Pair<Integer, Integer> getInitialPosition() {
        return initialPosition;
    }

    public Integer getCellNumber(){
        return dimension.first * dimension.second;
    }

    public Pair<Integer, Integer> getPosition() {
        return position;
    }

    public ArrayList<Pair<Integer, Integer>> getBalls() {
        return balls;
    }

    public void moveForward(){
        this.position = new Pair<>(this.position.first, this.position.second-1);
    }

    public void moveBackward(){
        this.position = new Pair<>(this.position.first, this.position.second+1);
    }

    public void moveLeft(){
        this.position = new Pair<>(this.position.first-1, this.position.second);
    }

    public void moveRight(){
        this.position = new Pair<>(this.position.first+1, this.position.second);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringArray(new String[]{
                this.dimension.first + " " + this.dimension.second,
                this.initialPosition.first + " " + this.initialPosition.second});
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
