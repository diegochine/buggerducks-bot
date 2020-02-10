package com.example.buggerduckbot;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class CellAdapter extends BaseAdapter {
    private final Context context;
    private final int resourceId;
    private int height;
    private Map map;

    public CellAdapter(Context context, int resourceId,  Map map){
        this.context = context;
        this.resourceId = resourceId;
        this.height = 200;
        this.map = map;
    }


    @Override
    public int getCount() {
        return this.map.getCellNumber();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        int y = map.getNumeroColonne();

        if (view == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(R.layout.cell_adapter, null);
        }
        final ImageView imageView = view.findViewById(R.id.cell);
        if (map.hasFound(i/y, i%y)){
            imageView.setImageResource(R.drawable.square_ball);
        }else {
            imageView.setImageResource(this.resourceId);
        }
        imageView.getLayoutParams().height = this.height;

        return view;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
