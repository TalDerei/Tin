package edu.lehigh.cse216.teamtin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ViewHolder> {

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView mIndex;
        TextView mText;
        ImageView mProfile;

        ViewHolder(View itemView) {
            super(itemView);
            this.mIndex = itemView.findViewById(R.id.listItemIndex);
            this.mText = itemView.findViewById(R.id.listItemText);
            this.mProfile = itemView.findViewById(R.id.listItemImage);
        }
    }

    private ArrayList<Datum> mData;
    private LayoutInflater mLayoutInflater;

    ItemListAdapter(Context context, ArrayList<Datum> data) {
        mData = data;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    interface ClickListener{
        void onClick(Datum d);
    }
    private ClickListener mClickListener;
    private ClickListener mImageClickListener;
    ClickListener getClickListener() {return mClickListener;}
    void setClickListener(ClickListener c) { mClickListener = c;}
    void setImageClickListener(ClickListener d) { mImageClickListener = d;}

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        @SuppressLint("InflateParams") View view = mLayoutInflater.inflate(R.layout.list_item,
                null);
        // E/RecyclerView: No adapter attached
        // try parent, false); instead of null but then it spaces thme out
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Datum d = mData.get(position);
        holder.mIndex.setText(d.mSubject);
        holder.mText.setText(d.mText);
        final View.OnClickListener listener = new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mClickListener.onClick(d);
            }
        };
        final View.OnClickListener listener2 = new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mImageClickListener.onClick(d);
            }
        };
        holder.mIndex.setOnClickListener(listener);
        holder.mText.setOnClickListener(listener);
        holder.mProfile.setOnClickListener(listener2);
    }
}

