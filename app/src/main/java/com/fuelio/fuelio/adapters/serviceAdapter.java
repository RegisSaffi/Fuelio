package com.fuelio.fuelio.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
;

import com.fuelio.fuelio.R;
import com.fuelio.fuelio.models.service;

import java.util.ArrayList;
import java.util.List;


public class serviceAdapter extends RecyclerView.Adapter<serviceAdapter.ViewHolder> implements Filterable {

    Context context;
    private List<service> serviceList;
    private List<service> serviceListFiltered;

    public serviceAdapter(List<service> source, Context context) {
        super();
        this.serviceList = source;
        this.context = context;
        this.serviceListFiltered=source;
    }

    @Override
    public int getItemViewType(int position) {
        service pp = serviceList.get(position);
        return 0;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.service_card, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder Viewholder, int position) {
        final service service1 = serviceListFiltered.get(position);

        Viewholder.name.setText(service1.getName());
        Viewholder.amount.setText("Starting: "+service1.getAmount()+" RWF");
    }

    @Override
    public int getItemCount() {
        return serviceListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    serviceListFiltered = serviceList;
                } else {
                    List<service> filteredList = new ArrayList<>();
                    for (service row : serviceList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getName().toLowerCase().contains(charString.toLowerCase()) || row.getName().contains(charSequence)) {
                            filteredList.add(row);
                        }
                    }

                    serviceListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = serviceListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                serviceListFiltered = (ArrayList<service>) filterResults.values;

                // refresh the list with filtered data
                notifyDataSetChanged();
            }
        };
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView amount,name;

        ImageView statusImg;
        public ViewHolder(View itemView) {

            super(itemView);

            amount = itemView.findViewById(R.id.tvAmount);
            name = itemView.findViewById(R.id.tvName);


        }
    }

}