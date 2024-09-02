package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

public class ColorAdapter extends ArrayAdapter<Integer> {
    private final Context context;
    private final List<Integer> colors;

    public ColorAdapter(Context context, List<Integer> colors) {
        super(context, 0, colors);
        this.context = context;
        this.colors = colors;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        View colorView = convertView.findViewById(android.R.id.text1);
        colorView.setBackgroundColor(colors.get(position));
        return convertView;
    }
}

