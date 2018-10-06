package com.example.shivamgandhi.gesture;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter {

    Context context;
    ArrayList<String> playerList;
    ArrayList<String> status;

    CustomAdapter(Context context, ArrayList<String> playerList, ArrayList<String> status){
        this.context = context;
        this.playerList = playerList;
        this.status = status;
    }

    @Override
    public int getCount() {
        return playerList.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        View V = view;
        if (V == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(context);
            V = vi.inflate(R.layout.temp, null);
        }
        String playerName = playerList.get(position);
        String stat = status.get(position);
        TextView tv1 = V.findViewById(R.id.temp_name);
        TextView tv2 = V.findViewById(R.id.temp_status);

        tv1.setText(playerName);
        tv2.setText(stat);


        return V;
    }
}
