package com.example.buggerduckbot;

import android.util.Pair;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class Maze {
    private int [][] campo;
    private int r_i, c_i;

    public Maze (Map m){
        campo = new int [m.getNumeroRighe()] [m.getNumeroColonne()];
        for(int r = 0; r < campo.length; ++r){
            for(int c = 0; c < campo[0].length; ++c){
                if(m.haTrovato(r,c))    campo[r][c]=0;
                else campo[r][c]=1;
            }
        }

        r_i = m.getInitialRiga();
        c_i = m.getInitialColonna();
    }

    public void setFree (Pair<Integer,Integer> pos){
        campo[pos.first][pos.second]=1;
    }

    private ArrayList <Pair<Integer,Integer>> solve (int r, int c, int [][] sol, int r_f, int c_f, ArrayList <Pair<Integer,Integer>> v){
        //caso base 1: sono fuori dalla matrice
        if(r<0 || r >= campo.length || c<0 || c >= campo[0].length) return null;

        //caso base 2: sono gia passato o c'è una mina
        if(sol[r][c]!=0 || campo[r][c]!=1)return null;

        //marco pos
        sol[r][c]=1;
        v.add(new Pair<>(r,c));

        //caso base 3: sono arrivato a destinazione
        if(r == r_f && c == c_f)return v;

        //casi
        ArrayList <Pair<Integer,Integer>> [] soluzioni = new ArrayList[4];
        soluzioni [0] = solve(r+1, c, sol, r_f, c_f,v);
        soluzioni [1] = solve(r-1, c, sol, r_f, c_f,v);
        soluzioni [2] = solve(r, c+1, sol, r_f, c_f,v);
        soluzioni [3] = solve(r, c-1, sol, r_f, c_f,v);

        int best = Integer.MAX_VALUE, pos_best = -1;
        for(int i=0; i<4; ++i){
            int parziale = soluzioni[i].size();
            if(parziale < best){
                best = parziale;
                pos_best = i;
            }
        }

        //backtrack
        sol[r][c]=0;
        ArrayList <Pair<Integer,Integer>> percorso = (ArrayList <Pair<Integer,Integer>>) soluzioni[pos_best].clone();//clono cosi referenzio un oggetto diverso
        v.remove(v.size()-1);

        return percorso;
    }

    private ArrayList <Pair<Integer,Integer>> getSolution(Pair<Integer, Integer> arrivo){
        int [][] m = new int [campo.length][campo[0].length];
        ArrayList <Pair<Integer,Integer>> v = new ArrayList<>();
        return solve(r_i, c_i, m, arrivo.first, arrivo.second,v);
    }

    public ArrayList<Integer> getComandi (Pair<Integer, Integer> arrivo){
        ArrayList <Pair<Integer,Integer>> sol = getSolution(arrivo);
        ArrayList<Integer> andata = new ArrayList<>(), ritorno = new ArrayList<>();

        Pair<Integer,Integer> x, pos = new Pair<>(r_i, c_i);
        for(int i = 1; i < sol.size(); ++i){
            x = sol.get(i);


            //andata
            //punto la direzione corretta
            if( pos.first == x.first){//riga uguale quindi mi devo spostare in orizzontale
                if(pos.second < x.second){// devo andare a destra
                    andata.add(Comandi.DX);
                }else{// devo andare a sinistra
                    andata.add(Comandi.SX);
                }
            }else{// mi devo spostare in verticale
                if(pos.first < x.first){// devo andare giu
                    andata.add(Comandi.GIU);
                }else{// devo andare a su
                    andata.add(Comandi.SU);
                }
            }
            //avanto
            andata.add(Comandi.AVANZA);

            //ritorno (è tipo tutto al contrario)
            //metto tutto in pos 0 cosi shifta il resto in avanti e non devo invertirlo alla fine
            ritorno.add(0, Comandi.AVANZA);
            //punto la direzione corretta
            if( pos.first == x.first){//riga uguale quindi mi devo spostare in orizzontale
                if(pos.second < x.second){// devo andare a sinistra
                    andata.add(0, Comandi.SX);
                }else{// devo andare a destra
                    andata.add(0, Comandi.DX);
                }
            }else{// mi devo spostare in verticale
                if(pos.first < x.first){// devo andare su
                    andata.add(0, Comandi.SU);
                }else{// devo andare a giu
                    andata.add(0, Comandi.GIU);
                }
            }


            //aggiorno la posizione
            pos = x;
        }

        //raccolgo la mina
         andata.add(Comandi.RACCOGLI);
        //concateno il ritorno all' andata
        andata.addAll(ritorno);
        return andata;//non mollo la palla, ma non sapendo in che direzione è la zona sicura lo faccio "fuori"
    }
}
