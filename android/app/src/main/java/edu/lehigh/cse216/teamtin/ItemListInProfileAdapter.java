package edu.lehigh.cse216.teamtin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ItemListInProfileAdapter extends RecyclerView.Adapter<ItemListInProfileAdapter.ViewHolder> {

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView mText;

        ViewHolder(View itemView) {
            super(itemView);
            this.mText = itemView.findViewById(R.id.listItemText);
        }
    }

    private ArrayList<String> mTexts;
    private LayoutInflater mLayoutInflater;

    ItemListInProfileAdapter(Context context, ArrayList<String> messages) {
        mTexts = messages;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getItemCount() {
        return mTexts.size();
    }

    @NonNull
    @Override
    public ItemListInProfileAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        @SuppressLint("InflateParams") View view = mLayoutInflater.inflate(R.layout.list_item_profile,
                null);
        // E/RecyclerView: No adapter attached
        // try parent, false); instead of null but then it spaces thme out
        return new ItemListInProfileAdapter.ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String message = mTexts.get(position);
        holder.mText.setText(message);
    }
}
