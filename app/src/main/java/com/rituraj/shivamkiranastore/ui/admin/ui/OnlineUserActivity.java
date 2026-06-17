package com.rituraj.shivamkiranastore.ui.admin.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.shivamkiranastore.Models.User;
import com.rituraj.shivamkiranastore.R;
import com.rituraj.shivamkiranastore.adapter.UserListView;
import com.rituraj.shivamkiranastore.databinding.ActivityOnlineUserBinding;

import java.util.ArrayList;

public class OnlineUserActivity extends AppCompatActivity {
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private FirebaseAuth auth;
    private ProgressDialog progressDialog;
    private String currUserEmail;
    private User currUser = new User();
    private ArrayList<User> users = new ArrayList<>();
    private RecyclerView recyclerView;
    private UserListView userAdapter;
    private TextView userStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_online_user);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.logo_blue));
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        setupRecyclerView();
        fun();
    }

    private void setupRecyclerView() {
        userStatus = findViewById(R.id.userStatus);
        recyclerView = findViewById(R.id.userRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userAdapter = new UserListView(this, users, currUser);
        recyclerView.setAdapter(userAdapter);
    }

    public void fun() {
        if (auth.getCurrentUser() == null) return;

        currUserEmail = auth.getCurrentUser().getEmail();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Loading users ...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        currUser.setUserId(auth.getCurrentUser().getUid());
        currUser.setProfileUrl(String.valueOf(auth.getCurrentUser().getPhotoUrl()));
        currUser.setEmail(auth.getCurrentUser().getEmail());
        currUser.setUserName(auth.getCurrentUser().getDisplayName());

        getAllUsers();
    }

    public void getAllUsers() {
        reference = database.getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                users.clear();
                int onlineUsers = 0;
                for (DataSnapshot snap : snapshot.getChildren()) {
                    User user = snap.getValue(User.class);
                    if (user != null) {
                        user.setUserId(snap.getKey());
                        Boolean online = snap.child("isOnline").getValue(Boolean.class);
                        user.setOnline(online != null && online);
                        if (user.isOnline()) {
                            users.add(0, user);
                            onlineUsers++;
                        } else
                            users.add(user);
                    }
                }
                userStatus.setText(String.valueOf("Online Users: " + onlineUsers + " & Total Users: " + users.size()));
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Toast.makeText(OnlineUserActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
