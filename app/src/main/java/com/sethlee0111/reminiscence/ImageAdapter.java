package com.sethlee0111.reminiscence;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private File[] mDataset;
    private Context context;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ImageView imageView;
        public ImageViewHolder(ImageView v) {
            super(v);
            imageView = v;
        }
    }
    // Provide a suitable constructor (depends on the kind of dataset)
    public ImageAdapter(Context context, File[] myDataset) {
        mDataset = myDataset;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ImageAdapter.ImageViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new ImageView
        ImageView v = (ImageView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_image_view, parent, false);

        ImageViewHolder vh = new ImageViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ImageViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Bitmap myBitmap = BitmapFactory.decodeFile(mDataset[position].getAbsolutePath());
        holder.imageView.setImageBitmap(myBitmap);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PhotoContextSpecActivity.class);
                intent.putExtra("FilePath", mDataset[position].getAbsolutePath());
                context.startActivity(intent);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.length;
    }

}
