package com.example.buggerduckbot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Pair;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

public class MapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent myIntent = getIntent(); // gets the previously created intent
        Map map = myIntent.getParcelableExtra("map");
        int task = myIntent.getIntExtra("taskId", 0);

        final GridView mapLayout =  findViewById(R.id.map);
        mapLayout.setNumColumns(map.getDimension().second);
        mapLayout.setAdapter(new CellAdapter(this, R.drawable.empty_square, map.getDimension().first*map.getDimension().second));


    }

    private void taskOne(){

    }

    private void taskTwo(){

    }

    private void taskThree(){

    }
}
