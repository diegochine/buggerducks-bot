package com.example.buggerduckbot;

public class Direzione {
    private int d;


    public static final int AVANTI = 0;
    public static final int DESTRA = 1;
    public static final int SINISTRA = 3;
    public static final int INDIETRO = 2;

    public Direzione(int d) {
        this.d = d;
    }

    public boolean is_avanti(){
        return d == AVANTI;
    }
    public boolean is_dx(){
        return d == DESTRA;
    }
    public boolean is_indietro(){
        return d == INDIETRO;
    }
    public boolean is_sx(){
        return d == SINISTRA;
    }


    public void gira_dx(){
        d = (d+1)%4;
    }
    public void gira_sx(){
        d = (d+3)%4;
    }
    public void voltati(){ d = (d+2)%4; }



}
