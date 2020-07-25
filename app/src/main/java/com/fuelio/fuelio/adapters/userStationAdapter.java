package com.fuelio.fuelio.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.fuelio.fuelio.R;
import com.fuelio.fuelio.models.userStation;

import java.util.List;


public class userStationAdapter extends RecyclerView.Adapter<userStationAdapter.ViewHolder> {

    Context context;
    private List<userStation> userStationList;

    public userStationAdapter(List<userStation> source, Context context) {
        super();
        this.userStationList = source;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        userStation pp = userStationList.get(position);

        return 0;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_station_card, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder Viewholder, int position) {
        final userStation userStation1 = userStationList.get(position);

        Viewholder.type.setText(userStation1.getType());
        Viewholder.address.setText(userStation1.getPhone());
        Viewholder.name.setText(userStation1.getName());

        if(userStation1.getType().equals("user")){
            Viewholder.statusImg.setImageDrawable(VectorDrawableCompat.create(context.getResources(),R.drawable.ic_person_black_24dp,context.getTheme()));
        }else{
            Viewholder.statusImg.setImageDrawable(VectorDrawableCompat.create(context.getResources(),R.drawable.ic_local_gas_station_black_24dp,context.getTheme()));
        }

    }

    @Override
    public int getItemCount() {
        return userStationList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView name,type,address;
        ImageView statusImg;

        public ViewHolder(View itemView) {

            super(itemView);

            statusImg=itemView.findViewById(R.id.statusImg);
            type=  itemView.findViewById(R.id.type);
           address=  itemView.findViewById(R.id.address);
            name=itemView.findViewById(R.id.name);

        }
    }

}