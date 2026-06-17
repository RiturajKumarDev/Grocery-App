package com.rituraj.shivamkiranastore.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rituraj.shivamkiranastore.MainActivity;
import com.rituraj.shivamkiranastore.Models.HeroImage;
import com.rituraj.shivamkiranastore.Models.User;
import com.rituraj.shivamkiranastore.R;
import com.rituraj.shivamkiranastore.ui.admin.AdminDashboardActivity;

import java.util.ArrayList;

public class ImageSliderRecycler extends RecyclerView.Adapter<ImageSliderRecycler.ViewHolder> {

    private ArrayList<HeroImage> images;
    private Context context;
    private String email;
    private FirebaseDatabase database;

    public ImageSliderRecycler(Context context, ArrayList<HeroImage> images, String email) {
        this.context = context;
        this.images = images;
        this.email = email;
        database = FirebaseDatabase.getInstance();
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
        holder.imageId = images.get(position).getImageId();
        try {
            Glide.with(context)
                    .load(decodeBase64ToImage(images.get(position).getImageUrl()))
                    .placeholder(R.drawable.shivam_kirana_store_bg)
                    .error(R.drawable.shivam_kirana_store_bg)
                    .into(holder.imageView);
        } catch (Exception e) {
            holder.imageView.setImageResource(images.get(position).getImage());
        }
    }

    private Bitmap decodeBase64ToImage(String base64) {
        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView imageView;
        public String imageId;

        ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            imageView = itemView.findViewById(R.id.homeShaliderImg);
        }

        @Override
        public void onClick(View view) {
            if (email != null && email.equals("riturajkumar1105@gmail.com") || email.equals("shivamkumarjkt@gmail.com") || email.equals("shubham756291@gmail.com")) {
                new AlertDialog.Builder(context).setTitle("Delete Hero Image").setMessage("Are you sure you want to delete this Hero Image?").setPositiveButton("Delete", (dialog, which) -> {
                    DatabaseReference reference = database.getReference().child("HeroImages").child(imageId);
                    reference.removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(context, "Delete Image Success ....", Toast.LENGTH_SHORT).show();
                                }
                            });
                    dialog.dismiss();
                }).setNegativeButton("Cancel", null).show();
            }
        }
    }
}
