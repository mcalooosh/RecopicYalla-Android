package com.yallaproductionz.recopicyalla.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.yallaproductionz.recopicyalla.R;

import java.util.ArrayList;

/**
 * Created by Aloush on 9/22/2016.
 */
public class PredictionsAdapter extends RecyclerView.Adapter<PredictionsAdapter.ViewHolder> {
    private ArrayList<Prediction> predictions;
    private Context context;

    public PredictionsAdapter(Context context, ArrayList<Prediction> predictions) {
        this.context = context;
        this.predictions = predictions;
    }

    @Override
    public PredictionsAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.rowcard, viewGroup, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {

        viewHolder.tv_name.setText(predictions.get(i).getName());
        double perc= Math.floor((predictions.get(i).getPercentage()*100.0f) * 100) / 100;

        viewHolder.tv_percentage.setText(String.valueOf(perc)+"%");
    }

    @Override
    public int getItemCount() {
        return predictions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView tv_name,tv_percentage;

        public ViewHolder(View view) {
            super(view);

            tv_name = (TextView)view.findViewById(R.id.nameTV);
            tv_percentage = (TextView)view.findViewById(R.id.percentageTV);
        }
    }
}
