package com.rituraj.shivamkiranastore.ui.admin.ui.dasboard;

import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rituraj.shivamkiranastore.R;
import com.rituraj.shivamkiranastore.ui.admin.ui.UploadProductActivity;
import com.rituraj.shivamkiranastore.ui.product.ProductActivity;

public class DashboardFragment extends Fragment {
    private View view;
    private DashboardViewModel mViewModel;
    private TextView tvTotalProducts, tvTotalCustomers, tvTotalOrders;
    private FirebaseDatabase database;
    private DatabaseReference reference;

    public static DashboardFragment newInstance() {
        return new DashboardFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        fun();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        // TODO: Use the ViewModel
    }

    private void fun() {
        LinearLayout tvUploadProduct = view.findViewById(R.id.tvUploadProduct);
        LinearLayout productPage = view.findViewById(R.id.productPage);
        tvUploadProduct.setOnClickListener((v) -> {
            requireContext().startActivity(new Intent(requireContext(), UploadProductActivity.class));
        });
        init();
        initViews();
        getTotalCustomers();
        getTotalOrders();
        getTotalProducts();
    }

    private void init() {
        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
    }

    private void initViews() {
        tvTotalCustomers = view.findViewById(R.id.tvTotalCustomers);
        tvTotalOrders = view.findViewById(R.id.tvTotalOrders);
        tvTotalProducts = view.findViewById(R.id.tvTotalProducts);
        tvTotalCustomers.setText("0");
        tvTotalProducts.setText("0");
        tvTotalOrders.setText("0");
    }

    private void getTotalProducts() {
        reference = database.getReference("Products");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    tvTotalProducts.setText(String.valueOf(snapshot.getChildrenCount()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getTotalCustomers() {
        reference = database.getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    tvTotalCustomers.setText(String.valueOf(snapshot.getChildrenCount()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getTotalOrders() {
        reference = database.getReference("Orders");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    tvTotalOrders.setText(String.valueOf(snapshot.getChildrenCount()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}