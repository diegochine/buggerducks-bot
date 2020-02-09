package com.example.buggerduckbot;

public class Direzione {
    private int d;

    /*
     * 0 = avanti (aka se avanzi y aumenta)
     * 1 = destra (aka se avanzi x aumenta)
     * 2 = indietro (aka se avanzi y diminuisce)
     * 3 = sinistra (aka se avanzi x diminuisce)
     */

    public Direzione() {
        this.d = 0;
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
