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

    private int [][] solve (int r, int c, int [][] sol, int r_f, int c_f){
        //caso base 1: sono fuori dalla matrice
        if(r<0 || r >= campo.length || c<0 || c >= campo[0].length) return null;

        //caso base 2: sono gia passato o c'è una mina
        if(sol[r][c]!=0 || campo[r][c]!=1)return null;

        //marco pos
        sol[r][c]=1;

        //caso base 3: sono arrivato a destinazione
        if(r == r_f && c == c_f)return sol;

        //casi
        int [][][] soluzioni = new int [4] [sol.length] [sol[0].length];
        soluzioni [0] = solve(r+1, c, sol, r_f, c_f);
        soluzioni [1] = solve(r-1, c, sol, r_f, c_f);
        soluzioni [2] = solve(r, c+1, sol, r_f, c_f);
        soluzioni [3] = solve(r, c-1, sol, r_f, c_f);

        int best = Integer.MAX_VALUE, pos_best = -1;
        for(int i=0; i<4; ++i){
            int parziale = n_passi(soluzioni[i]);
            if(parziale < best){
                best = parziale;
                pos_best = i;
            }
        }

        //backtrack
        sol[r][c]=0;

        return soluzioni[pos_best];
    }

    private int n_passi (int [][] m){
        if(m==null)return Integer.MAX_VALUE;
        int n=0;
        for(int r = 0; r < campo.length; ++r){
            for(int c = 0; c < campo[0].length; ++c){
                n+=m[r][c];
            }
        }
        return n;
    }

    /*private ArrayList<Pair<Integer, Integer>> calcola_percorso(int r, int c, int [][] m, ArrayList<Pair<Integer, Integer>> old){
        m[r][c]=0;

    }

    public ArrayList<Pair<Integer, Integer>> get_percorso(int r, int c){
        int [][] parametro = new int [campo.length][campo[0].length];
        for(int i =0; i<campo.length; ++i) Arrays.fill(parametro[i], 0);

        int [][] sol = solve(r_i, c_i, parametro, r, c);

        ArrayList<Pair<Integer, Integer>> percorso = calcola_percorso(r_i, c_i);
    }*/
}
