package com.example.buggerduckbot;

public class Direzione {
    private int d;


    public final int AVANTI = 0;
    public final int DESTRA = 1;
    public final int SINISTRA = 2;
    public final int INDIETRO = 3;

    public Direzione(int d) {
        this.d = d;
    }

    public boolean is_avanti(){
        return d == 0;
    }
    public boolean is_dx(){
        return d == 1;
    }
    public boolean is_indietro(){
        return d == 2;
    }
    public boolean is_sx(){
        return d == 3;
    }


    public void gira_dx(){
        d = (d+1)%4;
    }
    public void gira_sx(){
        d = (d-1)%4;
    }
    public void voltati(){ d = (d+2)%4; }



}
