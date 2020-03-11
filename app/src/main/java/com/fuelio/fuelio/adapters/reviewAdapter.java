package com.fuelio.fuelio.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.recyclerview.widget.RecyclerView;

import com.fuelio.fuelio.R;
import com.fuelio.fuelio.models.review;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

;


public class reviewAdapter extends RecyclerView.Adapter<reviewAdapter.ViewHolder> implements Filterable {

    Context context;
    private List<review> reviewList;
    private List<review> reviewListFiltered;

    public reviewAdapter(List<review> source, Context context) {
        super();
        this.reviewList = source;
        this.context = context;
        this.reviewListFiltered=source;
    }

    @Override
    public int getItemViewType(int position) {
        review pp = reviewList.get(position);
        return 0;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_card, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder Viewholder, int position) {
        final review review1 = reviewListFiltered.get(position);

        Viewholder.name.setText(review1.getName());
        Viewholder.review.setText(review1.getReview());
        Viewholder.ratingBar.setRating(review1.getRating());

    }

    @Override
    public int getItemCount() {
        return reviewListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    reviewListFiltered = reviewList;
                } else {
                    List<review> filteredList = new ArrayList<>();
                    for (review row : reviewList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getName().toLowerCase().contains(charString.toLowerCase()) || row.getName().contains(charSequence)) {
                            filteredList.add(row);
                        }
                    }

                    reviewListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = reviewListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                reviewListFiltered = (ArrayList<review>) filterResults.values;

                // refresh the list with filtered data
                notifyDataSetChanged();
            }
        };
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView review,name;

        ImageView statusImg;
        AppCompatRatingBar ratingBar;

        public ViewHolder(View itemView) {

            super(itemView);

            ratingBar=itemView.findViewById(R.id.rating);
            review = itemView.findViewById(R.id.tvReview);
            name = itemView.findViewById(R.id.tvName);


        }
    }

}