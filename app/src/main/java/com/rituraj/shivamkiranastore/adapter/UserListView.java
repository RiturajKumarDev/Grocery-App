package com.rituraj.shivamkiranastore.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.shivamkiranastore.Models.User;
import com.rituraj.shivamkiranastore.R;

import java.util.ArrayList;

public class UserListView extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<User> users;
    private FirebaseDatabase database;
    private DialogInterface myDialog;
    private User currUser;
    private ValueEventListener listener;

    public UserListView(Context context, ArrayList<User> users, User currUser) {
        this.context = context;
        this.users = users;
        database = FirebaseDatabase.getInstance();
        this.currUser = currUser;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_card_list, parent, false);
        return new UserListView.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        User user = users.get(position);
        ViewHolder vh = (ViewHolder) holder;

        Glide.with(context)
                .load(user.getProfileUrl())
                .placeholder(R.drawable.baseline_account_circle_24)
                .error(R.drawable.baseline_account_circle_24)
                .into(vh.onlineUserProfileImgList);
        vh.userId = user.getUserId();
        vh.onlineUserNameList.setText(user.getUserName());
        if (user.isOnline())
            vh.greenLed.setVisibility(View.VISIBLE);
        else
            vh.greenLed.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public String userId;
        public ImageView onlineUserProfileImgList;
        public TextView onlineUserNameList;
        public View greenLed;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            onlineUserProfileImgList = itemView.findViewById(R.id.onlineUserProfileImgList);
            onlineUserNameList = itemView.findViewById(R.id.onlineUserNameList);
            greenLed = itemView.findViewById(R.id.greenLed);
        }
    }
}
