package com.example.shivamgandhi.gesture;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class DisplayGameCustomAdapter extends BaseAdapter {

    Context context;
    ArrayList<String> GameIDs;

    DisplayGameCustomAdapter(Context context, ArrayList<String> gameIDs){
        this.context = context;
        GameIDs = gameIDs;

    }

    @Override
    public int getCount() {
        return GameIDs.size();
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
            V = vi.inflate(R.layout.display_game_adapter, null);
        }
        String game = GameIDs.get(position);

        TextView gID = V.findViewById(R.id.btn);
        gID.setText(game);

        return V;
    }
}
