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


    public void setAvanti(){
        d = 0;
    }
    public void setDx(){
        d = 1;
    }
    public void setIndietro(){
        d = 2;
    }
    public void setSx(){
        d = 3;
    }


    public boolean isAvanti(){
        return d == 0;
    }
    public boolean isDx(){
        return d == 1;
    }
    public boolean isIndietro(){
        return d == 2;
    }
    public boolean isSx(){
        return d == 3;
    }


    public void giraDx(){
        d = (d+1)%4;
    }
    public void giraSx(){
        d = (d-1)%4;
    }
    public void voltati(){ d = (d+2)%4; }
}
