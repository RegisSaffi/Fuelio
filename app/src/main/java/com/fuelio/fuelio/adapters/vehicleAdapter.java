package com.fuelio.fuelio.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.fuelio.fuelio.R;
import com.fuelio.fuelio.models.vehicle;

import java.util.List;


public class vehicleAdapter extends RecyclerView.Adapter<vehicleAdapter.ViewHolder> {

    Context context;
    private List<vehicle> vehicleList;

    public vehicleAdapter(List<vehicle> source, Context context) {
        super();
        this.vehicleList = source;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        vehicle pp = vehicleList.get(position);
        if(pp.getIsSmall()){
            return 0;
        }
        return 1;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(viewType==0){
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.vehicle_card2, parent, false);
            ViewHolder viewHolder = new ViewHolder(v);

            return viewHolder;
        }else{
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.vehicle_card, parent, false);
            ViewHolder viewHolder = new ViewHolder(v);

            return viewHolder;
        }

    }

    @Override
    public void onBindViewHolder(ViewHolder Viewholder, int position) {
        final vehicle vehicle1 = vehicleList.get(position);

        Viewholder.plate.setText(vehicle1.getPlate());
        Viewholder.name.setText(vehicle1.getName());

        if(vehicle1.getIsService()){
            Viewholder.car.setVisibility(View.GONE);
        }
        if(vehicle1.getIsSmall()){

            if(vehicle1.getSelected()){
                Viewholder.parent.setBackgroundColor(context.getResources().getColor(R.color.colorTransparentPrimary));
            }else{
                Viewholder.parent.setBackgroundColor(context.getResources().getColor(R.color.white));
            }

        }


    }

    @Override
    public int getItemCount() {
        return vehicleList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView name,plate;
        LinearLayout parent;
        ImageView car;


        public ViewHolder(View itemView) {

            super(itemView);

            parent=itemView.findViewById(R.id.parent);
            plate=itemView.findViewById(R.id.tvPlate);
            name=  itemView.findViewById(R.id.tvName);
            car=itemView.findViewById(R.id.car);


        }
    }

}