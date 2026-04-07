package com.rituraj.shivamkiranastore.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.rituraj.shivamkiranastore.Models.User;
import com.rituraj.shivamkiranastore.R;
import com.rituraj.shivamkiranastore.ui.admin.AdminDashboardActivity;

import java.util.ArrayList;

public class ImageSliderRecycler extends RecyclerView.Adapter<ImageSliderRecycler.ViewHolder> {

    private ArrayList<Integer> images;
    private Context context;
    private String email;

    public ImageSliderRecycler(Context context, ArrayList<Integer> images, String email) {
        this.context = context;
        this.images = images;
        this.email = email;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.home_shalider, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.imageView.setImageResource(images.get(position));
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            imageView = itemView.findViewById(R.id.homeShaliderImg);
        }

        @Override
        public void onClick(View view) {
            Toast.makeText(context, "Click", Toast.LENGTH_SHORT).show();
            if (email.equals("riturajkumar1105@gmail.com") || email.equals("shivamkumarjkt@gmail.com") || email.equals("shubham756291@gmail.com"))
                context.startActivity(new Intent(context, AdminDashboardActivity.class));
        }
    }
}
