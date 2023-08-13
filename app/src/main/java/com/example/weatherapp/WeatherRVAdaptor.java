package com.example.weatherapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class WeatherRVAdaptor extends RecyclerView.Adapter<WeatherRVAdaptor.ViewHolder> {

    private Context context;
    private ArrayList<WeatherRVModel> weatherRVModelArrayList;

    public WeatherRVAdaptor(Context context, ArrayList<WeatherRVModel> weatherRVModelArrayList) {
        this.context = context;
        this.weatherRVModelArrayList = weatherRVModelArrayList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private TextView tvTime,tvTemp,tvWindSpeed;
        private ImageView ivCondition;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTime = itemView.findViewById(R.id.tvTime);
            tvTemp = itemView.findViewById(R.id.tvTemp);
            tvWindSpeed = itemView.findViewById(R.id.tvWindSpeed);
            ivCondition = itemView.findViewById(R.id.ivCondition);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.weather_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            WeatherRVModel model = weatherRVModelArrayList.get(position);
            holder.tvTemp.setText(model.getTemp() + "°C");
            Picasso.get().load("https:".concat(model.getIcon())).into(holder.ivCondition);
            holder.tvWindSpeed.setText(model.getWindSpeed()+"Km/h");
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd hh:mm");
            SimpleDateFormat output = new SimpleDateFormat("hh:mm aa");
            try{
                Date t = input.parse(model.getTime());
                holder.tvTime.setText(output.format(t));
            }catch (ParseException e){
                e.printStackTrace();
            }
    }

    @Override
    public int getItemCount() {
        return weatherRVModelArrayList.size();
    }
}
