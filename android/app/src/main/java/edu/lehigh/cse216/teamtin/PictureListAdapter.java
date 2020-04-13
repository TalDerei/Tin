package edu.lehigh.cse216.teamtin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

class PictureListAdapter extends RecyclerView.Adapter<PictureListAdapter.ViewHolder> {

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mPicture;

        ViewHolder(View itemView) {
            super(itemView);
            this.mPicture = itemView.findViewById(R.id.listItemImage);
        }
    }

    interface ClickListener{
        void onClick(File d);
    }
    private ClickListener mClickListener;
    private ClickListener mImageClickListener;
    ClickListener getClickListener() {return mClickListener;}
    void setClickListener(ClickListener c) { mClickListener = c;}
    void setImageClickListener(ClickListener d) { mImageClickListener = d;}

    private ArrayList<File> mData;
    private LayoutInflater mLayoutInflater;

    PictureListAdapter(Context context, ArrayList<File> data) {
        mData = data;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

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
        // try parent, false); instead of null but then it spaces them out
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final File d = mData.get(position);
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
        holder.mPicture.setOnClickListener(listener2);
    }
}

