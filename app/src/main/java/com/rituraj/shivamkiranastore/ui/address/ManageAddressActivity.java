package com.rituraj.shivamkiranastore.ui.address;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.shivamkiranastore.MainActivity;
import com.rituraj.shivamkiranastore.R;
import com.rituraj.shivamkiranastore.Models.AddressModel;
import com.rituraj.shivamkiranastore.adapter.AddressAdapter;
import com.rituraj.shivamkiranastore.ui.order.OrderActivity;

import java.util.ArrayList;
import java.util.List;

public class ManageAddressActivity extends AppCompatActivity implements AddressAdapter.OnAddressActionListener {
    private RecyclerView recyclerView;
    private AddressAdapter adapter;
    private List<AddressModel> addressList;
    private Button btnProceed;
    private FloatingActionButton fabAddAddress;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private String productId, cartId;
    private String userId;
    private boolean isProduct = false, isCartItems = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_address);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.logo_blue));
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        init();
        setupToolbar();
        setupRecyclerView();
        loadSampleData();
        setupClickListeners();
    }

    private void init() {
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        productId = getIntent().getStringExtra("productId");
        cartId = getIntent().getStringExtra("cartId");
        if (productId != null && !productId.isEmpty()) {
            isProduct = true;
            btnProceed.setVisibility(View.VISIBLE);
        } else if (cartId != null && !cartId.isEmpty()) {
            isCartItems = true;
            btnProceed.setVisibility(View.VISIBLE);
        } else {
            fabAddAddress.setVisibility(View.VISIBLE);
            btnProceed.setVisibility(View.GONE);
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        btnProceed = findViewById(R.id.btnProceed);
        fabAddAddress = findViewById(R.id.fabAddAddress);
        fabAddAddress.setOnClickListener((v) -> {
            Intent intent = new Intent(ManageAddressActivity.this, AddAddressActivity.class);
            startActivity(intent);
            Toast.makeText(this, "Add new address", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Addresses");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        addressList = new ArrayList<>();
        adapter = new AddressAdapter(addressList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadSampleData() {
        reference = database.getReference("Address").child(userId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && getApplicationContext() != null) {
                    addressList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        AddressModel address = dataSnapshot.getValue(AddressModel.class);
                        if (address != null) {
                            address.setId(dataSnapshot.getKey());
                            addressList.add(address);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Sample data for testing
        addressList.add(new AddressModel("HOME", "NAME", "+91 9876543210", "", "111111", "123, Main Street", "Sector 62", "Noida", "State"));
    }

    private void setupClickListeners() {
        btnProceed.setOnClickListener(v -> {
            AddressModel selectedAddress = adapter.getSelectedAddress();
            if (selectedAddress != null) {
                Toast.makeText(this, "Proceeding with address: " + selectedAddress.getFullName(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ManageAddressActivity.this, OrderActivity.class);
                intent.putExtra("addressId", selectedAddress.getId());
                if (isProduct) intent.putExtra("productId", productId);
                else if (isCartItems) intent.putExtra("cartId", cartId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please select an address", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEditClick(AddressModel address, int position) {
        Toast.makeText(this, "Edit address: " + address.getFullName(), Toast.LENGTH_SHORT).show();
        String addressId = addressList.get(position).getId();
        Intent intent = new Intent(this, AddAddressActivity.class);
        intent.putExtra("addressId", addressId);
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(AddressModel address, int position) {
        new AlertDialog.Builder(this).setTitle("Delete Address").setMessage("Are you sure you want to delete this address?").setPositiveButton("Delete", (dialog, which) -> {
            addressList.remove(position);
            adapter.notifyItemRemoved(position);
            Toast.makeText(this, "Address deleted", Toast.LENGTH_SHORT).show();
            database.getReference("Address").child(userId).child(addressList.get(position).getId()).removeValue();
            dialog.dismiss();
        }).setNegativeButton("Cancel", null).show();
    }

    @Override
    public void onSetDefaultClick(AddressModel address, int position) {
        Toast.makeText(this, "Default address updated", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAddressSelected(AddressModel address, int position) {
        Toast.makeText(this, "Selected: " + address.getFullName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}