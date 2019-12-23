package com.example.buggerduckbot;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class CellAdapter extends BaseAdapter {
    private final Context context;
    private final int resourceId;
    private final int n;

    public CellAdapter(Context context, int resourceId, int n){
        this.context = context;
        this.resourceId = resourceId;
        this.n = n;
    }


    @Override
    public int getCount() {
        return n;
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
        ImageView cell = new ImageView(this.context);
        cell.setImageResource(resourceId);
        return cell;
    }
}
