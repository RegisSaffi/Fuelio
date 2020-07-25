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
import com.fuelio.fuelio.models.requestCard;

import java.util.List;


public class requestCardsAdapter extends RecyclerView.Adapter<requestCardsAdapter.ViewHolder> {

    Context context;
    private List<requestCard> requestCardList;

    public requestCardsAdapter(List<requestCard> source, Context context) {
        super();
        this.requestCardList = source;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        requestCard pp = requestCardList.get(position);

        return 0;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.station_request_card, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder Viewholder, int position) {
        final requestCard requestCard1 = requestCardList.get(position);

        Viewholder.date.setText(requestCard1.getDate());
        Viewholder.issue.setText(requestCard1.getIssue());
        Viewholder.name.setText(requestCard1.getDesc());

        if(requestCard1.getStatus().equals("pending")){
            Viewholder.statusImg.setImageDrawable(VectorDrawableCompat.create(context.getResources(),R.drawable.ic_access_time_black_24dp,context.getTheme()));
        }else if(requestCard1.getStatus().equals("accepted")){
            Viewholder.statusImg.setImageDrawable(VectorDrawableCompat.create(context.getResources(),R.drawable.ic_check_black_24dp,context.getTheme()));
        }else if(requestCard1.getStatus().equals("completed")){
            Viewholder.statusImg.setImageDrawable(VectorDrawableCompat.create(context.getResources(),R.drawable.ic_check_circle_black_24dp,context.getTheme()));
        }else if(requestCard1.getStatus().equals("paid")){
            Viewholder.statusImg.setImageDrawable(VectorDrawableCompat.create(context.getResources(),R.drawable.ic_done_all_black_24dp,context.getTheme()));
        }else{
            Viewholder.statusImg.setImageDrawable(VectorDrawableCompat.create(context.getResources(),R.drawable.ic_report_problem_black_24dp,context.getTheme()));
        }
    }

    @Override
    public int getItemCount() {
        return requestCardList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView date,issue,name;
        ImageView statusImg;

        public ViewHolder(View itemView) {

            super(itemView);

            statusImg=itemView.findViewById(R.id.statusImg);
            date=  itemView.findViewById(R.id.tvDate);
            issue=  itemView.findViewById(R.id.tvIssue);
            name=itemView.findViewById(R.id.tvName);

        }
    }

}